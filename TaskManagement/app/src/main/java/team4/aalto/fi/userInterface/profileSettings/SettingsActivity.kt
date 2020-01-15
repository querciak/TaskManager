package team4.aalto.fi.userInterface.profileSettings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import team4.aalto.fi.data.repository.UserRepository
import team4.aalto.fi.dataInterface.SettingsView
import team4.aalto.fi.domain.usecase.ProfileSettingsUseCase
import team4.aalto.fi.userInterface.projectDetails.ProjectDetailsActivity
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import team4.aalto.fi.R
import java.util.*


class SettingsActivity: AppCompatActivity(), SettingsView, AdapterView.OnItemSelectedListener {

    private lateinit var presenter: SettingsPresenter
    private val IMAGE_PICK_CODE = 1000
    private lateinit var currentUser : String
    private lateinit var url: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        currentUser = intent.getStringExtra("currentUser")
        url = intent.getStringExtra("url")
        presenter = SettingsPresenter(this, ProfileSettingsUseCase(UserRepository()))

        //set current quality
        presenter.showQuality(currentUser)

        //manage quality
        original.setOnClickListener{
            if(original.isChecked){
                high.isChecked = false
                low.isChecked = false
            }
            presenter.setQuality("original", currentUser)
        }
        high.setOnClickListener{
            if(high.isChecked){
                original.isChecked = false
                low.isChecked = false
            }
            presenter.setQuality("high", currentUser)
        }
        low.setOnClickListener{
            if(low.isChecked){
                original.isChecked = false
                high.isChecked = false
            }
            presenter.setQuality("low", currentUser)
        }

        saveButton.setOnClickListener {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken,0)

            presenter.changePass(currentUser,old_pass_edit_settings.text.toString(),new_pass_edit_settings.text.toString(),
                repeat_pass_edit_settings.text.toString())
        }

        //presenter.renderImage(currentUser)
        println("THERE IS NO SHIT THAT CAN STOP ME! >:)   " + url)
        //updateImage(url)
        putImage(url)
        showNormalBoxOldPass()
        showNormalBoxNewPass()
        showNormalBoxRepeatPass()



        profile_image_settings.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type= "image/*"
            ActivityCompat.startActivityForResult ( this, intent,
                ProjectDetailsActivity.IMAGE_PICK_CODE, null)
        }
        //setSpinner()
    }

    override fun qualityyy(q: String) {
        super.qualityyy(q)
        when(q){
            "original" -> {
                original.isChecked = true
                high.isChecked = false
                low.isChecked = false
            }
            "high" -> {
                original.isChecked = false
                high.isChecked = true
                low.isChecked = false
            }
            "low" -> {
                original.isChecked = false
                high.isChecked = false
                low.isChecked = true
            }
        }
    }

    private fun putImage(url: String){
        if(url != ""){

            Glide.with(this)
                .load(url)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_settings)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImage = data?.data.toString()
                    presenter.updateProfilePicture(currentUser, selectedImage)
                }
            }
        }
    }

    /*fun setSpinner(){
        /**this list needs to be retrieved from database
         *
         * call something like listOfMembers = fetchProjectMember()
         *
         * */
        val listOfOptions = arrayOf("Original Quality", "High Quality", "Low Quality")

        //Set setOnItemSelectedListener to the Spinner
        optionSpinner!!.onItemSelectedListener = this

        //Create an ArrayAdapter with the list of items and default layouts
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOfOptions)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Set ArrayAdapter to Spinner
        optionSpinner!!.adapter = arrayAdapter
        optionSpinner.setBackgroundResource(R.drawable.standar_box)

    }

     */

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        presenter.onSelectionSet(view!!.context, parent, view, position, id)
    }

    override fun showNormalBoxOldPass(){
        old_pass_edit_settings.setBackgroundResource(R.drawable.standar_box)
    }

    override fun showNormalBoxNewPass(){
        new_pass_edit_settings.setBackgroundResource(R.drawable.standar_box)
    }

    override fun showNormalBoxRepeatPass(){
        repeat_pass_edit_settings.setBackgroundResource(R.drawable.standar_box)
    }

    override fun showErrorBoxOldPass() {
        super.showErrorBoxOldPass()
        new_pass_edit_settings.setBackgroundResource(R.drawable.error_box)
    }

    override fun showErrorBoxNewPass() {
        super.showErrorBoxOldPass()
        new_pass_edit_settings.setBackgroundResource(R.drawable.error_box)
        old_pass_edit_settings.error = "Old password does not match"
    }

    override fun showErrorBoxRepeatPass() {
        super.showErrorBoxOldPass()
        repeat_pass_edit_settings.setBackgroundResource(R.drawable.error_box)
    }

    override fun changePassSuccessful() {
        this.finish()
    }

    override fun changePassError() {
        super.changePassError()
        val toast = Toast.makeText(this,"Error while updating the password", Toast.LENGTH_LONG)
        toast.show()
    }

    override fun updateImage(url: String) {
        super.updateImage(url)
        runOnUiThread {
            println("AND HERE WE HAVE!!!!!!! ++++++  !!!!!!!  **********    " + url)
            Glide.with(this).load(url).apply(RequestOptions().circleCrop()).into(profile_image_settings)
            //Picasso.get().load(url).into(profile_image_settings)
        }
    }

    override fun showErrorImage() {
        super.showErrorImage()
        val toast = Toast.makeText(this,"Error changing the profile picture", Toast.LENGTH_LONG)
        toast.show()
    }
}