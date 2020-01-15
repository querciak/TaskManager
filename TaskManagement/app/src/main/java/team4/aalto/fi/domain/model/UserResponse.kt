package team4.aalto.fi.domain.model

data class UserResponse (val user: User, val error: Boolean = false, val msg: String)