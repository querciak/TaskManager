package team4.aalto.fi.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User (
    val display_name: String,
    val email: String,
    val profileImageUrl: String,
    val password: String? = ""
)
