package team4.aalto.fi.userInterface.home

import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.project_list_row.view.*
import org.jetbrains.anko.runOnUiThread
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.ItemProjectViewHolder
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.userInterface.projectDetails.ProjectDetailsActivity

class UserProjectViewHolder(itemView: View, private val presenter: HomePresenter): RecyclerView.ViewHolder(itemView), ItemProjectViewHolder  {

    private val db = FirebaseFirestore.getInstance()

    fun render(project: Project, currentUser: String){

        renderProjectIcon(project.project_img_url)
        renderTitle(project.name)
        renderLastModificationDate(project.last_modification)
        renderFavoriteIcon(project.isFavorite)
        renderMediaIcon(project.containsMedia)
        renderMembersPicture(project.assigned_to)
        preparePopupMenu(project, currentUser)
        prepareListeners(itemView.projectPicture, itemView.projectTitle, itemView.textView3, itemView.lastModification,project, currentUser)

        itemView.isProjectFavorite.setOnClickListener {
            presenter.changeProjectFavorite(project, this, adapterPosition)
        }
    }


    override fun markAsFavorite() {
        itemView.context.runOnUiThread {
            super.markAsFavorite()

            itemView.isProjectFavorite.setColorFilter(itemView.context.resources.getColor(R.color.blue))
        }
    }

    override fun removeAsFavorite() {
        itemView.context.runOnUiThread {
            super.removeAsFavorite()
            itemView.isProjectFavorite.setColorFilter(itemView.context.resources.getColor(R.color.grey))
        }
    }


    private fun renderProjectIcon(url: String){
        if (url == ""){
            Picasso.get().load(R.drawable.folder).resize(80,80).centerCrop().into(itemView.projectPicture)
        } else {
            Picasso.get().load(url).resize(80,80).centerCrop().into(itemView.projectPicture)
        }
    }
    private fun renderTitle(title: String){
        itemView.projectTitle.text = title
    }
    private fun renderLastModificationDate(last_modification: String){
        itemView.lastModification.text = last_modification
    }
    private fun renderFavoriteIcon(isFavorite: Boolean){
        if(isFavorite) markAsFavorite()
        else removeAsFavorite()
    }
    private fun renderMediaIcon(containsMedia: Boolean){
        if(containsMedia) itemView.imgHasFiles.visibility = View.VISIBLE
        else itemView.imgHasFiles.visibility = View.INVISIBLE
    }


    private fun setFirstImage(url: String){
        itemView.context.runOnUiThread {
        if (url==""){
            itemView.memberPicture1.visibility = View.VISIBLE
        } else {
            Glide.with(itemView).load(url).apply(RequestOptions().circleCrop()).into(itemView.memberPicture1)
            //Picasso.get().load(url).resize(30,30).centerCrop().into(itemView.memberPicture1)

        }}
    }
    private fun setSecondImage(url: String){
       itemView.context.runOnUiThread {
           if (url==""){
               itemView.memberPicture2.visibility = View.VISIBLE
           } else {
               Picasso.get().load(url).resize(30,30).centerCrop().into(itemView.memberPicture2)
           }
       }
    }
    private fun setThrirdImage(url: String){
        itemView.context.runOnUiThread {
        if (url==""){
            itemView.memberPicture3.visibility = View.VISIBLE
        } else {
            Picasso.get().load(url).resize(30,30).centerCrop().into(itemView.memberPicture3)
        }
        }
    }

    private fun renderMembersPicture(members: List<String>){
        if(members.isNotEmpty()){
            /** For 1 member in the project */
            val userRef = db.collection("Users").document(members[0])
            userRef.get()
                .addOnSuccessListener { doc ->
                    Thread{
                        val url = doc.data!!["pictureURL"].toString()
                        setFirstImage(url)
                    }.start()
                }
            /*
            if (url==""){
                itemView.memberPicture1.visibility = View.VISIBLE
            } else {
                Picasso.get().load(members[0].profileImageUrl).resize(30,30).centerCrop().into(itemView.memberPicture1)
            }
             */

            /** For 2 members in the project */
            if (members.size >= 2){
                val userRef = db.collection("Users").document(members[1])
                userRef.get()
                    .addOnSuccessListener { doc ->
                        Thread{
                            val url = doc.data!!["pictureURL"].toString()
                            setSecondImage(url)
                        }.start()
                    }
            } else {
                itemView.memberPicture2.visibility = View.INVISIBLE
                itemView.memberPicture3.visibility = View.INVISIBLE
            }

            /** For 3 members in the project */
            if (members.size >=3){
                val userRef = db.collection("Users").document(members[2])
                userRef.get()
                    .addOnSuccessListener { doc ->
                        Thread{
                            val url = doc.data!!["pictureURL"].toString()
                            setThrirdImage(url)
                        }.start()
                    }
            } else {
                itemView.memberPicture3.visibility = View.INVISIBLE
            }

        } else {
            itemView.memberPicture1.visibility = View.INVISIBLE
            itemView.memberPicture2.visibility = View.INVISIBLE
            itemView.memberPicture3.visibility = View.INVISIBLE
        }
    }
    private fun preparePopupMenu(project: Project, _currentUser: String){
        itemView.projectMenuOptions.setOnClickListener {
            val popup = PopupMenu(itemView.context,itemView.projectMenuOptions)
            popup.inflate(R.menu.project_options_menu)
            if (!project.group_project){
                popup.menu.removeItem(R.id.add_members)
                popup.menu.removeItem(R.id.generate_project_report)
            }

            if(project.project_admin != _currentUser){
                popup.menu.removeItem(R.id.add_members)
            }

            popup.setOnMenuItemClickListener {
                presenter.setMenuOptions(this,project,it, itemView.context)
            }
            popup.show()
        }
    }
    private fun prepareListeners(img: View, title: TextView, fixedText: View, lmodif: View, project: Project, currentUser: String){
        img.setOnClickListener {
            val intent = Intent(itemView.context,ProjectDetailsActivity::class.java)
            println(project.id + "---------------------------------------------------------------------------------")
            intent.putExtra("name",project.name)
            intent.putExtra("id", project.id)
            intent.putExtra("members", project.assigned_to)
            intent.putExtra("admin", project.project_admin)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("group", project.group_project)

            itemView.context.startActivity(intent)
        }
        title.setOnClickListener {
            val intent = Intent(itemView.context,ProjectDetailsActivity::class.java)
            intent.putExtra("name",project.name)
            intent.putExtra("id", project.id)
            intent.putExtra("admin", project.project_admin)
            intent.putExtra("members", project.assigned_to)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("group", project.group_project)

            itemView.context.startActivity(intent)
        }
        fixedText.setOnClickListener {
            val intent = Intent(itemView.context,ProjectDetailsActivity::class.java)
            intent.putExtra("name",project.name)
            intent.putExtra("id", project.id)
            intent.putExtra("members", project.assigned_to)
            intent.putExtra("admin", project.project_admin)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("group", project.group_project)

            itemView.context.startActivity(intent)
        }
        lmodif.setOnClickListener {
            val intent = Intent(itemView.context,ProjectDetailsActivity::class.java)
            intent.putExtra("name",project.name)
            intent.putExtra("id", project.id)
            intent.putExtra("members", project.assigned_to)
            intent.putExtra("admin", project.project_admin)
            intent.putExtra("currentUser", currentUser)
            intent.putExtra("group", project.group_project)

            itemView.context.startActivity(intent)
        }
    }

}