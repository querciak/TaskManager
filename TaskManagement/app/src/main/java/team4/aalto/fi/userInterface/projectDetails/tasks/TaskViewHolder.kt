package team4.aalto.fi.userInterface.projectDetails.tasks

import android.graphics.Paint
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_task_layout.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import team4.aalto.fi.domain.model.Task
import team4.aalto.fi.util.HttpRequest
import team4.aalto.fi.util.Util
import java.io.IOException


class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun render (task: Task, projectId: String){

        //set the name of the task
        itemView.taskDescripion.text = task.description
        //set the checkbox according to task's status

        when(task.current_status){
            "completed" -> {
                itemView.taskName.isChecked = true
                itemView.taskDescripion.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                itemView.taskName.setClickable(false);
            }
            "on-going" -> {
                itemView.taskDescripion.paintFlags = Paint.LINEAR_TEXT_FLAG
                itemView.taskName.isChecked = false
            }
            "pending" -> {
                itemView.taskDescripion.paintFlags = Paint.LINEAR_TEXT_FLAG
                itemView.taskName.isChecked = false
            }
        }

        itemView.setOnClickListener {
            //if pending/on-going set it as completer
            if(task.current_status == "on-going" || task.current_status == "pending"){
                itemView.taskName.isChecked = true
                itemView.taskDescripion.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                val taskRef = FirebaseFirestore.getInstance()
                    .collection("Projects")
                    .document(projectId)
                    .collection("Tasks")
                    .document(task.taskId)

                println("[TaskViewHolder] --------------------- $taskRef")
                /** CALL HERE THE API TO UPDATE TASK */

                apiUpdateTask(projectId, task.taskId)
                itemView.taskName.setClickable(false);

                /*println("OLD TASK!!!!!!!!"+task.events)
                var updatedTask: Task = Task(task.assignedTo,task.creation_date,"completed",task.description,task.deadline_date,events)
                println("NEW TASK!!!!!!!!"+events.toString())*/
            }

        }

        }

    fun apiUpdateTask(projectId:String, taskId: String){
        var updateTaskEndpoint = Util.url_enpoints+"/task/"+taskId

        val projectMap = hashMapOf(
            "projectId" to projectId
        ) as Map<String, List<String>>

        val gson = Gson()
        val outputJson = gson.toJson(projectMap)

        println("*********-- PROYECT AS JSON "+outputJson)

        var client = OkHttpClient()
        var request = HttpRequest(client)

        request.PUT(updateTaskEndpoint, outputJson , object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                val jObject = JSONObject(responseData);

                Log.d("Task Status Updated :", responseData.toString())
            }
            override fun onFailure(call: Call?, e: IOException?) {
                println("Request Failure.")
                Log.d("Error Task Status Updated", e.toString())
            }
        })



       /* val c = Calendar.getInstance()
        val eventDate: String = c.get(Calendar.DAY_OF_MONTH).toString() + "/" + c.get(
            Calendar.MONTH).toString() + "/" + c.get(Calendar.YEAR).toString()
        TaskStatus("completed",eventDate)
        var statusEvent = "{ \"projectId\":" +"\""+pName+"\","
                "}"
        */

    }
}

