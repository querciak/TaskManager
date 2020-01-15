package team4.aalto.fi.domain.model

data class Task (val assignedTo: ArrayList<String>,
                 val creation_date: String,
                 val current_status:  String,
                 val deadline_date: String,
                 val description:String,
                 val events:  ArrayList<TaskStatus> = arrayListOf(),
                 val taskId: String = "")