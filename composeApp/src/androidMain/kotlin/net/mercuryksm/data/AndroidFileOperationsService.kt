package net.mercuryksm.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidFileOperationsService(
    private val context: Context,
    private val onExportFile: suspend (String) -> Uri?,
    private val onImportFile: suspend () -> Uri?
) : FileOperationsService {
    
    override suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val uri = onExportFile(fileName)
            
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                FileOperationResult.Success("Export saved successfully")
            } else {
                FileOperationResult.Error("Export cancelled by user")
            }
        } catch (e: Exception) {
            FileOperationResult.Error("Failed to export file: ${e.message}")
        }
    }
    
    override suspend fun importFromFile(): FileReadResult = withContext(Dispatchers.IO) {
        try {
            val uri = onImportFile()
            
            if (uri != null) {
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes().toString(Charsets.UTF_8)
                }
                
                if (content != null) {
                    FileReadResult.Success(content)
                } else {
                    FileReadResult.Error("Failed to read file content")
                }
            } else {
                FileReadResult.Error("Import cancelled by user")
            }
        } catch (e: Exception) {
            FileReadResult.Error("Failed to import file: ${e.message}")
        }
    }
}

// Actual implementation for expect/actual pattern
actual class FileOperationsServiceImpl : FileOperationsService {
    
    actual constructor() {
        // This should not be used directly on Android
        // Use rememberFileOperationsService() instead
    }
    
    override suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult {
        return FileOperationResult.Error("Use rememberFileOperationsService() in Compose context")
    }
    
    override suspend fun importFromFile(): FileReadResult {
        return FileReadResult.Error("Use rememberFileOperationsService() in Compose context")
    }
}

