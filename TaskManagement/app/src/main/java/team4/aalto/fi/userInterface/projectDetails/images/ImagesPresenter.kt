package team4.aalto.fi.userInterface.projectDetails.images

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import team4.aalto.fi.dataInterface.ImageFragmentView
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.usecase.ProjectImagesUseCase
import java.text.DateFormat
import java.util.*
import com.android.volley.Request.Method.POST
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject
import android.provider.MediaStore
import android.graphics.Bitmap
import android.util.Base64OutputStream
import androidx.core.net.toFile
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.android.volley.*
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream


class ImagesPresenter(private val view: ImageFragmentView, private val imageUseCase: ProjectImagesUseCase) {

    private val listOfProjectImages = mutableListOf<String>()

    /** projectFolderName should be called as the name of the project + unique value */
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference
    private lateinit var imageRef: StorageReference

    private val database = FirebaseFirestore.getInstance()
    private var projectsRef = database.collection("Projects")

    fun getImagesFromProject(projectId: String){
        imageRef = storageRef.child("images").child(projectId)
        Thread {
            val task = imageUseCase.getProjectImages(projectId)
            task.addOnSuccessListener { result ->
                for (document in result) {
                    val imgUrl = document.data["link"] as String
                    listOfProjectImages.add(imgUrl)
                    view.showImages(listOfProjectImages)
                }
            }
        }.start()
    }

    // Method to get Base64
    fun encoder(myURI: Uri, context: Context): String{
        val bytes = context.contentResolver.openInputStream(myURI)?.readBytes()
        val base64 = Base64.getEncoder().encodeToString(bytes)
        return base64
    }

    fun uploadImage(selectedImage: Uri, projectId: String, quality: String, context: Context, progressDialog : ProgressDialog) {

        val url = "https://us-central1-mcc-fall-2019-g04.cloudfunctions.net/uploadImage"

        val img_base64 = encoder(selectedImage, context)

        val STRING_LENGTH = 12;
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = (1..STRING_LENGTH)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("");

        // Post parameters
        val params = hashMapOf(
            "project" to projectId,
            "name" to randomString,
            "image" to img_base64,
            "resolution" to quality
        ) as Map<String,String>

        val body = hashMapOf(
            "data" to params
        ) as Map<String, Map<String, String>>

        val jsonObject = JSONObject(body)

        Log.w(ContentValues.TAG, "image upload body $jsonObject")

        // Volley post request with parameters
        val request = JsonObjectRequest(
            POST,url,jsonObject,
            Response.Listener { response ->
                // Process the json
                progressDialog.dismiss()
                Log.w(ContentValues.TAG, "response from image upload $response")

                val downloadUri = response.get("data") as String
                val date = DateFormat.getDateTimeInstance().format(Date())
                val projectRef = projectsRef.document(projectId)
                val imagesSubCollection = projectRef.collection("Images")
                var imageId = ""
                val file = hashMapOf(
                    "link" to downloadUri,
                    "uploadDate" to date
                )

                imagesSubCollection.add(file as Map<String,String>)
                    .addOnSuccessListener { documentReference ->
                        listOfProjectImages.add(downloadUri)
                        view.showImages(listOfProjectImages)
                        imageId = documentReference.id
                        Log.d(
                            "Images:",
                            "Image successfully written with ID: ${documentReference.id}"
                        )
                        projectRef.get()
                            .addOnSuccessListener { document ->

                                try {
                                    if (document != null) {
                                        var projectInfo = document.toObject(Project::class.java) ?: Project()
                                        Log.d("PROJECT IMAGES BEFORE: ",projectInfo.images_attached.toString())
                                        projectInfo.images_attached.add(imageId)
                                        Log.d("PROJECT IMAGES AFTER: ",projectInfo.images_attached.toString())
                                        Log.d("Images:",
                                            "Image successfully added to the project: ${projectInfo.name}")
                                        projectRef.set(projectInfo)
                                            .addOnSuccessListener { Log.d("Project :", "Project successfully updated!") }
                                            .addOnFailureListener { e -> Log.w("Project :", "Error updating the project", e) }

                                    } else {
                                        Log.w("Images:", "Error writing the subcollection image in the project")
                                    }
                                }catch (ex: Exception){
                                    Log.e(ContentValues.TAG, ex.message)
                                }
                            }.addOnFailureListener {
                                    e -> Log.e(ContentValues.TAG, "Error writing document", e)
                            }

                    }
                    .addOnFailureListener { e -> Log.w("Images:", "Error writing the image", e) }


            }, Response.ErrorListener{
                // Error in request
                progressDialog.dismiss()
                Log.w(ContentValues.TAG, "error on image upload $it")
            })


        request.retryPolicy = DefaultRetryPolicy(60000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)


        // Add the volley post request to the request queue
        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    fun saveNewImage(selectedImage: Uri, it: Context, projectId: String, user: String, context: Context) {
        println("WE ARE IN THE RIGHT PRESENTER--------------")
        val progressDialog = ProgressDialog(it)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()
        imageRef = storageRef.child("images").child(projectId)


        Thread{
            database.collection("Users").document(user).get().addOnSuccessListener {
                val quality = it?.data?.get("quality")
                /** CALL TO CLOUD FUNCTION TO RESIZE IMAGE */
                uploadImage(selectedImage, projectId, quality as String, context, progressDialog)

            }

        }.start()
    }
}