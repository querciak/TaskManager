package team4.aalto.fi.userInterface.projectDetails.images

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.single_image_layout.view.*

class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun render (image: String){

        Glide.with(itemView).load(image).apply(RequestOptions().centerCrop()).into(itemView.imageView)

        itemView.setOnClickListener {
            downloadImage(image, itemView.context)
            }
        }

    fun downloadImage(fileUrl: String, context: Context) {
            val request = DownloadManager.Request(Uri.parse(fileUrl))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("Download")
            request.setDescription("downloading...")

            //request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}")

            //enqueue
            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager.enqueue(request)

    }
}