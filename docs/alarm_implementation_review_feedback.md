
# アラーム実装に関する改善提案

## 1. はじめに

このドキュメントは、`docs/review_alarm_implementation.md` のレビュー項目に基づき、WeeklySignalのAndroidアラーム実装に関する具体的な改善提案をまとめたものです。
現在の実装は非常に高品質ですが、さらなるユーザー体験の向上とメンテナンス性の改善を目指します。

## 2. 改善提案

### 2.1. 通知チャネルの設計を単一化する

**現状の課題:**
`AndroidAlarmManager.kt` では、アラームのサウンドとバイブレーションの設定に応じて、動的に通知チャネルIDを生成しています。

```kotlin
// AndroidAlarmManager.kt
private fun createNotificationChannel(sound: Boolean, vibration: Boolean): String {
    val channelId = "${CHANNEL_ID_BASE}_${if (sound) "s" else "n"}_${if (vibration) "v" else "n"}"
    // ...
}
```

この実装は、ユーザーがOSの「設定」アプリから通知の挙動（サウンドの変更、バイブレーションのON/OFFなど）をカスタマイズするのを困難にします。設定の組み合わせごとに別のチャネルが作られるため、ユーザーは「WeeklySignalのアラーム」という単一のカテゴリとして設定を管理できません。

**提案:**
通知チャネルを単一（例: `weekly_signal_alarms`）に統一し、サウンドやバイブレーションの制御は通知を生成するタイミングで行うように変更します。

**具体的な実装案:**

1.  **`AndroidAlarmManager.kt` の `createNotificationChannel` を修正**
    -   引数をなくし、常に固定のチャネルID（`CHANNEL_ID_BASE`）を使用するようにします。
    -   チャネルの重要度（Importance）は `IMPORTANCE_HIGH` に設定し、デフォルトでサウンドとバイブレーションが有効になるようにします。

    ```kotlin
    // 修正案
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            val channelId = CHANNEL_ID_BASE // 固定IDを使用
            
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    enableVibration(true)
                    vibrationPattern = VIBRATION_PATTERN
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    ```

2.  **`AlarmReceiver.kt` の通知生成処理を修正**
    -   通知を構築する際に、`AlarmInfo` の `sound` と `vibration` の設定に応じて、`setSound(null)` や `setVibrate(null)` を呼び出し、動的に音や振動を無効化します。

    ```kotlin
    // AlarmReceiver.kt の修正案
    val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID) // 固定IDを使用
        // ...
        .apply {
            if (alarmInfo.sound) {
                // チャネルのデフォルトサウンドを使用
            } else {
                setSound(null)
            }
            
            if (alarmInfo.vibration) {
                // チャネルのデフォルト振動を使用
            } else {
                setVibrate(null)
            }
        }
    ```

**期待される効果:**
ユーザーはOSの設定画面で「Weekly Signal Alarms」という単一のチャネルに対して、好みのサウンドやバイブレーションパターン、通知のポップアップ表示などを自由にカスタマイズできるようになり、Androidプラットフォーム標準のユーザー体験を提供できます。

### 2.2. データ永続化戦略をRoomに一本化する

**現状の課題:**
アラーム情報の永続化に関して、`SharedPreferences` を使用した古い実装と、Roomデータベース (`AlarmStateEntity`) を使用した新しい実装が混在しています。特に `BootReceiver.kt` は、デバイス再起動時のアラーム再設定処理を `SharedPreferences` に依存しています。

```kotlin
// BootReceiver.kt
// SharedPreferencesから保存されたアラームIDセットを取得している
val allAlarmIds = sharedPrefs.getStringSet("all_alarm_ids", emptySet()) ?: emptySet()
allAlarmIds.forEach { alarmIdStr ->
    // SharedPreferencesからアラーム情報を取得して再設定
}
```

この状態は、データソースが分散しているため、管理が煩雑で、将来的に不整合を引き起こす可能性があります。

**提案:**
`SharedPreferences` への依存を完全に排除し、アラーム情報の永続化をRoomデータベースに一本化します。`BootReceiver` は、Roomに保存されている `AlarmStateEntity` の情報を基にアラームを再設定するように修正します。

**具体的な実装案:**

1.  **`BootReceiver.kt` の `rescheduleAllAlarms` を修正**
    -   `SharedPreferences` の代わりに `SignalDatabaseService` (Room) を使って、保存されているすべてのアラーム状態 (`AlarmStateEntity`) を取得します。
    -   取得したエンティティから `SignalItem` と `TimeSlot` の情報を復元し、`AndroidSignalAlarmManager` を使ってアラームを再スケジュールします。

    ```kotlin
    // BootReceiver.kt の修正案
    private suspend fun rescheduleAllAlarms(context: Context) {
        val databaseService = DatabaseServiceFactory(context).createSignalDatabaseService()
        val alarmManager = AndroidSignalAlarmManager(context, databaseService)
        
        // Room DBからスケジュール済みのアラーム状態をすべて取得
        val scheduledAlarms = databaseService.getAllScheduledAlarmStates() // 仮のメソッド名
        
        for (alarmState in scheduledAlarms) {
            // alarmStateからSignalItemとTimeSlotの情報を取得
            val signalItem = databaseService.getSignalItemById(alarmState.signalItemId) // 仮のメソッド名
            val timeSlot = signalItem?.timeSlots?.find { it.id == alarmState.timeSlotId }
            
            if (signalItem != null && timeSlot != null) {
                val settings = AlarmSettings(
                    sound = signalItem.sound,
                    vibration = signal.vibration,
                    title = signalItem.name,
                    message = signalItem.description,
                    alarmId = "${signalItem.id}_${timeSlot.id}"
                )
                // アラームを再スケジュール
                alarmManager.scheduleAlarm(timeSlot, settings)
            }
        }
    }
    ```
    *注: 上記コードは概念的なものであり、`AlarmStateEntity` に `signalItemId` を含めるなど、エンティティの設計見直しが必要になる場合があります。*

2.  **`AndroidSignalAlarmManager.kt` から `SharedPreferences` 関連コードを削除**
    -   `sharedPrefs` プロパティと、`migrateSharedPreferencesToRoom` メソッド、関連する定数を削除します。

**期待される効果:**
データ永続化の方法がRoomに統一されることで、データの一貫性が保証され、コードの可読性とメンテナンス性が大幅に向上します。トランザクション管理も容易になり、より堅牢なシステムを構築できます。

## 3. その他の評価

-   **フルスクリーンインテント:** レビュー対象のドキュメントでは言及されていましたが、現在の `AlarmReceiver.kt` では使用されておらず、通常の通知が使われています。これは適切な改善が既に行われたものと評価します。
-   **アラーム音の停止処理:** `AlarmReceiver` 内で `Ringtone` オブジェクトを管理し、Dismissアクションで明示的に停止する現在の実装は、ドキュメントの提案を満たす堅牢なものです。

以上の改善提案を実装することで、WeeklySignalのアラーム機能はさらに洗練され、ユーザーにとって使いやすく、開発者にとってメンテナンスしやすいものとなるでしょう。
