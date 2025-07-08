package net.mercuryksm.data.database

actual fun getRoomDatabase(): AppDatabase {
    return getDatabaseBuilder().build()
}
