package team4.aalto.fi.dataInterface

import android.content.Intent

interface CreateProjectView {
    fun showOkMessage(string: String){}
    fun showKeywordError(s: String){}
    fun makeKeyVisible(key: Int, s: String){}
    fun openGallery(intent: Intent){}
    fun closeCreationNewProject(){}
    fun pickDL(){}
    fun showNameError(s: String){}
    fun showDescriptionError(s: String){}

}