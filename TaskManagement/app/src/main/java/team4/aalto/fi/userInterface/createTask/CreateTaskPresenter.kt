package team4.aalto.fi.userInterface.createTask

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import team4.aalto.fi.R
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.core.app.ActivityCompat
import com.android.volley.RequestQueue
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_createtask.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import team4.aalto.fi.dataInterface.CreateTaskView
import team4.aalto.fi.domain.model.Task
import team4.aalto.fi.util.HttpRequest
import team4.aalto.fi.util.Util
import java.io.IOException

class CreateTaskPresenter(private val view: CreateTaskView) {
    private var client = OkHttpClient()
    private var request = HttpRequest(client)

    fun setDL() {
        view.pickDL()
    }

    fun onSelectionSet(it: Context, parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(parent!!.getItemAtPosition(position).toString() == "Myself"){
            //do nothing
            makeText(it, "personal task", Toast.LENGTH_SHORT).show()
        }else{
            //set user as member
        }
    }

    fun pickImageFromGallery(it: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        ActivityCompat.startActivityForResult(it, intent, IMAGE_PICK_CODE, null)
    }

    fun findText(image: ImageView, itContext: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            IMAGE_PICK_CODE -> {
                if( resultCode == Activity.RESULT_OK ) {
                    image.setImageURI(data?.data)

                    val drawable = image.drawable as? BitmapDrawable
                    val bitmap = drawable!!.bitmap

                    val imageLoaded = FirebaseVisionImage.fromBitmap(bitmap)
                    val detector = FirebaseVision.getInstance()
                        .onDeviceTextRecognizer

                    val result = detector.processImage(imageLoaded)
                        .addOnSuccessListener { firebaseVisionText ->
                            // Task completed successfully
                            makeText(itContext, "Text found", Toast.LENGTH_SHORT).show()
                            view.setDescription(firebaseVisionText.text)
                        }
                        .addOnFailureListener {
                            // Task failed with an exception
                            makeText(itContext, "Text not found", Toast.LENGTH_SHORT).show()
                        }

                }
            }
            else -> {
                makeText(itContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun showAvailableMembers() {
        view.showAvailableMembers()
    }

    fun apiCreateTask(projectId: String, task: Task): String{
        var createTaskEndpoint = Util.url_enpoints+"/task/"+projectId
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonTask: String = gson.toJson(task)
        var taskId = ""
        Log.d("Task JSON", jsonTask)
        request.POST(createTaskEndpoint, jsonTask , object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                val jObject = JSONObject(responseData);
                taskId = jObject.getString("success")
                Log.d("Request Successful :", taskId)
            }
            override fun onFailure(call: Call?, e: IOException?) {
                println("Request Failure.")
                Log.d("Error", e.toString())
            }
        })
        return taskId
    }

    companion object{
        //img pick code
        val IMAGE_PICK_CODE = 1000

        var image_uri : Uri? = null
    }

}