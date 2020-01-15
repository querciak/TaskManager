package team4.aalto.fi.data.repository


import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ProjectRepository {

    // db reference
    val db = FirebaseFirestore.getInstance()

    fun getAllUserProjects(currentUser: String): Task<QuerySnapshot>{
        // reference to user's projects
        //println("[ProjectRepository] currentUser: --------------------------- $currentUser")
        val projectsRef = db.collection("Users").document(currentUser).collection("Projects")

        // get all the projects in the collection Projects
        return projectsRef.get()


        /** return List<Project> */
        //return listOfProjects
    }




}