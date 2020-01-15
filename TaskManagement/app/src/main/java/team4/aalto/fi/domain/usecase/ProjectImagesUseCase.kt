package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.ImageRepository

class ProjectImagesUseCase(private val repo: ImageRepository) {

    fun getProjectImages(projectId: String) = repo.getProjectImages(projectId)
}