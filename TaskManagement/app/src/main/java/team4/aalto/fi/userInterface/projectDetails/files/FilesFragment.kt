package team4.aalto.fi.userInterface.projectDetails.files

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.files_layout.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.FileRepository
import team4.aalto.fi.dataInterface.FileFragmentView
import team4.aalto.fi.domain.model.FilePrint
import team4.aalto.fi.domain.usecase.ProjectFilesUseCase


class FilesFragment: Fragment(), FileFragmentView {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"

    private lateinit var adapter : FilesAdapter
    private var selectedFile: Uri? = null
    private lateinit var presenter: FilesPresenter
    private lateinit var projectId: String
    private lateinit var currentUser: String
    private lateinit var add_file_name: String


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        projectId = arguments?.getString("projectId")!!
        currentUser = arguments?.getString("user") as String
        presenter = FilesPresenter(this, ProjectFilesUseCase(FileRepository()))

        //initialize the recycler
        val myFiles = view?.findViewById(R.id.fileRecyclerView) as RecyclerView
        //tell the recycler view that is going to be linear
        myFiles.layoutManager = LinearLayoutManager(this.context)
        //create the adapter
        adapter = FilesAdapter(activity!!)
        //attach the adapter of the recycler view to the adapter just created
        myFiles.adapter = adapter

        presenter.getFilesFromProject(projectId)

        addFileToProject.setOnClickListener {
            openAddNewFile()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.files_layout, container, false)
    }

    private fun openAddNewFile() {
        val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
        this.startActivityForResult(Intent.createChooser(intent, "Select a file"), FILE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == FILE_PICK_CODE){
            if(resultCode == Activity.RESULT_OK && data?.getData()!=null) selectedFile = data.data
            if(selectedFile != null){
                val extensionArray = selectedFile.toString().split(".")
                val extension = extensionArray[extensionArray.size-1]
                if(!extension.equals("pdf", true) && !extension.equals("txt", true) &&
                    !extension.equals("mp4", true)){
                    val alert = AlertDialog.Builder(context)
                        .setTitle("Extension not allowed")
                        .setMessage("Extension allowed: pdf, mp4, txt")
                        .setPositiveButton("Ok"){ dialog, _ ->
                            dialog.dismiss()
                        }
                    alert.show()
                } else {
                    val input = EditText(context)
                    input.hint = "Name"
                    val alert = AlertDialog.Builder(context)
                        .setTitle("Name the file")
                        .setView(input)
                        .setPositiveButton("Ok"){ dialog, _ ->
                            add_file_name = input.text.toString()
                            dialog.dismiss()
                            presenter.saveFile(selectedFile!!, context!!, projectId,add_file_name)
                        }
                        .setNegativeButton("Cancel"){dialog, _ ->
                            dialog.cancel()
                        }
                    alert.show()

                }
            }

        }

    }
    
    override fun showFiles(fileList: List<FilePrint>) {
        super.showFiles(fileList)
        adapter.fillList(fileList)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FilesFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FilesFragment().apply {
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