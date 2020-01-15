package team4.aalto.fi.dataInterface

interface SelectProjectMemberView {
    fun showToast(s: String){}
    fun showError(s: String){}
    fun updateList(matchingList: List<String>){}
    fun clearRecycler(){}


}