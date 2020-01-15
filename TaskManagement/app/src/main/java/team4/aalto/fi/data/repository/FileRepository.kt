package team4.aalto.fi.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage


class FileRepository {

    val db = FirebaseFirestore.getInstance()
    val storageRef = FirebaseStorage.getInstance().reference

    // Get reference to the file
    val forestRef = storageRef.child("images/forest.jpg")

    fun getAllFiles(projectId: String): Task<QuerySnapshot>{
        return  db.collection("Projects").document(projectId).collection("Files").get()
    }

}