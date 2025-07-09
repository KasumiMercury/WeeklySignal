package net.mercuryksm.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

actual class FileOperationsServiceImpl(
    private val context: Context
) : FileOperationsService {
    
    actual constructor() : this(getCurrentContext())
    
    override suspend fun exportToFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            
            val uri = suspendCancellableCoroutine<Uri?> { continuation ->
                if (context is ComponentActivity) {
                    val launcher = context.registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            continuation.resume(result.data?.data)
                        } else {
                            continuation.resume(null)
                        }
                    }
                    launcher.launch(intent)
                } else {
                    continuation.resume(null)
                }
            }
            
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            
            val uri = suspendCancellableCoroutine<Uri?> { continuation ->
                if (context is ComponentActivity) {
                    val launcher = context.registerForActivityResult(
                        ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            continuation.resume(result.data?.data)
                        } else {
                            continuation.resume(null)
                        }
                    }
                    launcher.launch(intent)
                } else {
                    continuation.resume(null)
                }
            }
            
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
    
    override suspend fun shareFile(
        content: String,
        fileName: String
    ): FileOperationResult = withContext(Dispatchers.IO) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "Weekly Signal Export")
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share Weekly Signal Data")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooserIntent)
            FileOperationResult.Success("Share dialog opened")
        } catch (e: Exception) {
            FileOperationResult.Error("Failed to share file: ${e.message}")
        }
    }
}

// Helper function to get current context
private fun getCurrentContext(): Context {
    // This is a simplified implementation
    // In a real app, you'd want to pass the context properly
    throw UnsupportedOperationException("Context must be provided on Android")
}

// Compose helper to create file operations service
@Composable
fun rememberFileOperationsService(): FileOperationsService {
    val context = LocalContext.current
    return remember { FileOperationsServiceImpl(context) }
}