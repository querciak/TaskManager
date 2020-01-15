package team4.aalto.fi.dataInterface

import android.view.View

interface ProjectDetailView {
    fun openAddNewTask(){}
    fun openAddNewFile(){}
    fun openCamera(){}
    fun openGallery(){}
    fun downloadFile(fileUrl: String){}
}