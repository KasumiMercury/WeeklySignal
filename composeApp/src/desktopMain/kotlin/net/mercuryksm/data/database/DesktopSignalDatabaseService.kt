package net.mercuryksm.data.database

/**
 * Desktop-specific implementation of SignalDatabaseService.
 * Inherits all common database operations from BaseSignalDatabaseService.
 */
class DesktopSignalDatabaseService(
    override val databaseRepository: DatabaseRepository
) : BaseSignalDatabaseService()