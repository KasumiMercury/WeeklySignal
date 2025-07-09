package net.mercuryksm.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FileOperationsServiceImpl : FileOperationsService {
    
    override suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Export Weekly Signal Data"
            fileChooser.selectedFile = File(fileName)
            fileChooser.fileFilter = FileNameExtensionFilter(
                "Weekly Signal Files (*.weeklysignal)", 
                "weeklysignal"
            )
            
            val result = fileChooser.showSaveDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                
                // Ensure the file has the correct extension
                val finalFile = if (!file.name.endsWith(".weeklysignal")) {
                    File(file.parentFile, file.nameWithoutExtension + ".weeklysignal")
                } else {
                    file
                }
                
                finalFile.writeText(content)
                FileOperationResult.Success("Export saved to: ${finalFile.absolutePath}")
            } else {
                FileOperationResult.Error("Export cancelled by user")
            }
        } catch (e: Exception) {
            FileOperationResult.Error("Failed to export file: ${e.message}")
        }
    }
    
    override suspend fun importFromFile(): FileReadResult = withContext(Dispatchers.IO) {
        try {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Import Weekly Signal Data"
            fileChooser.fileFilter = FileNameExtensionFilter(
                "Weekly Signal Files (*.weeklysignal)", 
                "weeklysignal"
            )
            
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                
                if (!file.exists()) {
                    return@withContext FileReadResult.Error("Selected file does not exist")
                }
                
                if (!file.name.endsWith(".weeklysignal")) {
                    return@withContext FileReadResult.Error("Invalid file type. Please select a .weeklysignal file")
                }
                
                val content = file.readText()
                FileReadResult.Success(content)
            } else {
                FileReadResult.Error("Import cancelled by user")
            }
        } catch (e: Exception) {
            FileReadResult.Error("Failed to import file: ${e.message}")
        }
    }
    
}
