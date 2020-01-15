package team4.aalto.fi.dataInterface

import team4.aalto.fi.domain.model.FilePrint

interface FileFragmentView {

    fun showFiles(fileList: List<FilePrint>){}

}