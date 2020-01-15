package team4.aalto.fi.dataInterface

interface SettingsView {
    fun showNormalBoxOldPass(){}
    fun showNormalBoxNewPass(){}
    fun showNormalBoxRepeatPass(){}
    fun showErrorBoxOldPass(){}
    fun showErrorBoxNewPass(){}
    fun showErrorBoxRepeatPass(){}
    fun changePassSuccessful(){}
    fun changePassError(){}
    fun showErrorLength(){}
    fun showErrorSame(){}
    fun updateImage(url: String){}
    fun showErrorImage(){}
    fun qualityyy(q: String){}
}