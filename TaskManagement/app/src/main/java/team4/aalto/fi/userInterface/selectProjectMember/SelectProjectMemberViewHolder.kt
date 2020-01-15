package team4.aalto.fi.userInterface.selectProjectMember

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import kotlinx.android.synthetic.main.single_mamber_layout.view.*
import team4.aalto.fi.R

class SelectProjectMemberViewHolder(itemView: View, private val presenter: SelectProjectMemberPresenter): RecyclerView.ViewHolder(itemView)  {

    fun render(user: String, currentUser: String, assignedTo: ArrayList<String>, requestQueue: RequestQueue) {
        itemView.member_name.text = user
        if(user == currentUser || checkMember(user, assignedTo)) {
            println("ME COLEEEEEEEEE -----------")
            itemView.member_name.isChecked = true
            itemView.textView2.hint = "Member"
        } else {
            itemView.setOnClickListener {
                if(!itemView.member_name.isChecked) {
                    itemView.member_name.isChecked = true
                    presenter.forcemember(itemView.member_name.text.toString())
                    presenter.sendNotificationToNewMember(itemView.member_name.text.toString(), requestQueue)
                }
            }
        }

    }

    private fun checkMember(user: String, list: ArrayList<String>): Boolean{
        if (list.isEmpty()) return false
        else{
            for (member in list){
                println("------------------------------------------------    $user    VS    $member")
                if (user.equals(member,true)) return true
            }
            return false
        }
    }

}