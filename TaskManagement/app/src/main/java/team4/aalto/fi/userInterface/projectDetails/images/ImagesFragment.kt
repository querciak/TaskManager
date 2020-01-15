package team4.aalto.fi.userInterface.projectDetails.images

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.images_layout.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.ImageRepository
import team4.aalto.fi.dataInterface.ImageFragmentView
import team4.aalto.fi.domain.usecase.ProjectImagesUseCase


class ImagesFragment: Fragment(), ImageFragmentView{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"

    lateinit var adapter: ImagesAdapter

    private var selectedImage: Uri? = null
    private lateinit var presenter: ImagesPresenter
    private lateinit var projectId: String
    private lateinit var currentUser: String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        projectId = arguments?.getString("projectId")!!
        currentUser = arguments?.getString("user") as String
        presenter = ImagesPresenter(this, ProjectImagesUseCase(ImageRepository()))

        //initialize the recycler
        val myImages = view?.findViewById(R.id.image_recycler_view) as RecyclerView
        //tell the recycler view that is going to be linear
        myImages.layoutManager = GridLayoutManager(this.context, 3)
        //create the adapter
        adapter = ImagesAdapter()
        //attach the adapter of the recycler view to the adapter just created
        myImages.adapter = adapter

        //get all the images from the cloud
        presenter.getImagesFromProject(projectId)

        addImageToProject.setOnClickListener {
            //execute(projectId)
            openDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.images_layout, container, false)
    }

    override fun showImages(imageList: List<String>) {
        super.showImages(imageList)
        activity?.runOnUiThread{
            adapter.fillList(imageList)
        }
    }

    fun openDialog() {

        val options : Array<String?>? = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Select Options")
        builder.setItems(options) { dialog, which  ->
            when(which){
                0 -> {
                    openCamera()
                }
                1 -> {
                    openGallery()
                }
                2 -> dialog.cancel()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type= "image/*"

        this.startActivityForResult ( intent, IMAGE_PICK_CODE, null)
    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(context!!,
                Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                IMAGE_CAPTURE_CODE
            )
        }

        else {
            // Permission has already been granted
            image_uri = context!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
        }
    }

    //save image/file and call appropriate function to save it
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            IMAGE_PICK_CODE ->{
                // if everything ok, save the uri
                if(resultCode == Activity.RESULT_OK && data?.getData()!=null) selectedImage = data.data
                // if uri is not null
                if(selectedImage != null) presenter.saveNewImage(selectedImage!!, context!!, projectId, currentUser, context!!)
            }
            IMAGE_CAPTURE_CODE -> {
                if(resultCode == Activity.RESULT_OK && data?.getData()!=null) selectedImage = data.data
                if(selectedImage != null) presenter.saveNewImage(selectedImage!!, context!!, projectId, currentUser, context!!)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImagesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

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