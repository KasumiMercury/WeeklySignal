package net.mercuryksm.data.database

expect class DatabaseServiceFactory {
    fun createSignalDatabaseService(): SignalDatabaseService
}