package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.ProjectRepository
import team4.aalto.fi.domain.model.Project
import team4.aalto.fi.domain.model.User

class UserProjectsUseCase(private val projectRepository: ProjectRepository) {

    fun getAllUserProjects(user: String) = projectRepository.getAllUserProjects(user)

}