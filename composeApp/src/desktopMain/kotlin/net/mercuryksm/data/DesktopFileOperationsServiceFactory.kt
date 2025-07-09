package net.mercuryksm.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberFileOperationsService(): FileOperationsService {
    return remember { FileOperationsServiceImpl() }
}
