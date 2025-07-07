
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

### 2.2. データ永続化戦略のRoomへの一本化 (対応済み)

**評価:**
以前の実装では、アラーム情報の永続化に `SharedPreferences` とRoomデータベースが混在しており、特にデバイス再起動時の処理 (`BootReceiver`) が古い `SharedPreferences` に依存していました。

現在の実装では、この点が**完全に改善されています。**

-   **`BootReceiver` の修正:** `BootReceiver.kt` は、Roomデータベース (`SignalDatabaseService`経由) から直接スケジュール済みのアラーム情報を取得して再設定するよう修正されました。これにより、`SharedPreferences` への依存がなくなり、データソースが一本化されました。

    ```kotlin
    // BootReceiver.kt (現在の実装)
    private suspend fun rescheduleAllAlarms(context: Context) {
        try {
            val databaseService = DatabaseServiceFactory(context).createSignalDatabaseService()
            val alarmManager = AndroidSignalAlarmManager(context, databaseService)
            
            // Roomデータベースからすべてのアラーム状態を取得
            val scheduledAlarmsResult = databaseService.getAllScheduledAlarmStates()
            
            if (scheduledAlarmsResult.isSuccess) {
                val scheduledAlarms = scheduledAlarmsResult.getOrNull() ?: emptyList()
                
                scheduledAlarms.forEach { alarmState ->
                    // ... アラームを再スケジュールするロジック ...
                }
            }
        } // ...
    }
    ```

-   **堅牢なデータ移行:** `AndroidSignalAlarmManager.kt` には、`SharedPreferences` に残っている古いデータを一度だけRoomデータベースに移行するための `migrateSharedPreferencesToRoom` メソッドが実装されています。移行完了後は古いデータを削除する処理も含まれており、非常に堅牢な設計です。

-   **一貫性の確保:** `AlarmStateEntity` に `signalItemId` が追加されたことで、データ構造がより明確になり、アラームと `SignalItem` の関連性が保証されるようになりました。

**結論:**
データ永続化戦略は、当初の提案通りRoomデータベースに完全に一本化されました。これにより、アプリケーションの信頼性とメンテナンス性が大幅に向上したと評価します。

## 3. その他の評価

-   **フルスクリーンインテント:** レビュー対象のドキュメントでは言及されていましたが、現在の `AlarmReceiver.kt` では使用されておらず、通常の通知が使われています。これは適切な改善が既に行われたものと評価します。
-   **アラーム音の停止処理:** `AlarmReceiver` 内で `Ringtone` オブジェクトを管理し、Dismissアクションで明示的に停止する現在の実装は、ドキュメントの提案を満たす堅牢なものです。

以上の改善提案を実装することで、WeeklySignalのアラーム機能はさらに洗練され、ユーザーにとって使いやすく、開発者にとってメンテナンスしやすいものとなるでしょう。
