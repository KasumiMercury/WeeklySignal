package net.mercuryksm.data.database

/**
 * Android-specific implementation of SignalDatabaseService.
 * Inherits all common database operations from BaseSignalDatabaseService.
 */
class AndroidSignalDatabaseService(
    override val databaseRepository: DatabaseRepository
) : BaseSignalDatabaseService()