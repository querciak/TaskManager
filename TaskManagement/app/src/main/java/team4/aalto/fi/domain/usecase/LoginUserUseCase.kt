package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.UserRepository

class LoginUserUseCase(private val userRepository: UserRepository) {

    fun loginUser(email: String, password: String) = userRepository.loginUser(email, password)
    fun addToken(email: String, fcmToken: String) = userRepository.addToken(email, fcmToken)
}