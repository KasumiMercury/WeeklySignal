# WeeklySignalViewModel リファクタリング計画

## 概要

WeeklySignalViewModelは現在、複数の責務を持つ大規模なViewModelとなっており、単一責任の原則に違反しています。本文書では、責務を適切に分離するためのリファクタリング戦略を提案します。

## 現在の問題点

### 1. 単一責任の原則違反
WeeklySignalViewModelは以下の異なる責務を同時に扱っています：
- **週次表示ロジック**: SignalItemの表示とCRUD操作
- **アラーム管理**: 各SignalItemのアラームスケジューリング
- **エクスポート/インポート**: 複雑な選択状態管理と操作

### 2. 複雑な状態管理
```kotlin
// 現在の状態数: 6個のStateFlow
signalItems: StateFlow<List<SignalItem>>
isLoading: StateFlow<Boolean>
exportSelectionState: StateFlow<ExportSelectionState?>
importedItems: StateFlow<List<SignalItem>>
selectedImportItems: StateFlow<List<SignalItem>>
```

### 3. 責務の混在
- CRUD操作にアラーム管理のサイドエフェクトが混在
- エクスポート/インポートロジックが週次表示ViewModelに含まれている
- エラーハンドリングが一貫していない

## リファクタリング戦略

### 1. アーキテクチャ設計原則

#### **単一責任の原則 (SRP)**
各ViewModelは単一の機能領域に責任を持つ

#### **依存性逆転の原則 (DIP)**
抽象化に依存し、具体的な実装に依存しない

#### **疎結合設計**
ViewModel間の直接的な依存関係を避け、イベントベースの通信を使用

### 2. 新しいアーキテクチャ構成

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
├─────────────────────────────────────────────────────────────┤
│  WeeklySignalViewModel    │  ExportImportViewModel          │
│  - 週次表示管理            │  - エクスポート/インポート管理    │
│  - SignalItem CRUD        │  - 選択状態管理                 │
│  - 表示用データ変換        │  - 競合解決                     │
└─────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                           │
├─────────────────────────────────────────────────────────────┤
│  AlarmManagementService           │  EventBus               │
│  - アラームスケジューリング        │  - ViewModel間通信       │
│  - 通知管理                       │  - イベント配信          │
│  - 状態追跡                       │                         │
└─────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
├─────────────────────────────────────────────────────────────┤
│  SignalRepository  │  ExportImportService  │  FileService   │
└─────────────────────────────────────────────────────────────┘
```

### 3. 新しいViewModel構成

#### **WeeklySignalViewModel** (リファクタリング後)
```kotlin
class WeeklySignalViewModel(
    private val signalRepository: SignalRepository,
    private val alarmService: AlarmManagementService,
    private val eventBus: EventBus
) : ViewModel() {
    
    // 週次表示専用の状態
    val signalItems: StateFlow<List<SignalItem>> = signalRepository.signalItems
    val isLoading: StateFlow<Boolean> = signalRepository.isLoading
    
    // 週次表示専用のメソッド
    suspend fun addSignalItem(signalItem: SignalItem)
    suspend fun updateSignalItem(signalItem: SignalItem)
    suspend fun removeSignalItem(signalItemId: String)
    fun getSignalItemsForDay(dayOfWeek: Int): StateFlow<List<SignalItem>>
    fun refreshData()
    
    // アラーム操作は AlarmManagementService に委譲
    // エクスポート/インポートは ExportImportViewModel に分離
}
```

#### **ExportImportViewModel** (新規作成)
```kotlin
class ExportImportViewModel(
    private val signalRepository: SignalRepository,
    private val exportImportService: ExportImportService,
    private val fileService: FileOperationsService,
    private val eventBus: EventBus
) : ViewModel() {
    
    // エクスポート/インポート専用の状態
    private val _exportSelectionState = MutableStateFlow<ExportSelectionState?>(null)
    val exportSelectionState: StateFlow<ExportSelectionState?> = _exportSelectionState
    
    private val _importedItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val importedItems: StateFlow<List<SignalItem>> = _importedItems
    
    private val _selectedImportItems = MutableStateFlow<List<SignalItem>>(emptyList())
    val selectedImportItems: StateFlow<List<SignalItem>> = _selectedImportItems
    
    // エクスポート/インポート専用のメソッド
    fun setExportSelectionState(state: ExportSelectionState?)
    fun clearExportSelectionState()
    fun setImportedItems(items: List<SignalItem>)
    fun clearImportedItems()
    fun setSelectedImportItems(items: List<SignalItem>)
    fun clearSelectedImportItems()
    suspend fun importSignalItemsWithConflictResolution(
        items: List<SignalItem>,
        conflictResolution: ConflictResolution
    ): Result<Unit>
    suspend fun exportSelectedItems(): Result<String>
}
```

#### **AlarmManagementService** (新規作成)
```kotlin
interface AlarmManagementService {
    suspend fun scheduleSignalItemAlarms(signalItem: SignalItem): Result<Unit>
    suspend fun cancelSignalItemAlarms(signalItemId: String): Result<Unit>
    suspend fun updateSignalItemAlarms(signalItem: SignalItem): Result<Unit>
    fun isSignalItemAlarmsEnabled(signalItemId: String): StateFlow<Boolean>
    val alarmStates: StateFlow<Map<String, AlarmState>>
}

