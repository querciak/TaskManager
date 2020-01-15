package team4.aalto.fi.dataInterface

import team4.aalto.fi.domain.model.Project

interface HomeView {

    fun showLoading(){}
    fun hideLoading(){}
    fun showProjects(projects: List<Project>){}
    fun clearProjectList(){}
    fun checkCurrentTab(): Int
    fun updateList(projectList: List<Project>){}
    fun logoutCurrentUser(){}
    fun goToSettings(){}
    fun openMemberPickerActivity(proj: Project){}
    fun hereYouHave(url: String){}
    fun goToProjectDetails(proj: Project){}
    fun resetView(){}

}