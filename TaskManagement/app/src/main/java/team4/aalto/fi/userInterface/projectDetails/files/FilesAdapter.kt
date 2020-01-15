package team4.aalto.fi.userInterface.projectDetails.files

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.FilePrint

class FilesAdapter(private val activity: FragmentActivity): RecyclerView.Adapter<FileViewHolder>() {

    /** here we have to retrieve all the files stored in the database */
    private val fileList = ArrayList<FilePrint>()

    fun fillList(list: List<FilePrint>){
        fileList.clear()
        fileList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_file_layout, parent, false)
        return FileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.render(file, activity)
    }

    override fun getItemCount(): Int = fileList.size
}