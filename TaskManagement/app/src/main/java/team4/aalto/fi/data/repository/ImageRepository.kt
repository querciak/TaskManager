package team4.aalto.fi.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import team4.aalto.fi.domain.model.Image

class ImageRepository {

    val db = FirebaseFirestore.getInstance()

    companion object{

        /** projectFolderName should be called as the name of the project + unique value */
        private val projectFolderName = "testProject"

        private var storage = FirebaseStorage.getInstance()
        private var storageRef = storage.reference
        private val imageRef = storageRef.child("images").child(projectFolderName)

    }

    fun getProjectImages(projectId: String): Task<QuerySnapshot> {
       return  db.collection("Projects").document(projectId).collection("Images").get()
    }
}