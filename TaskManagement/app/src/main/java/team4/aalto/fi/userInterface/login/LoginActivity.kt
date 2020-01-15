package team4.aalto.fi.userInterface.login

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.android.synthetic.main.activity_login.*
import team4.aalto.fi.R
import team4.aalto.fi.data.repository.UserRepository
import team4.aalto.fi.dataInterface.LoginView
import team4.aalto.fi.domain.usecase.LoginUserUseCase
import team4.aalto.fi.userInterface.createProject.CreateProjectActivity
import team4.aalto.fi.userInterface.home.HomeActivity
import team4.aalto.fi.userInterface.projectDetails.ProjectDetailsActivity
import team4.aalto.fi.userInterface.projectDetails.images.ImagesFragment
import team4.aalto.fi.userInterface.register.RegisterActivity
import team4.aalto.fi.userInterface.register.RegisterPresenter


class LoginActivity : AppCompatActivity(), LoginView {

    val IMAGE_CAPTURE_CODE = 1000
    val FILE_DOWNLOAD_CODE = 1001

    private lateinit var mAuth: FirebaseAuth
    private lateinit var presenter: LoginPresenter

    //Defined the required values for the notification channel
    companion object {
        const val CHANNEL_ID = "task_management_main"
        private const val CHANNEL_NAME= "Task Management App"
        private const val CHANNEL_DESC = "Android Push Notifications to users assigned to tasks and projects, using FCM"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        showNormalBoxEmail()
        showNormalBoxPass()

        presenter = LoginPresenter(this, LoginUserUseCase( UserRepository() ) )

        goRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            //presenter.signIn(loginEmail.text.toString(), loginPass.text.toString())

            /** Start login just to try */
            val user = FirebaseAuth.getInstance()

            if(loginEmail.text.toString() != "" && loginPass.text.toString() != "" ){
                user.signInWithEmailAndPassword(loginEmail.text.toString(), loginPass.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            println("[LoginActivity] signInWithEmail: -------------------------------------------------- success")
                            presenter.registerFcmRegistrationToken(loginEmail.text.toString())
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.putExtra("currentUser", FirebaseAuth.getInstance().currentUser?.displayName)
                            startActivity(intent)
                        } else {
                            // If sign in fails, display a message to the user.
                            println( "[LoginActivity] signInWithEmail: -------------------------------------------------- failed - e: " + task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{
                println("[LoginActivity] signInWithEmail: -------------------------------------------------- failed - missing credentials")
                Toast.makeText(baseContext, "Missing credentials.",
                    Toast.LENGTH_SHORT).show()
            }
            /** End login just to try */

            //val intent = Intent(this, HomeActivity::class.java)
            //startActivity(intent)
        }
        askPermission()
    }


    override fun onStart() {
        super.onStart()
        startNotificationChannel()

        presenter.logFcmRegistrationToken()

        Log.d(ContentValues.TAG, "auto filling the fields: " + FirebaseAuth.getInstance().currentUser)
        presenter.checkUser(FirebaseAuth.getInstance().currentUser)
    }


    private fun askPermission(){
        if (ContextCompat.checkSelfPermission(applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA
                ),
                ProjectDetailsActivity.FILE_DOWNLOAD_CODE)

        }
    }


    fun startNotificationChannel() {

        //creating notification channel if android version is greater than or equals to Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESC
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    override fun showNormalBoxEmail() {
        super.showNormalBoxEmail()
        loginEmail.setBackgroundResource(R.drawable.standar_box)
    }
    override fun showNormalBoxPass() {
        super.showNormalBoxPass()
        loginPass.setBackgroundResource(R.drawable.standar_box)
    }

    override fun showErrorEmail(error: String) {
        super.showErrorEmail(error)
        loginError.text = error
        loginError.visibility = View.VISIBLE
        loginEmail.setBackgroundResource(R.drawable.error_box)
    }
    override fun showErrorPass(error: String) {
        super.showErrorPass(error)
        loginError.text = error
        loginError.visibility = View.VISIBLE
        loginPass.setBackgroundResource(R.drawable.error_box)
    }

    override fun showErrorAttemp(error: String) {
        super.showErrorAttemp(error)
        loginError.text = error
    }

    override fun autoFill(email: String) {
        super.autoFill(email)
        loginEmail.setText(email)

    }

    override fun loginSuccessful() {
        super.loginSuccessful()
        println("DONE!")
    }

}
