package team4.aalto.fi.userInterface.login

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import team4.aalto.fi.dataInterface.LoginView
import team4.aalto.fi.domain.usecase.LoginUserUseCase
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import team4.aalto.fi.R
import kotlin.random.Random

class LoginPresenter (private val view: LoginView, private val loginUserUseCase: LoginUserUseCase) {

    internal fun logFcmRegistrationToken() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    //displaying the error if the task is unsuccessful
                    view.showErrorPass("error")

                    //stopping the further execution
                    return@OnCompleteListener
                }

                //Getting the token if everything is fine
                val token=  task.result?.token

                // should save the token somewhere
                Log.d(TAG, "Messaging token: $token")
            })

    }

    interface MyCallback {
        fun onCallback(fcmToken: String)
    }


    fun getFcmRegistrationToken(myCallback : MyCallback) {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->

                if (!task.isSuccessful) {
                    //displaying the error if the task is unsuccessful
                    Log.d(TAG, "Unable to retrieve notification token")

                    //stopping the further execution
                    return@OnCompleteListener
                }

                //Getting the token if everything is fine
                val token=  task.result!!.token

                // should save the token somewhere
                Log.d(TAG, "Obtained token: $token")

                myCallback.onCallback(token)
            })

    }


    fun registerFcmRegistrationToken(email: String) {
        getFcmRegistrationToken(object: MyCallback {
            override fun onCallback(fcmToken: String) {
                loginUserUseCase.addToken(email, fcmToken)
            }
        })
    }


    internal fun checkUser(user: FirebaseUser?){
        if(user!=null && user.email!=null)
            view.autoFill(user.email.toString())
    }

    internal fun signIn(email: String, password: String){
        val response = loginUserUseCase.loginUser(email, password)

        if(response.error){
            view.loginSuccessful()
        }
        else{
            var msg = response.msg
            when {
                response.msg.contains("password") -> {
                    msg = msg.split(" or ")[0]
                    view.showErrorPass(msg)
                    view.showNormalBoxEmail()
                }
                msg.contains("[") -> {
                    msg = msg.split("[")[0]
                    view.showErrorAttemp(msg)
                }
                else -> {
                    if (msg.contains("user")) {
                        msg = msg.split(".")[0]
                    }
                    view.showErrorEmail(msg)
                }
            }
        }







        /**
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->

                if (task.isSuccessful){
                    //intent to app
                    view.showNormalBoxEmail()
                    view.showNormalBoxPass()
                }

                else {
                    var msg = task.exception.toString().split(":")[1]
                    when {
                        msg.contains("password") -> {
                            msg = msg.split(" or ")[0]
                            view.showErrorPass(msg)
                            view.showNormalBoxEmail()
                        }
                        msg.contains("[") -> {
                            msg = msg.split("[")[0]
                            view.showErrorAttemp(msg)
                        }
                        else -> {
                            if (msg.contains("user")) {
                                msg = msg.split(".")[0]
                            }
                            view.showErrorEmail(msg)
                        }
                    }



                }
            }*/
    }
}
