package team4.aalto.fi.userInterface.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.Project
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UserProjectsAdapter(private val presenter: HomePresenter, private val currentUser: String): RecyclerView.Adapter<UserProjectViewHolder>() {

    private val projectList = ArrayList<Project>()

    fun fillList(pList: List<Project>){
        clearList()
        projectList.addAll(pList)
        notifyDataSetChanged()
    }

    fun clearList(){
        projectList.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProjectViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.project_list_row, parent, false)
        return UserProjectViewHolder(itemView,presenter)
    }

    override fun getItemCount(): Int = projectList.size

    override fun onBindViewHolder(holder: UserProjectViewHolder, position: Int) {
        val project = projectList[position]
        holder.render(project, currentUser)
    }


}