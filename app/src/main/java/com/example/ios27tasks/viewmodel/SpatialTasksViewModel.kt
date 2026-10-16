package com.example.ios27tasks.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ios27tasks.TaskTab
import com.example.ios27tasks.data.CloudSyncStatus
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
            // Ensure initial seed tasks exist on first launch
            if (taskDao.countTasks() == 0) {
                taskDao.upsertAll(TaskDatabase.defaultSeedTasks())
            }
            while (isActive) {
                val currentPrefs = prefsRepo.preferencesFlow.first()
                if (currentPrefs.autoSyncEnabled) {
                    _syncStatus.value = CloudSyncStatus.SYNCING
                    val ok = supabaseSync.pullAndMergeFromCloud(currentPrefs)
                    _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
                }
                delay(8_000L)
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

    fun addTask(title: String, tag: String, tab: TaskTab) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val cleanTag = tag.trim().ifEmpty {
            if (tab == TaskTab.PERSONA) "Pessoal" else "Global"
        }

        viewModelScope.launch(Dispatchers.IO) {
            val entity = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = cleanTitle,
                tag = cleanTag,
                isCompleted = false,
                tabName = tab.name,
                createdAt = System.currentTimeMillis()
            )
            taskDao.upsertTask(entity)
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
            taskDao.deleteById(task.id)
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

    private suspend fun pushIfAutoSyncEnabled() {
        val prefs = prefsRepo.preferencesFlow.first()
        if (prefs.autoSyncEnabled) {
            _syncStatus.value = CloudSyncStatus.SYNCING
            val ok = supabaseSync.pushTasksToCloud(prefs)
            _syncStatus.value = if (ok) CloudSyncStatus.SYNCED else CloudSyncStatus.OFFLINE
        }
    }
}
