package team4.aalto.fi.data.repository

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.domain.model.UserResponse
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*


class UserRepository {

    private val KEY_USERNAME = "username"
    private val KEY_PICTURE_URL = "pictureURL"
    private val KEY_EMAIL = "email"
    private val KEY_PASSWORD = "password"
    private val KEY_TOKEN_LIST = "tokens"

    private val mAuth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private var userDocumentRef = database.collection("UsersCollection").document("UsersDocument")
    private var userTokens = database.collection("UserTokens")

    //storage ref
    val storageRef = FirebaseStorage.getInstance().reference


    fun registerUser(user: User, password:String): Task<AuthResult>{
        return mAuth.createUserWithEmailAndPassword(user.email, password)
    }

    // this is used on every login to add tokens from a possibly new device
    // note: the update function doesn't add duplicates
    fun addToken(email: String, fcmToken: String) {
        userTokens.document(email)

            //.set(tokenMap, /**MERGE*/SetOptions.merge())
            .update("tokens", FieldValue.arrayUnion(fcmToken))

            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: $email")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    // this is used on registration to add the initial token
    fun registerToken(email: String, username: String, fcmToken: String) {
        val tokenList = listOf(fcmToken) as List<String>

        val tokenMap = hashMapOf(
            KEY_TOKEN_LIST to tokenList,
            KEY_USERNAME to username
        ) as Map<String, List<String>>

        userTokens.document(email)
            .set(tokenMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: $email")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun loginUser(email: String, password: String): UserResponse{
        lateinit var response: UserResponse

        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<AuthResult> ->

                response = if (task.isSuccessful){
                    /** !!! CAUTION !!! change "" on display name when firebase is ready to get the username */
                    UserResponse(user = User(password = "",email= email, profileImageUrl = "", display_name = ""),
                        msg = "Login successful")
                } else {
                    UserResponse(user = User(password = "",email= email, profileImageUrl = "", display_name = ""),
                        msg = task.exception.toString().split(":")[1])

                }
            }
        return response
    }

    fun checkPassword(user: String, password_old: String): Task<DocumentSnapshot>{
        val userRef = database.collection("Users").document(user)
        return userRef.get()
    }

    fun changePassword(user: String, password_new: String): Task<Void>{
        val userRef = database.collection("Users").document(user)
        return userRef.update("password", password_new)
    }
    fun updatePassword(password_new: String){
        mAuth.currentUser?.updatePassword(password_new)
    }

    fun getUserImage(user: String): Task<DocumentSnapshot>{
        return database.collection("Users").document(user).get()
    }

    fun updateProfilePicture(user: String, image: String){

        val imageRef = storageRef.child("ProfileImage").child(UUID.randomUUID().toString())
        val tasK = imageRef.putFile(Uri.parse(image))
        tasK.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if(task.isSuccessful){
                val downloadUri = task.result.toString()

                val userRef = FirebaseFirestore.getInstance().collection("Users").document(user)

                userRef.update("pictureURL", downloadUri)
            }
        }

        /***/
    }

    fun addUserToDatabase(username: String, pictureURL: String, email: String, password: String, q: String) {
        if(pictureURL != "") {
            val imageRef = storageRef.child("ProfileImage").child(UUID.randomUUID().toString())
            val tasK = imageRef.putFile(Uri.parse(pictureURL))
            tasK.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()

                    val user = hashMapOf(
                        "username" to username,
                        "pictureURL" to downloadUri,
                        "email" to email,
                        "password" to password,
                        "quality" to q
                    )

                    database.collection("/Users").document(username)
                        .set(user as Map<String, Any>, SetOptions.merge())
                        .addOnSuccessListener { documentReference ->
                            Log.d(TAG, "DocumentSnapshot added with ID: $username")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }
            }
        }else{
            val user = hashMapOf(
                "username" to username,
                "pictureURL" to "",
                "email" to email,
                "password" to password,
                "quality" to q
            )

            database.collection("/Users").document(username)
                .set(user as Map<String, Any>, SetOptions.merge())
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: $username")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }
    }

}
