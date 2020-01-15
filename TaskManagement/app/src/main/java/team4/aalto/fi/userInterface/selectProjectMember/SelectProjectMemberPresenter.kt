package team4.aalto.fi.userInterface.selectProjectMember

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.SelectProjectMemberView
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.notifications.NotificationHelper

class SelectProjectMemberPresenter(private val view: SelectProjectMemberView, val projectId: String){

    //firebase instance
    val db = FirebaseFirestore.getInstance()
    // users ref
    val usersRef = db.collection("Users")

    private var usersList= mutableListOf<String>()
    private var matchingList= mutableListOf<String>()

    fun researchMembers(name: String) {
        view.clearRecycler()
        if(name.length < 2){
            view.showError("Name is too short")
        }else{
            matchingList.clear()
            view.clearRecycler()


                //retrieve all users
                usersRef.get()
                    .addOnSuccessListener { result ->
                        Thread {
                            for (user in result) {
                                val user = user.data["username"] as String
                                if(user.contains(name, true)){
                                    addMemberToList(user)
                                    view.updateList(matchingList)
                                }

                            }
                        }.start()
                    }
                    .addOnFailureListener { e ->
                        println("[SelectProjectMemberPresenter] Error: '$e' ")
                    }
        }


    }

    private fun returnMatchingUsers(name: String) {
        for(user in usersList){
            if(name in user){
                matchingList.add(user)

            }
        }

        println("[SelectMemberPresenter] -----B-A-N-A-N-A------------- MAtching Users: ${matchingList} ")
    }

    private fun addMemberToList(it: String) {
        matchingList.add(it)
        //println("[SelectMemberPresenter] userList: ${usersList}")

    }

    fun forcemember(user: String) {
        val db = FirebaseFirestore.getInstance()
        val projectRef = db.collection("Projects").document(projectId)
        val userRef = db.collection("Users").document(user).collection("Projects")

        projectRef.update("assigned_to", FieldValue.arrayUnion(user)).addOnSuccessListener {
            val reference = "Projects/$projectId"
            val map = hashMapOf(
                "reference" to reference
            )
            userRef.add(map).addOnSuccessListener {
                view.showToast("$user added to project $projectId")
            }
        }

    }

    fun sendNotificationToNewMember(user: String, requestQueue: RequestQueue){

        val fcm_message_title = "Task Manager Notification"
        val fcm_message_body_new_project= "You have been assigned to a new project"

        NotificationHelper.sendNotificationToUser(
            user,
            fcm_message_title,
            fcm_message_body_new_project,
            requestQueue
        )
    }

}