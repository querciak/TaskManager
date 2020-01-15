package team4.aalto.fi.userInterface.projectDetails

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_projectdetails.*
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.ProjectDetailView
import team4.aalto.fi.userInterface.createTask.CreateTaskActivity

class ProjectDetailsActivity: AppCompatActivity(), ProjectDetailView {
    private lateinit var presenter: ProjectDetailsPresenter
    private var selectedFile: Uri? = null
    private var selectedImage: Uri? = null
    private lateinit var projectName: String
    private lateinit var projectId: String
    private lateinit var members: ArrayList<String>
    private lateinit var currentUser: String
    private lateinit var admin: String
    private var isGrous: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_projectdetails)

        //set presenter
        presenter = ProjectDetailsPresenter(this)

        // set correct project name
        projectName = intent.getStringExtra("name")
        project_titleX.text = projectName

        //getting the projectId
        projectId = intent.getStringExtra("id")

        //getting members
        members = intent.getStringArrayListExtra("members")

        //set current user
        currentUser = intent.getStringExtra("currentUser")

        //set admin
        admin = intent.getStringExtra("admin")

        //set isGroup
        isGrous = intent.getBooleanExtra("group", false)

        //set up the tabs
        val sectionsPagerAdapter = SectionsPagerAdapter(
            this,
            supportFragmentManager,
            currentUser,
            projectId,
            members,
            admin,
            isGrous,
            projectName)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        // set up floatingButton to add task, image or file to project details
        /*addImageToProject.setOnClickListener {
            val f = viewPager.currentItem
            presenter.decideNextActivity(f, this, projectName)
        }*/


    }

    override fun downloadFile(fileUrl: String) {
        super.downloadFile(fileUrl)
        val request = DownloadManager.Request(Uri.parse(fileUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Download")
        request.setDescription("downloading...")

        //request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}")

        //enqueue
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    override fun openAddNewTask() {
        super.openAddNewTask()
        //makeText(this, "Create new task", LENGTH_SHORT).show()
        val intent = Intent(this, CreateTaskActivity::class.java)
        intent.putExtra("name" , projectName)
        intent.putExtra("members", members)
        intent.putExtra("admin", admin)
        intent.putExtra("currentUser", currentUser)
        intent.putExtra("group", isGrous)
        intent.putExtra("id", projectId)

        startActivity(intent)
    }

    /*override fun openCamera(){
        super.openCamera()
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), IMAGE_CAPTURE_CODE)

        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                IMAGE_CAPTURE_CODE
            )
        }

        else {
            // Permission has already been granted
            image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
        }
    }

     */

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            IMAGE_CAPTURE_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
                } else {
                    return
                }
                return
            }
            FILE_DOWNLOAD_CODE ->{
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    view_pager.currentItem = 2
                } else {
                    return
                }
            }
            else -> {
                return
            }
        }
    }


    override fun openGallery() {
        super.openGallery()
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type= "image/*"
        ActivityCompat.startActivityForResult ( this, intent, IMAGE_PICK_CODE, null)
    }

    override fun openAddNewFile() {
        super.openAddNewFile()
        val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICK_CODE)
    }



    companion object{
        //img pick code
        val IMAGE_PICK_CODE = 1000
        //permission code
        val PERMISSION_CODE = 1001
        //img from camera pick code
        val IMAGE_CAPTURE_CODE = 1002
        //filemanager code
        val FILE_PICK_CODE = 1003
        //
        val FILE_DOWNLOAD_CODE = 1004

        var image_uri : Uri? = null
    }
}