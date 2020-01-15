package team4.aalto.fi.notifications

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService
import team4.aalto.fi.userInterface.login.LoginActivity
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val KEY_USERNAME = "username"
    private val KEY_FCM_TOKEN = "fcmToken"

    private val database = FirebaseFirestore.getInstance()
    private var userTokens = database.collection("UserTokens")

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, token)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun getCurrentUser() : String {

        val currUser = FirebaseAuth.getInstance().currentUser?.displayName

        return currUser ?: "guest"
    }

    private fun sendRegistrationToServer(fcmToken: String?) {

        val username = getCurrentUser()

        val tokenData = hashMapOf(
            KEY_USERNAME to username,
            KEY_FCM_TOKEN to fcmToken
        ) as Map<String,String>

        userTokens.document(username)
            .set(tokenData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: $username")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    fun displayNotificationMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body

        NotificationHelper.displayNotification(
            applicationContext,
            title!!,
            body!!
        )
    }

    fun displayDataMessage(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data!!["title"]
        val body = remoteMessage.data!!["body"]

        NotificationHelper.displayNotification(
            applicationContext,
            title!!,
            body!!
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.w(TAG, "Message received")

        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            displayNotificationMessage(remoteMessage)
        } else {
            displayDataMessage(remoteMessage)
        }
    }

}