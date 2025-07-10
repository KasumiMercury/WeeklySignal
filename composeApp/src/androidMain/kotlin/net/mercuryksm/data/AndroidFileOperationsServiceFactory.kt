package net.mercuryksm.data

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
actual fun rememberFileOperationsService(): FileOperationsService {
    val context = LocalContext.current
    
    var exportContinuation by remember { mutableStateOf<kotlin.coroutines.Continuation<Uri?>?>(null) }
    var importContinuation by remember { mutableStateOf<kotlin.coroutines.Continuation<Uri?>?>(null) }
    
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data
        } else {
            null
        }
        exportContinuation?.resume(uri)
        exportContinuation = null
    }
    
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data
        } else {
            null
        }
        importContinuation?.resume(uri)
        importContinuation = null
    }
    
    return remember {
        AndroidFileOperationsService(
            context = context,
            onExportFile = { fileName ->
                suspendCancellableCoroutine { continuation ->
                    exportContinuation = continuation
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        putExtra(Intent.EXTRA_TITLE, fileName)
                    }
                    createDocumentLauncher.launch(intent)
                }
            },
            onImportFile = {
                suspendCancellableCoroutine { continuation ->
                    importContinuation = continuation
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "*/*"))
                    }
                    openDocumentLauncher.launch(intent)
                }
            }
        )
    }
}