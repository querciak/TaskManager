package team4.aalto.fi.domain.usecase

import team4.aalto.fi.data.repository.FileRepository

class ProjectFilesUseCase(private val repo: FileRepository) {

    fun getAllFiles(projectId: String) = repo.getAllFiles(projectId)
}