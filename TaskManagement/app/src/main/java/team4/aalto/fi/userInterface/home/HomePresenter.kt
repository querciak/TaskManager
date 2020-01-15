package team4.aalto.fi.userInterface.home

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.GsonBuilder
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.HomeView
import team4.aalto.fi.dataInterface.ItemProjectViewHolder
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.domain.usecase.UserProjectsUseCase
import team4.aalto.fi.userInterface.login.LoginActivity
import team4.aalto.fi.userInterface.profileSettings.SettingsActivity
import team4.aalto.fi.util.HttpRequest
import team4.aalto.fi.util.Util
import java.io.IOException
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Handler

class HomePresenter(private val view: HomeView, private val userProjectsUseCase: UserProjectsUseCase) {

    private var client = OkHttpClient()
    private var request = HttpRequest(client)

    val dateTimeStrToLocalDateTime: (String) -> LocalDate = {
        //LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm"))
        LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }
    val dateTimeStrToLocalDateTimeDeadline: (String) -> LocalDate = {
        //LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm"))
        LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
    val dateTimeStrToLocalDateTimeSort: (String) -> LocalDateTime = {
        //LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm"))
        LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }
    val dateTimeStrToLocalDateTimeDeadLineSort: (String) -> LocalDate = {
        //LocalDateTime.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm"))
        LocalDate.parse(it, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    //list of members
    private var selectedMembers = arrayListOf<String>()

    //firebase instance
    val db = FirebaseFirestore.getInstance()
    var currentuser = ""
    private var tabtab = 1

    private var projectList = mutableListOf<Project>()


    fun clearList(){
        projectList = mutableListOf()
    }

    fun getUserProjects(currentUser: String, favorite: Boolean = false, deadline: Boolean = false){
        view.showLoading()

        Thread {
        val myresult = userProjectsUseCase.getAllUserProjects(currentUser)

        myresult.addOnSuccessListener { result ->

                    for(document in result){
                            // print test
                            //Log.d(ContentValues.TAG, "[ProjectRepository] ---------------------- ${document.id} => ${document.data}")

                            // take document.data and re-create a project
                            val realProjectRef = db.document(document.data["reference"] as String)
                            // get the project data
                            realProjectRef.get()
                                .addOnSuccessListener { subresult ->
                                    // print test
                                    //Log.d(ContentValues.TAG, "[ProjectRepository] Project: ${subresult.id} :: ${subresult.data}")
                                    //re-create project
                                    val project = subresult.toObject(Project::class.java)
                                    project?.id = subresult.id
                                    // call function to add the project to the global list
                                    if (favorite){
                                        if (project!!.isFavorite){
                                            addProjectToList(project)
                                        }
                                    } else if(deadline) {
                                        if(compareDate(project)){
                                            addProjectToList(project)
                                        }
                                    }
                                    else {
                                        addProjectToList(project)
                                        //println("[HomePresenter] size of projectList (inside on success): ${projectList.size}")
                                    }

                                }
                                .addOnFailureListener { exception ->
                                    Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                                }

                    }
                    //println("[HomePresenter] size of projectList: ${projectList.size}")
                    //view.showProjects(projectList)

                }


            }.start()

        //val sortedList = listOfUserProjects.sortedByDescending{ it.last_modification }

        //view.showProjects(sortedList)
        view.hideLoading()

    }

    private fun addProjectToList(project: Project?) {
        if (project != null && tabtab == 1) {
            projectList.add(project)
            val sortedList= projectList.sortedByDescending{ dateTimeStrToLocalDateTimeSort(it.last_modification)}
            view.showProjects(sortedList)
        }
        else if (project != null && tabtab == 0) {
            projectList.add(project)
            val sortedList= projectList.sortedByDescending{ it.name }.reversed()
            view.showProjects(sortedList)
        }
        else if (project != null && tabtab == 2) {
            projectList.add(project)
            val sortedList= projectList.sortedByDescending{ dateTimeStrToLocalDateTimeDeadLineSort(it.deadline_date)}.reversed()
            view.showProjects(sortedList)
        }
    }


    fun manageList(tab: MenuItem, user: String): Boolean{
        currentuser = user
        view.showLoading()
        clearList()
        return when(tab.itemId){
            R.id.nav_favorites -> {
                tabtab = 0
                Thread{
                    //projectList
                    getUserProjects(user, true)
                    val sortedList = projectList.sortedWith(compareBy { it.name })
                    view.showProjects(sortedList)
                    view.hideLoading()
                    projectList.addAll(sortedList)
                }.start()
                true
            }
            R.id.nav_dashboard -> {
                tabtab = 1
                Thread{
                    getUserProjects(user)
                    val sortedList = projectList.sortedByDescending{ dateTimeStrToLocalDateTime(it.last_modification) }
                    view.showProjects(sortedList)
                    view.hideLoading()
                }.start()
                true
            }
            R.id.nav_recent ->{
                tabtab = 2
                Thread{
                    getUserProjects(user, deadline = true)
                    val sortedList = (projectList.sortedByDescending { it.deadline_date }).reversed()
                    view.showProjects(sortedList)
                    view.hideLoading()
                }.start()
                true
            }
            else -> false
        }
    }

    fun setMenuOptions(itemView: ItemProjectViewHolder, project: Project, menuItem: MenuItem, context: Context): Boolean {
        when (menuItem.itemId) {
            R.id.remove_project -> {
                println("REMOVE PROJECT FROM FIREBASE")
                apiDeleteProject(project.id, project.project_admin)
                return true
            }
            R.id.show_project_content -> {
                println("show_project_content PROJECT FROM FIREBASE")
                view.goToProjectDetails(project)
                return true
            }
            R.id.add_members -> {
                //open a new activity to show the results
                println("add_members PROJECT FROM FIREBASE")
                view.openMemberPickerActivity(project)

                //open alert dialog to show the available members
                //showAvailableMembers(project, context)

                return true
            }
            R.id.generate_project_report -> {
                println("SORRY :)")
                return true
            }
            else ->
                return false
        }
    }

    private fun showAvailableMembers(project: Project, context: Context) {
        // Late initialize an alert dialog object
        lateinit var dialog: AlertDialog

        // Initialize an array of available members calling the API
        /** dummy data */
        val arrayMembers = arrayOf("Tom","Agus","Yess","Nick","Andrea","Mario", "bibo")

        // Initialize a boolean array of checked items (same dimension as arrayMembers)
        val tmp = Array(arrayMembers.size){false}
        val arrayChecked: BooleanArray = tmp.toBooleanArray()

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(context)

        // Set a title for alert dialog
        builder.setTitle("Choose Members")

        // Define multiple choice items for alert dialog
        builder.setMultiChoiceItems(arrayMembers, arrayChecked) { dialog, which, isChecked ->
            // Update the clicked item checked status
            arrayChecked[which] = isChecked

            // Get the clicked item
            val member = arrayMembers[which]
        }

        // Set the positive/yes button click listener
        builder.setPositiveButton("OK") { _, _ ->
            // list selected members
            for (i in 0 until arrayMembers.size) {
                //val checked = arrayChecked[i]
                if (arrayChecked[i]) {
                    if (!selectedMembers.contains(arrayMembers[i]))
                    {
                        selectedMembers.add(arrayMembers[i])
                    }

                }
            }
        }

        builder.setNeutralButton("Cancel") { dialog, which ->
            // Do something when click the neutral button

        }

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // display the alert dialog
        dialog.show()
    }

    fun homeMenuOptions(menuItem: MenuItem): Boolean{
        when(menuItem.itemId){
            R.id.profile_settings -> {
                println("Edit profile settings")
                view.goToSettings()
            }
            R.id.logout -> {
                println("Logout...")
                view.logoutCurrentUser()
            }
            else -> {
                println("ELSE")
                return false
            }
        }
        return true
    }


    fun changeProjectFavorite(project: Project, itemView: ItemProjectViewHolder, position: Int){
        Thread {
            //reference to the current project
            val projRef = db.collection("Projects").document(project.id)
            if (project.isFavorite) {
                project.isFavorite = false
                itemView.removeAsFavorite()
                projRef.set(project)
                    .addOnSuccessListener {
                        if (tabtab == 0){
                            clearList()
                            getUserProjects(currentuser,favorite = true)
                        }
                    }

            } else {
                project.isFavorite = true
                itemView.markAsFavorite()
                projRef.set(project)
                    .addOnSuccessListener {
                    }

            }
            val currentTab = view.checkCurrentTab()
            if(currentTab == R.id.nav_favorites){
                projectList.removeAt(position)
                view.updateList(projectList as List<Project>)
            }

        }.start()
    }

    fun gimmeTheImage(usr: String){

        db.collection("Users").document(usr).get()
            .addOnSuccessListener {
            if(it.data != null){
                val docs = it.data!!["pictureURL"].toString()
                view.hereYouHave(docs)
            }else{
                view.hereYouHave("")
            }
        }
    }

    private fun compareDate(project: Project?): Boolean{
        val date = project?.deadline_date

        return if (date == "" || date == null) /*return thia value*/ false
        else{
            val time = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val now = dateTimeStrToLocalDateTimeDeadline(time.format(formatter))

            val dif = ChronoUnit.DAYS.between(now, dateTimeStrToLocalDateTimeDeadline(date!!))
            /*return this result*/
            dateTimeStrToLocalDateTimeDeadline(date!!) > now && dif < 8
        }

    }

    fun filterProjects(text: String) {
        val matchingProjects = mutableListOf<Project>()

        for(project in projectList){
            if(project.name.equals(text,true)) matchingProjects.add(project)
            else{
                if(project.keywords.isNotEmpty()){
                    for(word in project.keywords){
                        if(text.equals(word, true)) {
                            matchingProjects.add(project)
                            break
                        }
                    }
                }
            }
        }
        clearList()
        projectList.addAll(matchingProjects)
        view.showProjects(projectList)
    }


    fun test(){
        println("TESTING ")
        var tmp = ""
        db.collection("Users/"+"Agus"+"/Projects").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    var doc_reference = document.data["reference"]
                    if (doc_reference == "Projects/"+"11rwyQHMmUUwxlRfgwYe"){
                        tmp = document.id
                        Log.d("Document found TO DELETE ",tmp)
                        break
                    }
                    // Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("NOT FOUND ",tmp)
            }
        println("TEST FINISHED")
    }
    fun apiDeleteProject(projectId: String, userId: String){
        var deleteProjectEndpoint:String = Util.url_enpoints+"/project/"+projectId
        Log.d("PROJECT TO DELETE ",projectId)
        Thread {
            request.DELETE(deleteProjectEndpoint, projectId, object : Callback {

                override fun onResponse(call: Call?, response: Response) {
                    Log.i("Success", "Success: " + response.code());
                        val responseData = response.body()?.string()
                    if (response.code() == 200) {
                        // remove project reference from user document
                        db.collection("Users/"+userId+"/Projects").get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    var doc_reference = document.data["reference"]
                                    if (doc_reference == "Projects/"+projectId){
                                        db.collection("Users").document(userId).collection("/Projects").document(document.id)
                                            .delete()
                                            .addOnCompleteListener {
                                                view.resetView()
                                            }
                                            .addOnSuccessListener { Log.d("User Reference:", "User reference to project"+projectId+" successfully deleted!") }
                                            .addOnFailureListener { e -> Log.w("User Reference:", "Error deleting User reference to project"+projectId, e) }
                                        break
                                    }
                                   // Log.d(TAG, "${document.id} => ${document.data}")
                                }
                            }
                            .addOnFailureListener { exception ->

                            }
                    }
                        Log.d("Request Successful", responseData)
                }
                override fun onFailure(call: Call?, e: IOException?) {
                    println("Request Failure.")
                    Log.d("Error", e.toString())
                    //countDownLatch.countDown();
                }
            })
        }.start()
    }

}