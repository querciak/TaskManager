package team4.aalto.fi.userInterface.projectDetails.files

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import team4.aalto.fi.dataInterface.FileFragmentView
import team4.aalto.fi.domain.model.FilePrint
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.usecase.ProjectFilesUseCase
import java.text.DateFormat
import java.util.*

class FilesPresenter(private val view: FileFragmentView, private val fileUseCase: ProjectFilesUseCase) {

    /** Every mention to image here, refers to file, but copy-paste */
    private var storage = FirebaseStorage.getInstance()
    private var storageRef = storage.reference
    private lateinit var imageRef: StorageReference

    private val database = FirebaseFirestore.getInstance()
    private var projectsRef = database.collection("Projects")

    private val listOfProjectFiles = mutableListOf<FilePrint>()



    fun getFilesFromProject(projectId: String){
        imageRef = storageRef.child("files").child(projectId)
        Thread {
            val task = fileUseCase.getAllFiles(projectId)
            task.addOnSuccessListener { result ->

                for (document in result) {
                    val fileName = document.data["link"] as String
                    val fileRef = imageRef.child(fileName)

                    fileRef.metadata.addOnSuccessListener {
                        val fileObj = FilePrint(it.name!!,it.sizeBytes.toString(),it.contentType.toString(),it.getCustomMetadata("url")!!)
                        listOfProjectFiles.add(fileObj)
                        view.showFiles(listOfProjectFiles)
                    }

                }
            }
        }.start()
    }



    fun saveFile(selectedFile: Uri, it: Context, projectId: String, filename: String){

        val progressDialog = ProgressDialog(it)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()
        imageRef = storageRef.child("files").child(projectId)


        Thread{
            val imageNameRef = imageRef.child(filename)

            val uploadTask = imageNameRef
                .putFile(selectedFile)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Log.d(ContentValues.TAG, "File uploaded")
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Log.w(ContentValues.TAG, "Error while uploading file")
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
                    val imagesSubCollection = projectRef.collection("Files")
                    var imageId = ""
                    val file = hashMapOf(
                        "link" to filename,
                        "uploadDate" to date
                    )

                    val customMetadata = StorageMetadata.Builder()
                        .setCustomMetadata("url", downloadUri)
                        .build()
                    imageNameRef.updateMetadata(customMetadata)
                    imageNameRef.metadata.addOnSuccessListener {meta ->
                        val fileObj = FilePrint(filename, meta.sizeBytes.toString(),meta.contentType!!, downloadUri)
                        listOfProjectFiles.add(fileObj)
                        view.showFiles(listOfProjectFiles)
                    }

                    imagesSubCollection.add(file as Map<String,String>)
                        .addOnSuccessListener { documentReference ->

                            imageId = documentReference.id
                            Log.d(
                                "Files:",
                                "File successfully written with ID: ${documentReference.id}"
                            )
                            projectRef.get()
                                .addOnSuccessListener { document ->

                                    try {
                                        if (document != null) {
                                            var projectInfo = document.toObject(Project::class.java) ?: Project()
                                            Log.d("PROJECT FILES BEFORE: ",projectInfo.images_attached.toString())
                                            projectInfo.images_attached.add(imageId)
                                            Log.d("PROJECT FILES AFTER: ",projectInfo.images_attached.toString())
                                            Log.d("Files:",
                                                "File successfully added to the project: ${projectInfo.name}")
                                            projectRef.set(projectInfo)
                                                .addOnSuccessListener { Log.d("Project :", "Project successfully updated!") }
                                                .addOnFailureListener { e -> Log.w("Project :", "Error updating the project", e) }

                                        } else {
                                            Log.w("Project:", "Error writing the subcollection file in the project")
                                        }
                                    }catch (ex: Exception){
                                        Log.e(ContentValues.TAG, ex.message)
                                    }
                                }.addOnFailureListener {
                                        e -> Log.e(ContentValues.TAG, "Error writing document", e)
                                }

                        }
                        .addOnFailureListener { e -> Log.w("Files:", "Error writing the file", e) }



                } else {
                    // Handle failures
                    Log.w(ContentValues.TAG, "Error while taking url file")
                }
            }
        }.start()
    }

}

