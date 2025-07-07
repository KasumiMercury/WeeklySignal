package net.mercuryksm.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "app_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    ).addMigrations(MIGRATION_3_4)
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        // 1. 新しいテーブルを作成
        connection.execSQL("""
            CREATE TABLE `alarm_states_new` (
                `timeSlotId` TEXT NOT NULL, 
                `signalItemId` TEXT NOT NULL DEFAULT '',
                `isAlarmScheduled` INTEGER NOT NULL, 
                `pendingIntentRequestCode` INTEGER NOT NULL, 
                `scheduledAt` INTEGER NOT NULL, 
                `nextAlarmTime` INTEGER NOT NULL, 
                PRIMARY KEY(`timeSlotId`), 
                FOREIGN KEY(`timeSlotId`) REFERENCES `time_slots`(`id`) ON DELETE CASCADE
            )
        """)
        // 2. データを移行 (signalItemIdはtime_slotsテーブルから取得)
        connection.execSQL("""
            INSERT INTO alarm_states_new (timeSlotId, signalItemId, isAlarmScheduled, pendingIntentRequestCode, scheduledAt, nextAlarmTime)
            SELECT
                a.timeSlotId,
                t.signalId,
                a.isAlarmScheduled,
                a.pendingIntentRequestCode,
                a.scheduledAt,
                a.nextAlarmTime
            FROM alarm_states AS a
            INNER JOIN time_slots AS t ON a.timeSlotId = t.id
        """)
        // 3. 古いテーブルを削除
        connection.execSQL("DROP TABLE alarm_states")
        // 4. 新しいテーブルをリネーム
        connection.execSQL("ALTER TABLE alarm_states_new RENAME TO alarm_states")
        // 5. インデックスを再作成
        connection.execSQL("CREATE INDEX `index_alarm_states_timeSlotId` ON `alarm_states` (`timeSlotId`)")
        connection.execSQL("CREATE INDEX `index_alarm_states_signalItemId` ON `alarm_states` (`signalItemId`)")
    }
}