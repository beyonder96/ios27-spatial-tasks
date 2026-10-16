package com.example.ios27tasks.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ios27tasks.TaskTab
import com.example.ios27tasks.data.CloudSyncStatus
import com.example.ios27tasks.data.DeletedTaskTombstone
import com.example.ios27tasks.data.AiPlannedTask
import com.example.ios27tasks.data.GeminiPlannerResult
import com.example.ios27tasks.data.GeminiPlannerService
import com.example.ios27tasks.data.SpatialUserPreferences
import com.example.ios27tasks.data.SupabaseSyncManager
import com.example.ios27tasks.data.TaskDatabase
import com.example.ios27tasks.data.TaskEntity
import com.example.ios27tasks.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class SpatialTasksViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TaskDatabase.getInstance(application)
    private val taskDao = database.taskDao()
    private val prefsRepo = UserPreferencesRepository(application)
    private val supabaseSync = SupabaseSyncManager(taskDao, prefsRepo)
    private val geminiPlanner = GeminiPlannerService()

    val tasks: StateFlow<List<TaskEntity>> = taskDao.observeAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val userPreferences: StateFlow<SpatialUserPreferences> = prefsRepo.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SpatialUserPreferences()
        )

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.IDLE)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val _lastDeletedTask = MutableStateFlow<TaskEntity?>(null)
    val lastDeletedTask: StateFlow<TaskEntity?> = _lastDeletedTask.asStateFlow()
    private var undoDismissJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Garante isolamento estrito: limpa qualquer resquício de conexão com o Tessera
            prefsRepo.sanitizeTesseraIsolation()

            val currentPrefs = prefsRepo.preferencesFlow.first()
            // Semeia tarefas iniciais de exemplo apenas UMA ÚNICA VEZ no primeiro uso
            if (!currentPrefs.hasCompletedInitialSeed) {
                if (taskDao.countTasks() == 0) {
                    taskDao.upsertAll(TaskDatabase.defaultSeedTasks())
                }
                prefsRepo.setCompletedInitialSeed(true)
            }

            // Sincronização inicial com nuvem se ativada pelo usuário (filtrando tombstones locais)
            if (currentPrefs.autoSyncEnabled && currentPrefs.supabaseUrl.isNotBlank()) {
                _syncStatus.value = CloudSyncStatus.SYNCING
                val ok = supabaseSync.pullAndMergeFromCloud(currentPrefs)
                _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
            }

            while (isActive) {
                delay(12_000L)
                val prefs = prefsRepo.preferencesFlow.first()
                if (prefs.autoSyncEnabled && prefs.supabaseUrl.isNotBlank()) {
                    _syncStatus.value = CloudSyncStatus.SYNCING
                    val ok = supabaseSync.pullAndMergeFromCloud(prefs)
                    _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
                }
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = userPreferences.value.isDarkMode
            prefsRepo.setDarkMode(!current)
        }
    }

    fun selectTab(tab: TaskTab) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setSelectedTab(tab)
        }
    }

    fun addTask(
        title: String,
        tag: String,
        tab: TaskTab,
        scheduledTime: String? = null
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val cleanTag = tag.trim().ifEmpty {
            if (tab == TaskTab.PERSONA) "Autocuidado" else "Governança & Leitos"
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentTasks = taskDao.getTasksByTab(tab.name)
            val minOrder = currentTasks.minOfOrNull { it.orderIndex } ?: 0
            val entity = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = cleanTitle,
                tag = cleanTag,
                isCompleted = false,
                tabName = tab.name,
                createdAt = System.currentTimeMillis(),
                scheduledTime = scheduledTime?.trim()?.ifBlank { null },
                orderIndex = minOrder - 1
            )
            taskDao.removeTombstone(entity.id)
            taskDao.upsertTask(entity)
            pushIfAutoSyncEnabled()
        }
    }

    fun updateTaskTime(task: TaskEntity, newTime: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.updateScheduledTime(task.id, newTime?.trim()?.ifBlank { null })
            pushIfAutoSyncEnabled()
        }
    }

    fun moveTaskUp(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskDao.getTasksByTab(task.tabName)
            val index = list.indexOfFirst { it.id == task.id }
            if (index > 0) {
                val prevTask = list[index - 1]
                val prevOrder = prevTask.orderIndex
                val currOrder = task.orderIndex
                val newPrevOrder = if (prevOrder == currOrder) currOrder + 1 else currOrder
                val newCurrOrder = if (prevOrder == currOrder) currOrder else prevOrder
                taskDao.updateOrderIndex(task.id, newCurrOrder)
                taskDao.updateOrderIndex(prevTask.id, newPrevOrder)
                pushIfAutoSyncEnabled()
            }
        }
    }

    fun moveTaskDown(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskDao.getTasksByTab(task.tabName)
            val index = list.indexOfFirst { it.id == task.id }
            if (index in 0 until list.size - 1) {
                val nextTask = list[index + 1]
                val nextOrder = nextTask.orderIndex
                val currOrder = task.orderIndex
                val newNextOrder = if (nextOrder == currOrder) currOrder - 1 else currOrder
                val newCurrOrder = if (nextOrder == currOrder) currOrder else nextOrder
                taskDao.updateOrderIndex(task.id, newCurrOrder)
                taskDao.updateOrderIndex(nextTask.id, newNextOrder)
                pushIfAutoSyncEnabled()
            }
        }
    }

    fun sortByScheduledTime(tab: TaskTab) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = taskDao.getTasksByTab(tab.name)
            if (list.isEmpty()) return@launch
            val sorted = list.sortedWith(
                compareBy<TaskEntity> { it.scheduledTime.isNullOrBlank() }
                    .thenBy { it.scheduledTime }
                    .thenBy { it.createdAt }
            )
            val updated = sorted.mapIndexed { i, t -> t.copy(orderIndex = i) }
            taskDao.updateTasks(updated)
            pushIfAutoSyncEnabled()
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val nowCompleted = !task.isCompleted
            val updated = task.copy(
                isCompleted = nowCompleted,
                completedAt = if (nowCompleted) System.currentTimeMillis() else null
            )
            taskDao.updateTask(updated)
            pushIfAutoSyncEnabled()
        }
    }

    fun setTaskCompleted(task: TaskEntity, completed: Boolean) {
        if (task.isCompleted == completed) return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(
                isCompleted = completed,
                completedAt = if (completed) System.currentTimeMillis() else null
            )
            taskDao.updateTask(updated)
            pushIfAutoSyncEnabled()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Registra tombstone para NUNCA mais ressuscitar a tarefa ao fechar o app ou sincronizar
            taskDao.deleteById(task.id)
            taskDao.insertTombstone(DeletedTaskTombstone(task.id))
            _lastDeletedTask.value = task
            undoDismissJob?.cancel()
            undoDismissJob = viewModelScope.launch {
                delay(5_000L)
                _lastDeletedTask.value = null
            }
            pushIfAutoSyncEnabled()
        }
    }

    fun undoDelete() {
        val deleted = _lastDeletedTask.value ?: return
        undoDismissJob?.cancel()
        _lastDeletedTask.value = null
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.removeTombstone(deleted.id)
            taskDao.upsertTask(deleted)
            pushIfAutoSyncEnabled()
        }
    }

    fun dismissUndoBanner() {
        undoDismissJob?.cancel()
        _lastDeletedTask.value = null
    }

    fun syncWithCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            _syncStatus.value = CloudSyncStatus.SYNCING
            val prefs = prefsRepo.preferencesFlow.first()
            val ok = supabaseSync.pullAndMergeFromCloud(prefs) && supabaseSync.pushTasksToCloud(prefs)
            _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
        }
    }

    fun saveSupabaseSettings(
        url: String,
        anonKey: String,
        hubId: String,
        autoSync: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.updateSupabaseConfig(url, anonKey, hubId, autoSync)
            if (autoSync) {
                syncWithCloud()
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setCompletedOnboarding(true)
        }
    }

    fun linkGoogleAccount(email: String, displayName: String?, photoUrl: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setGoogleAccount(email, displayName, photoUrl)
            prefsRepo.setCompletedOnboarding(true)
        }
    }

    fun unlinkGoogleAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.unlinkGoogleAccount()
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setCompletedOnboarding(false)
        }
    }

    fun setGeminiApiKey(key: String) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.setGeminiApiKey(key)
        }
    }

    suspend fun planRoutineWithAi(prompt: String): GeminiPlannerResult {
        val prefs = prefsRepo.preferencesFlow.first()
        return geminiPlanner.generateHospitalRoutine(prompt, prefs.geminiApiKey)
    }

    fun addPlannedTasks(plannedTasks: List<AiPlannedTask>) {
        val selected = plannedTasks.filter { it.isSelected }
        if (selected.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val entities = selected.mapIndexed { index, task ->
                TaskEntity(
                    id = UUID.randomUUID().toString(),
                    title = task.title,
                    tag = task.tag,
                    isCompleted = false,
                    tabName = task.tab.name,
                    createdAt = System.currentTimeMillis() + index,
                    scheduledTime = task.scheduledTime,
                    orderIndex = index
                )
            }
            entities.forEach { taskDao.removeTombstone(it.id) }
            taskDao.upsertAll(entities)
            pushIfAutoSyncEnabled()
        }
    }

    private suspend fun pushIfAutoSyncEnabled() {
        val prefs = prefsRepo.preferencesFlow.first()
        if (prefs.autoSyncEnabled && prefs.supabaseUrl.isNotBlank() && !prefs.supabaseUrl.contains("hyoveowiisbigcpxzoro")) {
            _syncStatus.value = CloudSyncStatus.SYNCING
            val ok = supabaseSync.pushTasksToCloud(prefs)
            _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
        }
    }
}