class AlarmManagementServiceImpl(
    private val alarmManager: SignalAlarmManager,
    private val alarmRepository: AlarmRepository
) : AlarmManagementService {
    // 実装詳細
}
```

### 4. イベントベース通信システム

#### **EventBus Interface**
```kotlin
interface EventBus {
    fun publish(event: DomainEvent)
    fun <T : DomainEvent> subscribe(eventType: Class<T>): Flow<T>
}

// ドメインイベント
sealed class DomainEvent {
    data class SignalItemCreated(val signalItem: SignalItem) : DomainEvent()
    data class SignalItemUpdated(val signalItem: SignalItem) : DomainEvent()
    data class SignalItemDeleted(val signalItemId: String) : DomainEvent()
    data class SignalItemsImported(val signalItems: List<SignalItem>) : DomainEvent()
}
```

#### **イベントフロー例**
```kotlin
// WeeklySignalViewModel
suspend fun addSignalItem(signalItem: SignalItem) {
    signalRepository.addSignalItem(signalItem)
        .onSuccess { 
            eventBus.publish(DomainEvent.SignalItemCreated(signalItem))
        }
}

// AlarmManagementService
init {
    eventBus.subscribe(DomainEvent.SignalItemCreated::class.java)
        .onEach { event ->
            scheduleSignalItemAlarms(event.signalItem)
        }
        .launchIn(serviceScope)
}
```

## 実装計画

### フェーズ1: 基盤準備 (1-2日)
**目標**: 新しいアーキテクチャの基盤を構築

#### **1.1 EventBus実装**
```kotlin
// 実装ファイル: commonMain/kotlin/net/mercuryksm/event/EventBus.kt
// 実装ファイル: commonMain/kotlin/net/mercuryksm/event/DomainEvent.kt
```

#### **1.2 AlarmManagementService実装**
```kotlin
// 実装ファイル: commonMain/kotlin/net/mercuryksm/service/AlarmManagementService.kt
// 実装ファイル: commonMain/kotlin/net/mercuryksm/service/AlarmManagementServiceImpl.kt
```

#### **1.3 依存性注入の準備**
```kotlin
// Koin設定の追加
// ViewModelファクトリーの更新
```

### フェーズ2: ExportImportViewModel分離 (2-3日)
**目標**: エクスポート/インポート機能を専用ViewModelに分離

#### **2.1 ExportImportViewModel作成**
```kotlin
// 実装ファイル: commonMain/kotlin/net/mercuryksm/ui/exportimport/ExportImportViewModel.kt
```

#### **2.2 エクスポート/インポート機能の移行**
- **移行対象メソッド**:
  - `setExportSelectionState()` → ExportImportViewModel
  - `clearExportSelectionState()` → ExportImportViewModel
  - `setImportedItems()` → ExportImportViewModel
  - `clearImportedItems()` → ExportImportViewModel
  - `setSelectedImportItems()` → ExportImportViewModel
  - `clearSelectedImportItems()` → ExportImportViewModel
  - `importSignalItemsWithConflictResolution()` → ExportImportViewModel
  - `updateSignalItemsWithConflictResolution()` → ExportImportViewModel

#### **2.3 UI層の更新**
- **更新対象ファイル**:
  - `ExportImportScreen.kt` → ExportImportViewModel使用
  - `ExportSelectionScreen.kt` → ExportImportViewModel使用
  - `ImportSelectionScreen.kt` → ExportImportViewModel使用

### フェーズ3: アラーム管理分離 (2-3日)
**目標**: アラーム管理をサービス層に分離

#### **3.1 AlarmManagementService統合**
```kotlin
// WeeklySignalViewModelにAlarmManagementServiceを注入
// CRUD操作からアラーム管理コードを削除
```

#### **3.2 イベントベース通信の実装**
```kotlin
// SignalItem変更イベントの発行
// AlarmManagementServiceでのイベント購読
```

#### **3.3 アラーム状態の独立化**
```kotlin
// アラーム状態をAlarmManagementServiceに移行
// UI層でのアラーム状態表示の更新
```

### フェーズ4: WeeklySignalViewModel最適化 (1-2日)
**目標**: WeeklySignalViewModelを週次表示専用に最適化

#### **4.1 責務の整理**
```kotlin
// 週次表示に関係ないメソッドの削除
// 状態管理の簡素化
```

#### **4.2 パフォーマンス最適化**
```kotlin
// 不要な状態の監視を削除
// メモリ使用量の最適化
```

#### **4.3 エラーハンドリング統一**
```kotlin
// Result型の一貫した使用
// エラーメッセージの統一
```

### フェーズ5: テストとドキュメント (2-3日)
**目標**: 品質保証と文書化

#### **5.1 単体テスト作成**
```kotlin
// WeeklySignalViewModelTest.kt
// ExportImportViewModelTest.kt
// AlarmManagementServiceTest.kt
```

#### **5.2 統合テスト**
```kotlin
// ViewModelとサービス間の連携テスト
// イベントベース通信のテスト
```

#### **5.3 文書の更新**
```kotlin
// CLAUDE.md の更新
// アーキテクチャ図の更新
```

## 期待される効果

### 1. 保守性の向上
- **単一責任**: 各ViewModelが明確な責務を持つ
- **テスタビリティ**: 独立したテストが可能
- **可読性**: コードの理解しやすさが向上

### 2. 拡張性の向上
- **機能追加**: 新機能の追加が容易
- **変更影響**: 変更の影響範囲が限定的
- **再利用性**: サービス層の再利用が可能

### 3. パフォーマンス向上
- **メモリ使用量**: 不要な状態監視の削除
- **CPU使用率**: 効率的なイベント処理
- **バッテリー**: 無駄な処理の削減

## リスク管理

### 1. 技術的リスク
- **複雑性の増加**: 新しいアーキテクチャの学習コスト
- **デバッグの困難**: 非同期イベント処理のデバッグ
- **パフォーマンス**: イベントベース通信のオーバーヘッド

**対策**:
- 段階的な実装とテスト
- 詳細なログ機能の実装
- パフォーマンス測定の実施

### 2. 運用リスク
- **既存機能の動作**: リファクタリング中の機能回帰
- **開発速度**: 一時的な開発速度の低下
- **品質**: 新しいコードの品質保証

**対策**:
- 包括的なテストスイート
- 機能フラグによる段階的リリース
- コードレビューの強化

## 成功指標

### 1. 定量的指標
- **コード行数**: WeeklySignalViewModelの行数50%削減
- **メソッド数**: WeeklySignalViewModelのメソッド数60%削減
- **状態数**: WeeklySignalViewModelの状態数50%削減
- **テストカバレッジ**: 90%以上の維持

### 2. 定性的指標
- **保守性**: 新機能追加時の変更ファイル数削減
- **可読性**: チーム内でのコードレビュー時間短縮
- **安定性**: 機能回帰バグの発生率低下

## 結論

WeeklySignalViewModelの責務分離は、アプリケーションの保守性、拡張性、パフォーマンス向上に大きく貢献します。段階的な実装により、リスクを最小限に抑えながら、より良いアーキテクチャを実現できます。

特に**ExportImportViewModel**の分離は、最も大きな改善効果が期待され、WeeklySignalViewModelから約15個のメソッドと3個の状態を削除可能です。この変更により、各ViewModelの責務が明確になり、テストやメンテナンスが大幅に改善されます。

実装は約10-14日間の期間で完了予定であり、各フェーズで段階的に品質を確保しながら進めることで、安全かつ効果的なリファクタリングが可能です。