package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.UserRepository

class ProfileSettingsUseCase(private val userRepository: UserRepository) {

    fun checkPassword(user: String, password_old: String) = userRepository.checkPassword(user, password_old)
    fun changePassword(user: String, password_new: String) = userRepository.changePassword(user, password_new)
    fun updatePassword(password_new: String) = userRepository.updatePassword(password_new)
    fun updateProfilePicture(user: String, image: String) = userRepository.updateProfilePicture(user,image)
    fun getImage(user: String) = userRepository.getUserImage(user)
}