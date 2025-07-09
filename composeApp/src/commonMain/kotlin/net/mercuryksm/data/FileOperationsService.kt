package net.mercuryksm.data

sealed class FileOperationResult {
    data class Success(val message: String) : FileOperationResult()
    data class Error(val message: String) : FileOperationResult()
}

sealed class FileReadResult {
    data class Success(val content: String) : FileReadResult()
    data class Error(val message: String) : FileReadResult()
}

interface FileOperationsService {
    
    suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult
    
    suspend fun importFromFile(): FileReadResult
    
    suspend fun shareFile(
        content: String,
        fileName: String
    ): FileOperationResult
}

expect class FileOperationsServiceImpl() : FileOperationsService