package team4.aalto.fi.userInterface.register

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.view.*
import team4.aalto.fi.dataInterface.RegisterView
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.domain.usecase.RegisterUserUseCase
import team4.aalto.fi.userInterface.login.LoginPresenter


class RegisterPresenter (private val view: RegisterView, private val registerUserUseCase: RegisterUserUseCase){

    private val KEY_USERNAME = "username"
    private val KEY_PICTURE_URL = "pictureURL"
    private val KEY_EMAIL = "email"
    private val KEY_PASSWORD = "password"

    private val database = FirebaseFirestore.getInstance()
    private var userCollectionRef = database.collection("Users")

    private val existingEmails = arrayOf("abcd@abcd.ab","prueba1@test.com")



    internal fun checkUsername(username: String){
        /** Check if username exists in the real list of users
         * HOW to fetch data from firestore : https://www.youtube.com/watch?v=Cbb1wWg2Zn4
         * */
        val userDocumentRef = userCollectionRef.document(username)
        Thread{
            userDocumentRef.get()
                .addOnFailureListener { e ->
                    Log.w(TAG, "[UserRepository] Error: ", e)
                }
                .addOnSuccessListener { documentSnapshot ->
                    if(documentSnapshot.exists()){

                        view.showErrorUsernameUnique(username)
                        Log.d(TAG, "[UserRepository] Document exist!---------------------------------------- " + documentSnapshot.get("username"))

                    }else{
                        view.showNormalBoxUsername()
                        Log.d(TAG, "[UserRepository] Document does not exist!")
                    }
                }
        }.start()

    }

    internal fun checkEmail(email: String){
        /** Check if email is already used */
        if(email in existingEmails)
            view.showErrorEmail()
        else
            view.showNormalBoxEmail()
    }

    private fun checkPassword(password: String, rep_password: String): Boolean{
        return when {
            password.length < 6 -> {
                view.showErrorPassword()
                view.showErrorLength()
                false
            }
            password != rep_password -> {
                view.showNormalBoxPassword()
                view.showErrorPassword()
                view.showErrorSame()
                false
            }
            else -> {
                view.showNormalBoxPassword()
                true
            }
        }
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

    interface MyCallback {
        fun onCallback(fcmToken: String)
    }

    internal fun check(context: Context, username: String, email: String, picture: String, password: String, rep_password: String, quality: String) {

        if (!checkPassword(password, rep_password)) {
            val toast = Toast.makeText(context, "[UserRepository] Error during registration", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)
            toast.show()
        } else {
            val response = registerUserUseCase.registerUser(User(username,email,picture), password)
            response.addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    Thread{
                        val user = FirebaseAuth.getInstance().currentUser

                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    view.registrationSuccessful()
                                    Log.d(TAG, "[UserRepository] User profile updated. New displayName: " + user.displayName)
                                }
                            }
                        //FirebaseAuth.getInstance().currentUser.updateProfile({})
                        /** Here we are going to save the user successfully registered [in Background] */

                        registerUserUseCase.addRegisteredUserToDatabase(username, picture, email, password, quality)

                        getFcmRegistrationToken(object: MyCallback {
                            override fun onCallback(fcmToken: String) {
                                registerUserUseCase.registerToken(email, username, fcmToken)
                            }
                        })

                    }.start()
                } else {
                    val toast = Toast.makeText(context, task.exception?.message, Toast.LENGTH_SHORT)
                    toast.setGravity(Gravity.TOP, 0, 0)
                    toast.show()
                }
            }
        }
    }

    fun uploadImage(profilePicUrl: String) {

    }


}
