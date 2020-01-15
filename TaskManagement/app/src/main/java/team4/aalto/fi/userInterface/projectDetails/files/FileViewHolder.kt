package team4.aalto.fi.userInterface.projectDetails.files

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.single_file_layout.view.*
import team4.aalto.fi.R
import team4.aalto.fi.domain.model.FilePrint
import team4.aalto.fi.userInterface.projectDetails.ProjectDetailsActivity


class FileViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    var storage = FirebaseStorage.getInstance()

    fun render (file: FilePrint, activity: FragmentActivity){

        if(file.file_size.length > 3) {
            val nSize = (file.file_size.toInt() / 1000).toString()
            val checksize = nSize.split(".")[0]

            val size = if (checksize.length > 3) (checksize.toInt()/1000).toString() + " MB"
            else checksize + " KB"
            itemView.fileSize.text = size
        } else {
            itemView.fileSize.text = file.file_size + " B"
        }

        if(file.file_name.length > 18){
            val longName = file.file_name.substring(0,14) +"..."
            itemView.fileName.text = longName
        } else {
            itemView.fileName.text = file.file_name
        }

        val fType = file.file_type.split("/")[1]
        //function to manage download
        when (fType.toLowerCase()) {
            "pdf" -> {
                itemView.fileIcon.setImageResource(R.drawable.pdf)
            }
            "mp4" -> {
                itemView.fileIcon.setImageResource(R.drawable.mp4)
            }
            "plain" -> {
                itemView.fileIcon.setImageResource(R.drawable.txt)
            }
        }


        itemView.setOnClickListener {

            //function to manage download
            if (ContextCompat.checkSelfPermission(itemView.context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    ProjectDetailsActivity.FILE_DOWNLOAD_CODE
                )

            } else {
                downloadFile(file.file_url, itemView.context)
            }

        }

    }
    // next function
    fun downloadFile(fileUrl: String, context: Context) {
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