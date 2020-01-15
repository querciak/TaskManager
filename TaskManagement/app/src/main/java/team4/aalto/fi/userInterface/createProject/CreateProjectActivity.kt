package team4.aalto.fi.userInterface.createProject

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.CreateProjectView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_createproject.*
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.notifications.NotificationHelper
import team4.aalto.fi.userInterface.home.HomeActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CreateProjectActivity: AppCompatActivity(), CreateProjectView {

    //presenter
    private lateinit var presenter: CreateProjectPresenter
    //projectID
    private lateinit var projectID: String
    //current user
    private lateinit var currentUser: String

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createproject)

        //toolbar buttons
        val closeButton = findViewById<ImageView>(R.id.close)
        val addProjectIcon = findViewById<ImageView>(R.id.project_icon)
        val addKeyword = findViewById<TextView>(R.id.addKeyword)

        //set the current user
        currentUser = intent.getStringExtra("currentUser")


        //init presenter
        presenter = CreateProjectPresenter(this)

        //add project icon
        addProjectIcon.setOnClickListener {
            presenter.setIcon()
        }

        //handle closeButton
        closeButton.setOnClickListener{
            presenter.closeCreateNewProject()
        }

        //handle saveButton
        saveProject.setOnClickListener {
            //hide keyboard
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken,0)
            //TODO check the field and save it or give an
            val c = Calendar.getInstance()
            val time = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val creationDate = time.format(formatter)

                //c.get(Calendar.DAY_OF_MONTH).toString() + "/" + c.get(Calendar.MONTH).toString() + "/" + c.get(Calendar.YEAR).toString()
            //call the function to talk with API to save the

            // save the project members in a variable so it can then be used for sending notifications
            val members : ArrayList<User> = arrayListOf()

            projectID = presenter.saveProject(Project(
                                    project_name.text.toString(),
                                    project_description.text.toString(),
                                    creationDate,
                                    deadline.text.toString(),
                                    last_modification = creationDate,
                                    group_project = groupCheckBox.isChecked,
                                    project_img_url = "",
                                    project_admin = currentUser,
                                    keywords = arrayListOf(key1.text.toString(), key2.text.toString(), key3.text.toString()),
                                    assigned_to =  arrayListOf(),
                                    images_attached = arrayListOf(),
                                    files_attached = arrayListOf(),
                                    tasks = arrayListOf()
                                    ))

            if(projectID != "?-" && projectID != ""){
                println("[CreateProjectActivity] ---------------------- projectID: $projectID")
                //presenter.addProjectReferenceToUser(currentUser, projectID)
            }

            println("------------------------------------------- new project $projectID")

            notificationOnSave(members)

        }

        //pick date for deadline
        deadline.setOnClickListener {
            presenter.setDL()
        }

        //add up to 3 keywords
        addKeyword.setOnClickListener {
            presenter.addKeyword(this, keywords.text.toString())
        }



    }

    fun notificationOnSave(users: List<User>){
        for (user in users){
            Log.d(ContentValues.TAG, "user to notify about project assignment: $user")

            NotificationHelper.sendNotificationToUser(
                user.display_name,
                resources.getString(R.string.fcm_message_title),
                resources.getString(R.string.fcm_message_body_new_project),
                requestQueue
            )
        }
    }

    override fun showNameError(s: String){
        project_name.error = s
    }
    override fun showDescriptionError(s: String){
        project_description.error = s
    }
    override fun closeCreationNewProject(){
        this.finish()
    }
    override fun makeKeyVisible(key: Int, s: String){

    }
    override fun showKeywordError(s: String) {
        keywords.error = s
    }
    override fun showOkMessage(string: String){
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        keywords.text.clear()
    }
    override fun openGallery(intent: Intent){
        ActivityCompat.startActivityForResult ( this, intent, IMAGE_PICK_CODE, null)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.handleGallery(this,requestCode, resultCode, data)

    }
    override fun pickDL(){
        //tutorial https://www.youtube.com/watch?v=LMPmybCTKDA
        //for the moment, you cannot choose the hour of the deadline
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        //val hour = c.get(Calendar.HOUR_OF_DAY)
        //val min = c.get(Calendar.MINUTE)
        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDayOfMonth ->
            var aux: String
            if(mDayOfMonth.toString().length == 1){
                aux = "0$mDayOfMonth/"
            } else {
                aux = "$mDayOfMonth/"
            }
            if (mMonth.toString().length == 1){
                aux = aux + "0${mMonth+1}/$mYear"
            } else {
                aux = aux + "${mMonth+1}/$mYear"
            }


            deadline.text = aux
            /* + " " + hour.toString() + ":" + min.toString() */
        }, year, month, day)
        dpd.show()

    }
    companion object {
        //img pick code
        val IMAGE_PICK_CODE = 1000
    }
}