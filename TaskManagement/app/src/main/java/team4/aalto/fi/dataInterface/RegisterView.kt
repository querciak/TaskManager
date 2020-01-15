package team4.aalto.fi.dataInterface


interface RegisterView {

    fun showNormalBoxUsername(){}
    fun showNormalBoxEmail(){}
    fun showNormalBoxPassword(){}
    fun showErrorUsername(){}
    fun showErrorEmail(){}
    fun showErrorPassword(){}
    fun showErrorLength(){}
    fun showErrorSame(){}
    fun registrationSuccessful()
    fun showErrorUsernameUnique(username: String)

}