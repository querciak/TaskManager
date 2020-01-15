package team4.aalto.fi.userInterface.projectDetails.tasks

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.single_task_layout.*
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.Task

class TasksAdapter(private val projectName: String): RecyclerView.Adapter<TaskViewHolder>() {

    private val taskList = ArrayList<Task>()

    fun fillList(list: List<Task>){
        taskList.clear()
        taskList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        //inflate the item (always the same)
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_task_layout, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.render(task, projectName)
    }

    override fun getItemCount(): Int = taskList.size
    fun clearList() {
        taskList.clear()
        notifyDataSetChanged()
    }

}