package team4.aalto.fi.userInterface.createProject

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_createproject.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import team4.aalto.fi.dataInterface.CreateProjectView
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.util.HttpRequest
import team4.aalto.fi.util.Util
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch


class CreateProjectPresenter(private val view: CreateProjectView) {

    //array of keyword
    var arrayOfKeywords = arrayOf("?-", "?-", "?-")
    private var client = OkHttpClient()
    private var request = HttpRequest(client)
    private var img_url = ""
    private var project_id = ""

    //function to add max 3 keywords
    fun addKeyword(createProjectActivity: CreateProjectActivity, keyword: String) {
        if(keyword == "") {
            view.showKeywordError("Empty field")
            return
        }
        for(key in 1..arrayOfKeywords.size){
            if(arrayOfKeywords[key-1] == "?-"){
                arrayOfKeywords[key-1] = keyword
                view.showOkMessage(arrayOfKeywords[key-1])
                when(key){
                    1 -> {
                        createProjectActivity.key1.text = arrayOfKeywords[key-1]
                        createProjectActivity.key1.visibility = View.VISIBLE
                    }
                    2 -> {
                        createProjectActivity.key2.text = arrayOfKeywords[key-1]
                        createProjectActivity.key2.visibility = View.VISIBLE
                    }
                    3 -> {
                        createProjectActivity.key3.text = arrayOfKeywords[key-1]
                        createProjectActivity.key3.visibility = View.VISIBLE
                    }
                }
                return
            }
        }
        view.showKeywordError("max 3 keywords")
        return
    }

    //exit from 'create new project' view
    fun closeCreateNewProject() {
        view.closeCreationNewProject()
    }

    fun setIcon() {
        //open gallery
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        view.openGallery(intent)

    }

    fun setDL() {
        view.pickDL()
    }

    fun saveProject(p: Project): String {
        //connect to API and send project's data
        when {
            p.name == "" -> {
                view.showNameError("Name field empty")
                return "?-"
            }
            p.description == "" -> {
                view.showDescriptionError("Description field empty")
                return "?-"
            }
            else -> {
                //send to API
                p.project_img_url = img_url

                apiCreateProject(p)

                view.showOkMessage("Creating project...")
                //only if the project has been created

                return project_id

            }
        }
    }

    //next fun

    fun handleGallery(createProjectActivity: CreateProjectActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            CreateProjectActivity.IMAGE_PICK_CODE -> {
                if( resultCode == Activity.RESULT_OK ){ createProjectActivity.project_icon.setImageURI(data?.data)
                    val storage = FirebaseStorage.getInstance()
                    var storageReference = storage.reference
                    Log.d("Create Project", " Add Picture")
                    val filePath = data?.data
                    if (filePath != null) {
                        val progressDialog = ProgressDialog(createProjectActivity)
                        progressDialog.setTitle("Uploading...")
                        progressDialog.show()
                        val ref: StorageReference =
                            storageReference.child("images/" + UUID.randomUUID().toString())
                        ref.putFile(filePath)
                            .addOnSuccessListener { taskSnapshot ->
                                val uri: Task<Uri> =
                                    taskSnapshot.storage.downloadUrl
                                while (!uri.isComplete());
                                val url = uri?.result;

                                Log.i("FBApp1 URL ", url.toString())
                                this.img_url = url.toString()

                                progressDialog.dismiss()
                                Toast.makeText(createProjectActivity, (ref.storage).toString()+"**Uploaded**"+(ref.downloadUrl.toString()), Toast.LENGTH_SHORT).show()
                                Log.d((ref.storage).toString(),"-->"+ref.downloadUrl.toString())
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(createProjectActivity, "Error uploading the project picture " + e.message, Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
            }
            else -> {
                Toast.makeText(createProjectActivity, "Error uploading the project picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //fun apiCreateProject(project:Project,  countDownLatch: CountDownLatch){
    fun apiCreateProject(project:Project){
        var createProjectEndpoint:String = Util.url_enpoints+"/project"
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonProject: String = gson.toJson(project)
        Log.d("JSON", jsonProject.toString())

        request.POST(createProjectEndpoint, jsonProject , object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                Log.i("Success","Success: "+response.code());
                if (response.code() == 200) {
                    val responseData = response.body()?.string()
                    val jsonObject = JSONObject(responseData)
                    project_id = jsonObject.get("success").toString()
                    Log.d("Request Successful", project_id)
                   // countDownLatch.countDown();
                    view.closeCreationNewProject()

                }
            }
            override fun onFailure(call: Call?, e: IOException?) {
                println("Request Failure.")
                Log.d("Error", e.toString())
                //countDownLatch.countDown();
            }
        })

    }
    interface VolleyCallback {
        fun onSuccess(result: String?)
    }


    fun addProjectReferenceToUser(currentUser: String, projectID: String) {
        // get instance of Firebase
        val db = FirebaseFirestore.getInstance()

        //get reference to the projects of currentUser
        val userProjects = db
            .collection("Users")
            .document(currentUser)
            .collection("Projects")

        // create new map with the reference
        val data = hashMapOf(
            "reference" to projectID
        )


        //add reference to user's projects collection
        userProjects.add(data as Map<String, String>)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "[CreateProjectPresenter] User $currentUser have a new project: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "[CreateProjectPresenter] Error adding document", e)
            }

    }


    companion object {
        //img pick code
        val IMAGE_PICK_CODE = 1000
    }

}