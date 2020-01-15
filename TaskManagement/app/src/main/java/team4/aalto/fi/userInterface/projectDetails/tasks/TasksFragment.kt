package team4.aalto.fi.userInterface.projectDetails.tasks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.tasks_layout.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.TaskRepository
import team4.aalto.fi.dataInterface.TaskFragmentView
import team4.aalto.fi.domain.model.Task
import team4.aalto.fi.userInterface.createTask.CreateTaskActivity


class TasksFragment: Fragment(), TaskFragmentView {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"

    lateinit var adapter : TasksAdapter
    private lateinit var projectName: String
    private lateinit var projectId: String
    private lateinit var members: ArrayList<String>
    private lateinit var currentUser: String
    private lateinit var admin: String
    private var isGrous: Boolean = false
    //private lateinit var mylist: ArrayList<Task>

    private lateinit var presenter: TaskPresenter
    private val MY_CODE = 10

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // get project name
        projectName = arguments?.getString("projectName")!!
        // get project id
        projectId = arguments?.getString("projectId")!!
        //getting members
        members = arguments?.getStringArrayList("members") as ArrayList<String>
        //set current user
        currentUser = arguments?.getString("user") as String
        //set admin
        admin = arguments?.getString("admin")!!
        //set isGroup
        isGrous = arguments?.getBoolean("group")!!


        //initialize the recycler
        val myTasks = view?.findViewById(R.id.taskRecyclerView) as RecyclerView
        //tell the recycler view that is going to be linear
        myTasks.layoutManager = LinearLayoutManager(this.context)
        //create the adapter
        adapter = TasksAdapter(projectId)
        //attach the adapter of the recycler view to the adapter just created
        myTasks.adapter = adapter

        //set presenter
        presenter = TaskPresenter(this)

        //clear list
        adapter.clearList()
        //download the tasks
        presenter.getProjectTasks(projectId)

        //create new task
        addTaskToProject.setOnClickListener {
            val intent = Intent(context, CreateTaskActivity::class.java)
            intent.putExtra("name" , projectName)
            intent.putExtra("members", members)
            intent.putExtra("admin", admin)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("group", isGrous)
            intent.putExtra("id", projectId)

            startActivityForResult(intent, MY_CODE)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == MY_CODE){
            //clear list
            adapter.clearList()
            //download the tasks
            presenter.getProjectTasks(projectId)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tasks_layout, container, false)
    }

    override fun showTask(tList: List<Task>) {
        super.showTask(tList)
        activity?.runOnUiThread{
            adapter.fillList(tList)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}