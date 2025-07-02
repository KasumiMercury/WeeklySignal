package net.mercuryksm.data.database

import net.mercuryksm.data.SignalItem

interface SignalDatabaseService {
    suspend fun saveSignalItem(signalItem: SignalItem): Result<Unit>
    suspend fun updateSignalItem(signalItem: SignalItem): Result<Unit>
    suspend fun deleteSignalItem(signalId: String): Result<Unit>
    suspend fun getSignalItemById(signalId: String): Result<SignalItem?>
    suspend fun getAllSignalItems(): Result<List<SignalItem>>
    suspend fun clearAllData(): Result<Unit>
}