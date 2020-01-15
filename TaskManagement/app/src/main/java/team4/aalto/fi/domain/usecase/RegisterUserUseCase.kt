package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.UserRepository
import team4.aalto.fi.domain.model.User


class RegisterUserUseCase(private val userRepository: UserRepository) {

    fun registerUser(user: User, password: String) = userRepository.registerUser(user, password)
    fun addRegisteredUserToDatabase(username: String, pictureURL: String, email: String, password: String, quality: String) {
        userRepository.addUserToDatabase(username, pictureURL, email, password, quality)
    }
    fun registerToken(email: String, username: String, fcmToken: String) = userRepository.registerToken(email, username, fcmToken)

}
