package team4.aalto.fi.userInterface.selectProjectMember

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import kotlinx.android.synthetic.main.single_mamber_layout.view.*
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.User

class SelectProjectMemberAdapter(private val presenter: SelectProjectMemberPresenter, private val currentUser: String, private val assignedTo: ArrayList<String>, requestQueue: RequestQueue): RecyclerView.Adapter<SelectProjectMemberViewHolder>() {

    private val userList = ArrayList<String>()

    private val requestQueuePrivate = requestQueue

    fun fillList(pList: List<String>){
        userList.clear()
        userList.addAll(pList)
        notifyDataSetChanged()
    }

    fun clearScreen(){
        userList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectProjectMemberViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_mamber_layout, parent, false)
        return SelectProjectMemberViewHolder(itemView, presenter)
    }


    override fun onBindViewHolder(holder: SelectProjectMemberViewHolder, position: Int) {
        val user = userList[position]
        holder.render(user, currentUser, assignedTo, requestQueuePrivate)

    }

    override fun getItemCount(): Int = userList.size
}