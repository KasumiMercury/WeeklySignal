package net.mercuryksm.di

import net.mercuryksm.data.SignalRepository
import net.mercuryksm.data.database.DatabaseServiceFactory
import net.mercuryksm.data.ExportImportService
import net.mercuryksm.data.FileOperationsServiceFactory
import net.mercuryksm.event.EventBus
import net.mercuryksm.event.EventBusImpl
import net.mercuryksm.notification.AlarmServiceFactory
import net.mercuryksm.service.AlarmManagementService
import net.mercuryksm.service.AlarmManagementServiceImpl
import net.mercuryksm.ui.exportimport.ExportImportViewModel
import net.mercuryksm.ui.weekly.WeeklySignalViewModel
import org.koin.dsl.module
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val appModule = module {
    // Event System
    single<EventBus> { EventBusImpl() }
    
    // Service Scope for background services
    single<CoroutineScope> {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    
    // Database
    single { DatabaseServiceFactory.create() }
    single { SignalRepository(get()) }
    
    // File Operations
    single { FileOperationsServiceFactory.create() }
    
    // Export/Import Service
    single { ExportImportService() }
    
    // Alarm Management
    single { AlarmServiceFactory.create() }
    single<AlarmManagementService> {
        AlarmManagementServiceImpl(
            alarmManager = get(),
            eventBus = get(),
            serviceScope = get()
        )
    }
    
    // ViewModels
    single { WeeklySignalViewModel(get(), get(), get()) }
    single { ExportImportViewModel(get(), get(), get(), get(), get()) }
}