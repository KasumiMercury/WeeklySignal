package net.mercuryksm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.mercuryksm.data.DayOfWeekJp
import net.mercuryksm.data.SignalItem

@Composable
fun WeeklySignalView(
    modifier: Modifier = Modifier,
    items: List<SignalItem> = getSampleData(),
    onItemClick: (SignalItem) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // タイトル
        Text(
            text = "Weekly Signal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // コンテンツ
        if (items.isEmpty()) {
            EmptyState()
        } else {
            WeeklyGrid(
                items = items,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun WeeklyGrid(
    items: List<SignalItem>,
    onItemClick: (SignalItem) -> Unit
) {
    val timeSlots = generateTimeSlots(items)
    val scrollState = rememberLazyListState()
    
    Column {
        
        // メインコンテンツエリア
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 固定曜日ラベル列
            Column(
                modifier = Modifier.width(60.dp)
            ) {
                DayOfWeekJp.values().forEach { dayOfWeek ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayOfWeek.getDisplayName(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    if (dayOfWeek != DayOfWeekJp.SUNDAY) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
            
            // スクロール可能なタイムライン
            LazyRow(
                state = scrollState,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(timeSlots) { timeSlot ->
                    TimeSlotColumn(
                        timeSlot = timeSlot,
                        allItems = items,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Signal Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add your first signal to get started",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun generateTimeSlots(allItems: List<SignalItem>): List<TimeSlot> {
    if (allItems.isEmpty()) {
        return emptyList()
    }
    
    val itemTimes = allItems.map { it.getTimeInMinutes() }.toSet()
    val minTime = itemTimes.minOrNull() ?: 0
    val maxTime = itemTimes.maxOrNull() ?: 1440
    
    val slots = mutableListOf<TimeSlot>()
    var currentTime = minTime
    
    while (currentTime <= maxTime) {
        val hour = currentTime / 60
        val minute = currentTime % 60
        val hasItems = itemTimes.contains(currentTime)
        
        slots.add(TimeSlot(hour, minute, hasItems))
        
        if (hasItems) {
            currentTime += 30
        } else {
            val nextItemTime = itemTimes.filter { it > currentTime }.minOrNull()
            if (nextItemTime != null && nextItemTime - currentTime <= 15) {
                currentTime = nextItemTime
            } else {
                currentTime += 15
            }
        }
    }
    
    return slots
}

// テスト用サンプルデータ
private fun getSampleData(): List<SignalItem> {
    return listOf(
        SignalItem("1", "朝のストレッチ", 7, 0, DayOfWeekJp.MONDAY, "朝の運動を始めましょう"),
        SignalItem("2", "昼食の準備", 12, 0, DayOfWeekJp.MONDAY, "お昼ご飯の時間です"),
        SignalItem("3", "夕方の散歩", 17, 30, DayOfWeekJp.MONDAY, "散歩に出かけましょう"),
        
        SignalItem("4", "モーニングコーヒー", 8, 0, DayOfWeekJp.TUESDAY, "コーヒーブレイクの時間"),
        SignalItem("5", "会議の準備をする時間です", 14, 0, DayOfWeekJp.TUESDAY, "重要な会議があります"),
        SignalItem("6", "読書タイム", 20, 0, DayOfWeekJp.TUESDAY, "本を読む時間"),
        
        SignalItem("7", "ヨガの時間", 6, 30, DayOfWeekJp.WEDNESDAY, "朝のヨガセッション"),
        SignalItem("8", "ランチミーティング", 12, 30, DayOfWeekJp.WEDNESDAY, "チームとのランチ"),
        
        SignalItem("9", "プレゼンテーションの最終確認", 9, 0, DayOfWeekJp.THURSDAY, "発表前の最終チェック"),
        SignalItem("10", "おやつタイム", 15, 0, DayOfWeekJp.THURSDAY, "小腹が空いた時間"),
        SignalItem("11", "ジムに行く時間", 18, 0, DayOfWeekJp.THURSDAY, "運動する時間"),
        
        SignalItem("12", "週末の計画を立てる", 13, 0, DayOfWeekJp.FRIDAY, "楽しい週末の準備"),
        SignalItem("13", "友達との電話", 19, 0, DayOfWeekJp.FRIDAY, "久しぶりの友達との通話"),
        
        SignalItem("14", "掃除の時間", 10, 0, DayOfWeekJp.SATURDAY, "部屋の掃除をしましょう"),
        SignalItem("15", "買い物", 14, 30, DayOfWeekJp.SATURDAY, "食材の買い出し"),
        
        SignalItem("16", "のんびりとした朝食", 9, 30, DayOfWeekJp.SUNDAY, "ゆっくり朝食を楽しむ"),
        SignalItem("17", "家族との時間", 16, 0, DayOfWeekJp.SUNDAY, "家族と過ごす大切な時間")
    )
}