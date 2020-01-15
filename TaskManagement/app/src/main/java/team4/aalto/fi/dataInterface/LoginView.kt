package team4.aalto.fi.dataInterface

interface LoginView {

    fun showNormalBoxEmail(){}
    fun showNormalBoxPass(){}
    fun showErrorEmail(error: String){}
    fun showErrorPass(error: String){}
    fun showErrorAttemp(error: String){}
    fun autoFill(email: String){}
    fun loginSuccessful(){}

}