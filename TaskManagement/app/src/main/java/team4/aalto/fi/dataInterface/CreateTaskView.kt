package team4.aalto.fi.dataInterface

import com.google.firebase.ml.vision.text.FirebaseVisionText

interface CreateTaskView {
    fun pickDL(){}
    fun setDescription(t: String){}
    fun showAvailableMembers(){}
}