package team4.aalto.fi.userInterface.createTask

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_createtask.*
import team4.aalto.fi.dataInterface.CreateTaskView
import team4.aalto.fi.domain.model.Task
import team4.aalto.fi.domain.model.TaskStatus
import team4.aalto.fi.domain.model.User
import java.util.*
import team4.aalto.fi.R
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import team4.aalto.fi.notifications.NotificationHelper
import kotlin.collections.ArrayList


class CreateTaskActivity: AppCompatActivity(), CreateTaskView, AdapterView.OnItemSelectedListener {

    lateinit var presenter: CreateTaskPresenter
    var selectedMembers = arrayListOf<String>()
    private lateinit var membersList: ArrayList<String>
    private lateinit var currentUser: String
    private lateinit var admin: String
    private var isGrous: Boolean = false
    private lateinit var projectName: String
    private lateinit var projectId: String

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createtask)

        //set project name from intent
        projectName = intent.getStringExtra("name")
        project_title.text = projectName

        //set projectId
        projectId = intent.getStringExtra("id")

        //initialize presenter
        presenter = CreateTaskPresenter(this)

        //set current user
        currentUser = intent.getStringExtra("currentUser")

        //set admin
        admin = intent.getStringExtra("admin")

        //set isGroup
        isGrous = intent.getBooleanExtra("group", false)

        //pick date for deadline
        deadlineNewTask.setOnClickListener {
            presenter.setDL()
        }

        //get members
        membersList = intent.getStringArrayListExtra("members")

        //convert image to text
        convertImageToText.setOnClickListener {
            //pick image from gallery
            presenter.pickImageFromGallery(this)
            //extract text from image
            //presenter.findText(imageNotVisible, this) <- this function is called in onActivityResult

            //update description with text in the image
        }

        if(isGrous && currentUser==admin){
            assignedToTextView.visibility = View.VISIBLE
            members_view.visibility = View.VISIBLE
            selectMembers.visibility = View.VISIBLE

            selectMembers.setOnClickListener {
                presenter.showAvailableMembers()
            }
        }

        //save new task
        saveTaskButton.setOnClickListener {

            val description: String = task_description.text.toString()
            val c = Calendar.getInstance()
            val creationDate: String = c.get(Calendar.DAY_OF_MONTH).toString() + "/" + c.get(Calendar.MONTH).toString() + "/" + c.get(Calendar.YEAR).toString()
            val deadline: String = deadlineNewTask.text.toString()
            val events: ArrayList<TaskStatus> = arrayListOf()
            val assignedTo: List<String> = selectedMembers
            var status = "pending"
            if (assignedTo.isNotEmpty()){
                status = "on-going"
            }
            events.add(TaskStatus(status,creationDate))

            //selectedMembersUser.add(User(selectedMembers[0],"","matches ", ""))
            Log.d("**Selected Members",selectedMembers.toString())
            Log.d("**creationDate",creationDate)
            Log.d("*status*",status)
            Log.d("*events*",events.toString())
            var task: Task = Task(description = description,creation_date = creationDate,deadline_date = deadline, current_status = status, assignedTo = selectedMembers, events = events)



            var result = presenter.apiCreateTask(projectId,task)

            //if we obtain the taskId of the new task return to Project Details view
            if(result != ""){

            }
            //Otherwise error adding the new task
            else{

            }
            /** Take the data and save the task on firebase (call presenter function to talk with firebase <- to be implemented) */
            notificationOnSave(assignedTo)
            //saving task
            Toast.makeText(this, "Saving new Task", Toast.LENGTH_SHORT).show()

            // close the current activity and go back to the previous one
            Handler().postDelayed(
                {
                    // This method will be executed once the timer is over
                    this.finish()
                },
                1500 // value in milliseconds
            )

        }

    }

    fun notificationOnSave(users: List<String>){
        for (user in users){
            Log.d(ContentValues.TAG, "user to notify about project assignment: $user")

            NotificationHelper.sendNotificationToUser(
                user,
                resources.getString(R.string.fcm_message_title),
                resources.getString(R.string.fcm_message_body_new_task),
                requestQueue
            )
        }
    }

    override fun showAvailableMembers() {
        super.showAvailableMembers()

        // Late initialize an alert dialog object
        lateinit var mydialog: AlertDialog

        // Initialize an array of available members calling the API
        /** real data :D */
        val arrayMembers: Array<String> = membersList.toTypedArray()

        // Initialize a boolean array of checked items (same dimension as arrayMembers)
        val tmp = Array(arrayMembers.size){false}
        val arrayChecked: BooleanArray = tmp.toBooleanArray()

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(this)

        // Set a title for alert dialog
        builder.setTitle("Choose Members")

        // Define multiple choice items for alert dialog
        builder.setMultiChoiceItems(arrayMembers, arrayChecked) { _, which, isChecked ->
            // Update the clicked item checked status
            arrayChecked[which] = isChecked

            // Get the clicked item
            val member = arrayMembers[which]
        }

        // Set the positive/yes button click listener
        builder.setPositiveButton("OK") { _, _ ->
            // reset members.text
            members_view.text = ""

            // list selected members
            for (i in 0 until arrayMembers.size) {
                //val checked = arrayChecked[i]
                if (arrayChecked[i]) {
                    members_view.text = "${members_view.text}  ${arrayMembers[i]} \n"
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
        mydialog = builder.create()

        // display the alert dialog
        mydialog.show()
    }

    /*fun setSpinner(){
        /**this list needs to be retrieved from database
         *
         * call something like listOfMembers = fetchProjectMember()
         *
         * */
        listOfMembers = arrayOf("Myself", "John", "Maria", "Nicole", "molly", "bibo")

        //Set setOnItemSelectedListener to the Spinner
        memberSpinner!!.onItemSelectedListener = this

        //Create an ArrayAdapter with the list of items and default layouts
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOfMembers)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Set ArrayAdapter to Spinner
        memberSpinner!!.adapter = arrayAdapter
    }
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        presenter.findText(imageNotVisible, this, requestCode, resultCode, data)
    }

    override fun setDescription(t: String) {
        super.setDescription(t)
        this.task_description.setText(t)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        /** Here goes the code to set the members according to the user name fetched */
        presenter.onSelectionSet(view!!.context, parent, view, position, id)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun pickDL() {
        super.pickDL()
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

            deadlineNewTask.text = aux
        }, year, month, day)
        dpd.show()
    }

}