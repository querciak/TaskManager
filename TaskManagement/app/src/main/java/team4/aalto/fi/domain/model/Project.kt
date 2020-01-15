package team4.aalto.fi.domain.model

data class Project (val name: String = "",
                    var description:String = "",
                    val creation_date: String = "",
                    val deadline_date: String = "",
                    var last_modification: String = "",
                    val group_project: Boolean = false,
                    var isFavorite: Boolean = false,
                    var containsMedia: Boolean = false,
                    var project_img_url: String = "",
                    val project_admin: String = "",
                    val keywords: ArrayList<String> =  arrayListOf(),
                    var assigned_to: ArrayList<String> =  arrayListOf(),
                    var images_attached: ArrayList<String> =  arrayListOf(),
                    var files_attached: ArrayList<String> =  arrayListOf(),
                    val tasks: ArrayList<String> =  arrayListOf(),
                    var id: String = "")