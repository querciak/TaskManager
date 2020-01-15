package team4.aalto.fi.userInterface.projectDetails

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import team4.aalto.fi.dataInterface.ProjectDetailView
import team4.aalto.fi.domain.model.Project
import java.io.File
import team4.aalto.fi.util.HttpRequest
import team4.aalto.fi.util.Util
import java.io.IOException
import java.text.DateFormat
import java.util.*
import java.util.Calendar.*

class ProjectDetailsPresenter(private val view: ProjectDetailView){

    /** projectFolderName should be called as the name of the project + unique value */
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference
    private lateinit var imageRef: StorageReference
            //= storageRef.child("images").child(projectFolderName)
    private lateinit var fileRef: StorageReference
                    //= storageRef.child("files").child(projectFolderName)

    private val database = FirebaseFirestore.getInstance()
    private var imagesRef = database.collection("Images")
    private var filesRef = database.collection("Files")
    private var projectsRef = database.collection("Projects")

    fun decideNextActivity(f: Int, it: Context, projectName: String) {

        imageRef = storageRef.child("images").child(projectName)
        fileRef = storageRef.child("files").child(projectName)
        when(f){
            0 -> {
                view.openAddNewTask()
            }
            1 -> {
                val options : Array<String?>? = arrayOf("Camera", "Gallery", "Cancel")
                //create AlertDialog with Builder
                val builder = AlertDialog.Builder(it)
                builder.setTitle("Select Options")
                builder.setItems(options, DialogInterface.OnClickListener{ dialog, which  ->
                    when(which){
                        0 -> {
                            view.openCamera()
                        }
                        1 -> {
                            view.openGallery()
                        }
                        2 -> dialog.cancel()
                    }
                })
                val dialog = builder.create()
                dialog.show()
            }
            2 -> {
                view.openAddNewFile()
            }
        }
    }

    /*fun saveNewImage(selectedImage: Uri, it: Context, projectId: String, user: String) {
        println("WE ARE IN THE Wrong PRESENTER--------------")
        imageRef = storageRef.child("images").child(projectId)
            val progressDialog = ProgressDialog(it)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            Thread{
                database.collection("Users").document(user).get().addOnSuccessListener {
                    val quality = it?.data?.get("quality")
                    /** CALL TO CLOUD FUNCTION TO RESIZE IMAGE */
                    // uploadImage(selectedImage, projectId, currentUser)

                }

                /** CALL TO CLOUD FUNCTION TO RESIZE IMAGE */
                val imageNameRef = imageRef.child(UUID.randomUUID().toString())
                val uploadTask = imageNameRef
                    .putFile(selectedImage)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Log.d(TAG, "Image uploaded")
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Log.w(TAG, "Error while uploading image")
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
                    }

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageNameRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
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
                                            Log.e(TAG, ex.message)
                                        }
                                    }.addOnFailureListener {
                                            e -> Log.e(TAG, "Error writing document", e)
                                    }

                            }
                            .addOnFailureListener { e -> Log.w("Images:", "Error writing the image", e) }



                    } else {
                        // Handle failures
                        Log.w(TAG, "Error while taking url image")
                    }
                }
            }.start()
    }
     */

    fun saveNewFile(selectedFile: Uri, it: Context,projectId: String) {
        if(selectedFile != null){
            val progressDialog = ProgressDialog(it)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()

            val fileNameRef = fileRef.child(UUID.randomUUID().toString())
            Thread{
                val uploadTask = fileNameRef
                    .putFile(selectedFile)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Log.d(TAG, "File uploaded")
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Log.w(TAG, "Error while uploading file")
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred/taskSnapshot.totalByteCount
                        progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
                    }

                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    fileNameRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        val date = DateFormat.getDateTimeInstance().format(Date())
                        val projectRef = projectsRef.document(projectId)
                        val filesSubCollection = projectRef.collection("Files")
                        val file = hashMapOf(
                            "link" to downloadUri,
                            "uploadDate" to date
                        )

                        var fileId = ""
                        filesSubCollection.add(file as Map<String,String>)
                            .addOnSuccessListener { documentReference ->
                                fileId = documentReference.id
                                Log.d(
                                    "Images:",
                                    "Image successfully written with ID: ${documentReference.id}"
                                )
                                projectRef.get()
                                    .addOnSuccessListener { document ->
                                        try {
                                            if (document != null) {
                                                var projectInfo = document.toObject(Project::class.java) ?: Project()
                                                Log.d("PROJECT FIlES BEFORE: ",projectInfo.files_attached.toString())
                                                projectInfo.files_attached.add(fileId)
                                                Log.d("PROJECT FILES AFTER: ",projectInfo.files_attached.toString())
                                                Log.d("FILES:",
                                                    "FILES successfully added to the project: ${projectInfo.name}")
                                                projectRef.set(projectInfo)
                                                    .addOnSuccessListener { Log.d("Project :", "Project successfully updated!") }
                                                    .addOnFailureListener { e -> Log.w("Project :", "Error updating the project", e) }

                                            } else {
                                                Log.w("Images:", "Error writing the subcollection image in the project")
                                            }
                                        }catch (ex: Exception){
                                            Log.e(TAG, ex.message)
                                        }
                                    }.addOnFailureListener {
                                            e -> Log.e(TAG, "Error writing document", e)
                                    }

                            }
                            .addOnFailureListener { e -> Log.w("Images:", "Error writing the image", e) }

                        //filesRef.add(file as Map<String,String>)


                    } else {
                        // Handle failures
                        Log.w(TAG, "Error while taking url file")
                    }
                }
            }.start()
        }
    }

    fun downloadFileTest(currentFragment: Int) {
        // get the images from Images collection in the database

        // take the url

        // create the reference with http link
        // download to device
        if(currentFragment == 2){

            val httpsReference = storage.getReferenceFromUrl(
                "https://firebasestorage.googleapis.com/v0/b/mcc-fall-2019-g04.appspot.com/o/files%2Fproject2%2F05eb6e3c-42a2-4413-b5ee-ebb977065908?alt=media&token=58510dd4-9d55-4792-a735-63d1198e6699")

            /* this download the file somewhere
            val localFile = File.createTempFile("myFile", "pdf")

            httpsReference.getFile(localFile).addOnSuccessListener {
                // Local temp file has been created
                println("[ProjectDetailsPresenter] file saved ---------------------- ")
            }.addOnFailureListener {
                // Handle any errors
            }
             */

            val TWENTY_MEGABYTE: Long = 1024 * 1024 * 20
            httpsReference.getBytes(TWENTY_MEGABYTE).addOnSuccessListener {
                // Data is returned, use this as needed ... HOW?
                println("[ProjectDetailsPresenter] file saved 2nd way---------------------- ")
            }.addOnFailureListener {
                // Handle any errors
            }
        }

    }


}