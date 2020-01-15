package team4.aalto.fi.dataInterface

import team4.aalto.fi.domain.model.Task

interface TaskFragmentView {
    fun showTask(list: List<Task>){}
}