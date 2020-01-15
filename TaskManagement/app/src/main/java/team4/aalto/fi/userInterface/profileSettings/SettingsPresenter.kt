package team4.aalto.fi.userInterface.profileSettings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings.*
import team4.aalto.fi.R
import team4.aalto.fi.dataInterface.SettingsView
import team4.aalto.fi.domain.model.User
import team4.aalto.fi.domain.usecase.ProfileSettingsUseCase

class SettingsPresenter(private val view: SettingsView, private val profileSettingsUseCase: ProfileSettingsUseCase){


    private fun checkPasswords(password_new: String, rep_password: String): Boolean{
        return when {
            password_new.length < 6 -> {
                view.showErrorBoxNewPass()
                view.showErrorLength()
                false
            }
            password_new != rep_password -> {
                view.showNormalBoxNewPass()
                view.showErrorBoxRepeatPass()
                view.showErrorSame()
                false
            }
            else -> {
                view.showNormalBoxNewPass()
                view.showNormalBoxRepeatPass()
                true
            }
        }
    }


    internal fun changePass(user: String, password_old:String, password_new: String, rep_password: String){
        if (checkPasswords(password_new,rep_password)){
           val checkPassTask = profileSettingsUseCase.checkPassword(user,password_old)

            checkPassTask .addOnCompleteListener {
               if (it.isSuccessful) {
                   val userPass = it.result?.data?.get("password")
                   if (userPass == password_old) {
                       profileSettingsUseCase.changePassword(user,password_new).addOnCompleteListener { result ->
                           if (result.isSuccessful){
                               profileSettingsUseCase.updatePassword(password_new)
                               view.changePassSuccessful()
                           }
                           else{
                               view.changePassError()
                           }
                       }
                   }
               } else{
                   view.showErrorBoxOldPass()
               }
            }
        }
    }

    internal fun renderImage(user: String){
        val task = profileSettingsUseCase.getImage(user)
        task.addOnSuccessListener {

            val docs = it.data!!["pictureURL"].toString()

            println("--------------------------------------------WTF IS HERE MAN!!!!!!! ?  " + docs)
            view.updateImage(docs)

        }

    }


    internal fun updateProfilePicture(user: String, img: String){
        profileSettingsUseCase.updateProfilePicture(user, img)
        view.updateImage(img)
    }


    fun onSelectionSet(context: Context?, parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        when(position){
            0 -> {

            }
            1 -> {

            }
            2 -> {

            }
        }
    }

    val db = FirebaseFirestore.getInstance()

    fun showQuality(cUser: String) {
        val userRef = db.collection("Users").document(cUser)
        userRef.get()
            .addOnSuccessListener { task ->
                val q = task.data!!["quality"] as String
                view.qualityyy(q)
            }
    }

    fun setQuality(q: String, cUser: String) {
        val userRef = db.collection("Users").document(cUser)
        userRef.update("quality", q)
    }

}