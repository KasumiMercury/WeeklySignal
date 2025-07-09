package net.mercuryksm.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
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
    
    override suspend fun shareFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            // For desktop, sharing means saving to a default location and optionally opening file explorer
            val userHome = System.getProperty("user.home")
            val downloadsDir = File(userHome, "Downloads")
            
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            val file = File(downloadsDir, fileName)
            
            // Handle file name conflicts
            var finalFile = file
            var counter = 1
            while (finalFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val extension = fileName.substringAfterLast(".")
                finalFile = File(downloadsDir, "${nameWithoutExt}_$counter.$extension")
                counter++
            }
            
            finalFile.writeText(content)
            
            // Try to open the file location in file explorer
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(downloadsDir)
                }
            } catch (e: Exception) {
                // Ignore desktop opening errors
            }
            
            FileOperationResult.Success("File shared to: ${finalFile.absolutePath}")
        } catch (e: Exception) {
            FileOperationResult.Error("Failed to share file: ${e.message}")
        }
    }
}