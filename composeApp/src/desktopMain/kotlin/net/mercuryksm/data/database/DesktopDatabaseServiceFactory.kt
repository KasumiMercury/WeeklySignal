package net.mercuryksm.data.database

actual class DatabaseServiceFactory {
    actual fun createSignalDatabaseService(): SignalDatabaseService {
        val databaseRepository = DesktopDatabaseRepository()
        return DesktopSignalDatabaseService(databaseRepository)
    }
}