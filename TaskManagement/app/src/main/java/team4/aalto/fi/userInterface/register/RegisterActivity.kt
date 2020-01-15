package team4.aalto.fi.userInterface.register

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_register.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.UserRepository
import team4.aalto.fi.dataInterface.RegisterView
import team4.aalto.fi.domain.usecase.RegisterUserUseCase

class RegisterActivity: AppCompatActivity(), RegisterView {

    private val GALLERY_REQUEST_CODE = 1889
    private var profilePicUrl = ""
    private lateinit var presenter: RegisterPresenter

    private val database = FirebaseFirestore.getInstance()
    private var userDocumentRef = database.collection("UsersCollection").document("UsersDocument")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        showNormalBoxUsername()
        showNormalBoxEmail()
        showNormalBoxPassword()

        presenter = RegisterPresenter(this, RegisterUserUseCase(UserRepository()))

        regProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }
        regEditImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        regUsername.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) {
                presenter.checkUsername(regUsername.text.toString())
            }
        }

        regEmail.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus) presenter.checkEmail(regEmail.text.toString())
        }

        regButton.setOnClickListener {

            //hide keyboard
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken,0)

            presenter.check(
                this,
                regUsername.text.toString(),
                regEmail.text.toString(),
                profilePicUrl,
                regPass.text.toString(),
                regRepPass.text.toString(),
                "original"
            )


         }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            profilePicUrl = data?.data.toString()
            Glide.with(this).load(profilePicUrl).apply(RequestOptions().circleCrop()).into(regProfilePicture)
            //Picasso.get().load(profilePicUrl).apply(RequestOptions.circleCropTransform()).into(regProfilePicture)
            presenter.uploadImage(profilePicUrl)
        }
    }


    override fun showNormalBoxUsername(){
        regUsername.setBackgroundResource(R.drawable.standar_box)
        regErrorUsername.visibility = View.INVISIBLE
    }
    override fun showNormalBoxEmail(){
        regEmail.setBackgroundResource(R.drawable.standar_box)
        regErrorEmail.visibility = View.INVISIBLE
    }
    override fun showNormalBoxPassword(){
        regPass.setBackgroundResource(R.drawable.standar_box)
        regRepPass.setBackgroundResource(R.drawable.standar_box)
        regErrorLength.visibility = View.INVISIBLE
        regErrorSame.visibility = View.INVISIBLE
    }

    override fun showErrorUsernameUnique(username: String) {
        regUsername.setBackgroundResource(R.drawable.error_box)
        val suggestion1 = username + (0..99).random().toString()
        val suggestion2 = username + (0..99).random().toString()
        regErrorUsername.text = "'$username' is not available, try $suggestion1 or $suggestion2"
        regErrorUsername.visibility = View.VISIBLE
    }
    override fun showErrorUsername() {
        super.showErrorUsername()
        regUsername.setBackgroundResource(R.drawable.error_box)
        regErrorUsername.visibility = View.VISIBLE
    }
    override fun showErrorEmail() {
        super.showErrorUsername()
        regEmail.setBackgroundResource(R.drawable.error_box)
        regErrorEmail.visibility = View.VISIBLE
    }
    override fun showErrorPassword() {
        super.showErrorUsername()
        regPass.setBackgroundResource(R.drawable.error_box)
        regRepPass.setBackgroundResource(R.drawable.error_box)
    }
    override fun showErrorLength() {
        super.showErrorLength()
        regErrorLength.visibility = View.VISIBLE
    }
    override fun showErrorSame() {
        super.showErrorSame()
        regErrorSame.visibility = View.VISIBLE
    }

    override fun registrationSuccessful() {
        runOnUiThread {
            this.finish()
        }
    }

}