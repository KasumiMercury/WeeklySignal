package net.mercuryksm.data.database

import android.content.Context

private var applicationContext: Context? = null

fun setDatabaseContext(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getDatabaseContext(): Any {
    return applicationContext ?: throw IllegalStateException(
        "Database context not initialized. Call setDatabaseContext() from your Application class."
    )
}