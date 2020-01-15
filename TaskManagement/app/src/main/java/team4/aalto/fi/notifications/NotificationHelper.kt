package team4.aalto.fi.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.Volley.*
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONException
import org.json.JSONObject
import team4.aalto.fi.R
import team4.aalto.fi.userInterface.login.LoginActivity
import com.google.firebase.firestore.QuerySnapshot
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.*
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import team4.aalto.fi.userInterface.register.RegisterPresenter


object NotificationHelper {

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val contentType = "application/json"
    private val serverKey =
        "key=" + "AAAAD5fib5Y:APA91bHlW-CIB7hkbmvSDJU7fa0vhGXoDsrLWCApWVfDGWduQBsLLSpn-sKFGLidn-5rEZMq47raG8gZd2PqU0JG4oJjwYfWXrI52w5fqFYBOJGLs1FwD9ihChFo9oMpgltX-k8U_2JV"

    val database = FirebaseFirestore.getInstance()

    fun getNotificationId(): Int{
        return 100 + (0..9000).random()
    }

    fun displayNotification(context: Context, title: String, body: String) {

        val intent = Intent(context, LoginActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val mBuilder = NotificationCompat.Builder(context,
            LoginActivity.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.salmon_image)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val mNotificationMgr = NotificationManagerCompat.from(context)
        // if notification id is always the same, then we will only see the last notification received
        mNotificationMgr.notify(getNotificationId(), mBuilder.build())
    }


    fun createNotificationJSON(to : String, title : String, message : String) : JSONObject{

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", title)
            notificationBody.put("body", message)
            notification.put("to", to)
            notification.put("data", notificationBody) // send data instead of notification so it gets handled even in the background
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        Log.d("json_request", notification.toString())

        return notification
    }


    fun sendNotification(notification: JSONObject, requestQueue: RequestQueue) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }

        requestQueue.add(jsonObjectRequest)
    }


    fun sendNotificationToUser(user: String, title: String, msg: String, requestQueue: RequestQueue) {

        getTokensFromUser(user, object: MyCallback {
            override fun onCallback(tokenList: List<String>) {

                for (fcmToken in tokenList){
                    val notification = createNotificationJSON(fcmToken, title, msg)
                    sendNotification(notification, requestQueue)
                }

            }
        })

    }


    interface MyCallback {
        fun onCallback(tokenList: List<String>)
    }

    fun getTokensFromUser(user: String, myCallback : MyCallback) {

        // https://android.jlelse.eu/getting-started-with-cloud-firestore-for-android-783e2910cfbd
        database.collection("UserTokens")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        if (document.data["username"] == user) {
                            Log.d(TAG, document.id + " => " + document.data)
                            myCallback.onCallback(document.data["tokens"] as List<String>)
                        }
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.exception)
                }
            }

    }
}