package team4.aalto.fi.userInterface.projectDetails.tasks

import com.google.firebase.firestore.FirebaseFirestore
import team4.aalto.fi.dataInterface.TaskFragmentView
import team4.aalto.fi.domain.model.Task

class TaskPresenter(private val view: TaskFragmentView) {

    private val db = FirebaseFirestore.getInstance()
    //list of tasks
    private var taskssList = arrayListOf<Task>()

    fun getProjectTasks(projectId: String){
        //clear list before everything
        taskssList.clear()

        val taskRef = db.collection("Projects").document(projectId).collection("Tasks")
        taskRef.get()
            .addOnSuccessListener {result ->
                for(it in result){

                    println("[]------------------ TASK ID: ${it.id}")
                    val task_ = Task(
                        assignedTo = it.data["assignedTo"] as ArrayList<String>,
                        creation_date = it.data["creation_date"] as String,
                        current_status = it.data["current_status"] as String,
                        deadline_date = it.data["deadline_date"] as String,
                        description = it.data["description"] as String,
                        taskId = it.id
                        )

                    taskssList.add(task_)

                    view.showTask(taskssList)
                }

            }
    }

}