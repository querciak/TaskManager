package team4.aalto.fi.userInterface.projectDetails.images

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.Image

class ImagesAdapter: RecyclerView.Adapter<ImageViewHolder>() {

    private val imageList = ArrayList<String>()

    fun fillList(list: List<String>){
        imageList.clear()
        imageList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        //inflate the item (always the same)
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_image_layout, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = imageList[position]
        holder.render(image)
    }

    override fun getItemCount(): Int = imageList.size
}