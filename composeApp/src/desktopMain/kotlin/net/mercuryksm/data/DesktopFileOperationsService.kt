package net.mercuryksm.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual class FileOperationsServiceImpl : FileOperationsService {

    override suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        val dialog = FileDialog(Frame(), "Export Weekly Signal Data", FileDialog.SAVE)
        dialog.file = fileName
        dialog.setFilenameFilter { _, name -> name.endsWith(".weeklysignal") }
        dialog.isVisible = true

        if (dialog.file != null) {
            try {
                val file = File(dialog.directory, dialog.file)
                // Ensure the file has the correct extension
                val finalFile = if (!file.name.endsWith(".weeklysignal")) {
                    File(file.parentFile, file.nameWithoutExtension + ".weeklysignal")
                } else {
                    file
                }
                finalFile.writeText(content)
                FileOperationResult.Success("Export saved to: ${finalFile.absolutePath}")
            } catch (e: Exception) {
                FileOperationResult.Error("Failed to export file: ${e.message}")
            }
        } else {
            FileOperationResult.Error("Export cancelled by user")
        }
    }

    override suspend fun importFromFile(): FileReadResult = withContext(Dispatchers.IO) {
        val dialog = FileDialog(Frame(), "Import Weekly Signal Data", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> name.endsWith(".weeklysignal") }
        dialog.isVisible = true

        if (dialog.file != null) {
            try {
                val file = File(dialog.directory, dialog.file)
                if (!file.exists()) {
                    return@withContext FileReadResult.Error("Selected file does not exist")
                }
                val content = file.readText()
                FileReadResult.Success(content)
            } catch (e: Exception) {
                FileReadResult.Error("Failed to import file: ${e.message}")
            }
        } else {
            FileReadResult.Error("Import cancelled by user")
        }
    }
}