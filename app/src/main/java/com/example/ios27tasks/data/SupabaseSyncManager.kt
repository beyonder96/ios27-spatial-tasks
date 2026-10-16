package com.example.ios27tasks.data

import android.util.Log
import com.example.ios27tasks.TaskTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE
}

class SupabaseSyncManager(
    private val taskDao: TaskDao,
    private val prefsRepo: UserPreferencesRepository
) {
    private val tag = "SupabaseSyncManager"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Pushes the current Room tasks snapshot to Supabase `shared_tasks_hub`
     */
    suspend fun pushTasksToCloud(prefs: SpatialUserPreferences): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = prefs.supabaseUrl.trim().trimEnd('/')
        val apiKey = prefs.supabaseAnonKey.trim()
        val hubId = prefs.supabaseHubId.trim().ifBlank { SpatialUserPreferences.DEFAULT_HUB_ID }
        if (baseUrl.isBlank() || apiKey.isBlank() || baseUrl.contains("hyoveowiisbigcpxzoro")) return@withContext false

        try {
            val localTasks = taskDao.getAllTasksSnapshot()
            val deletedIds = taskDao.getAllDeletedIds().toSet()
            val activeTasks = localTasks.filter { it.id !in deletedIds }

            val itemsArray = JSONArray()
            activeTasks.forEach { task ->
                val obj = JSONObject().apply {
                    put("id", task.id)
                    put("title", task.title)
                    put("description", task.tag)
                    put("tag", task.tag)
                    put("tab", task.tabName)
                    put("type", if (task.tab == TaskTab.WORK) "work" else "personal")
                    put("target_user", if (task.tab == TaskTab.WORK) "work" else "persona")
                    put("status", if (task.isCompleted) "completed" else "active")
                    put("completed", task.isCompleted)
                    put("created_by", "Hospitality Care App")
                    put("created_at", task.createdAt)
                    task.completedAt?.let { put("completed_at", it) }
                }
                itemsArray.put(obj)
            }

            val isoNow = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            val payload = JSONObject().apply {
                put("id", hubId)
                put("title", "iOS 27 Spatial Tasks — Hotelaria Hospitalar (Persona & Work)")
                put("items", itemsArray)
                put("updated_at", isoNow)
            }

            val url = "$baseUrl/rest/v1/shared_tasks_hub?on_conflict=id"
            val body = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    prefsRepo.recordSyncSuccess()
                    return@withContext true
                } else {
                    Log.w(tag, "Supabase push HTTP ${response.code}")
                    return@withContext false
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Supabase push failed (working offline with Room): ${e.message}")
            return@withContext false
        }
    }

    /**
     * Pulls remote tasks from Supabase `shared_tasks_hub` and merges into Room Database.
     * Prevents any tombstoned/deleted tasks from being resurrected.
     */
    suspend fun pullAndMergeFromCloud(prefs: SpatialUserPreferences): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = prefs.supabaseUrl.trim().trimEnd('/')
        val apiKey = prefs.supabaseAnonKey.trim()
        val hubId = prefs.supabaseHubId.trim().ifBlank { SpatialUserPreferences.DEFAULT_HUB_ID }
        if (baseUrl.isBlank() || apiKey.isBlank() || baseUrl.contains("hyoveowiisbigcpxzoro")) return@withContext false

        try {
            val url = "$baseUrl/rest/v1/shared_tasks_hub?id=eq.$hubId&select=*"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(tag, "Supabase pull HTTP ${response.code}")
                    return@withContext false
                }
                val rawBody = response.body?.string().orEmpty()
                if (rawBody.isBlank() || rawBody == "[]") {
                    return@withContext pushTasksToCloud(prefs)
                }

                val rootObj = if (rawBody.trimStart().startsWith("[")) {
                    JSONArray(rawBody).optJSONObject(0) ?: JSONObject()
                } else {
                    JSONObject(rawBody)
                }

                val itemsArray = rootObj.optJSONArray("items") ?: JSONArray()
                if (itemsArray.length() == 0) {
                    val localTasks = taskDao.getAllTasksSnapshot()
                    if (localTasks.isNotEmpty()) {
                        return@withContext pushTasksToCloud(prefs)
                    } else {
                        prefsRepo.recordSyncSuccess()
                        return@withContext true
                    }
                }

                // Recupera todos os IDs de tarefas excluídas localmente
                val deletedIds = taskDao.getAllDeletedIds().toSet()

                val parsedTasks = mutableListOf<TaskEntity>()
                var foundTombstonedInRemote = false

                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.optJSONObject(i) ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue

                    // Se foi excluída pelo usuário, JAMAIS ressuscita!
                    if (deletedIds.contains(id)) {
                        foundTombstonedInRemote = true
                        continue
                    }

                    val title = item.optString("title").takeIf { it.isNotBlank() } ?: continue

                    val explicitTab = item.optString("tab")
                    val inferredTab = when {
                        explicitTab == TaskTab.WORK.name || explicitTab == TaskTab.PERSONA.name -> explicitTab
                        item.optString("type") == "work" -> TaskTab.WORK.name
                        else -> TaskTab.WORK.name
                    }

                    val tagValue = item.optString("tag").ifBlank {
                        item.optString("description").ifBlank {
                            if (inferredTab == TaskTab.PERSONA.name) "Autocuidado" else "Governança & Leitos"
                        }
                    }

                    val isCompleted = item.optBoolean("completed", false) ||
                        item.optString("status") == "completed"

                    parsedTasks.add(
                        TaskEntity(
                            id = id,
                            title = title,
                            tag = tagValue,
                            isCompleted = isCompleted,
                            tabName = inferredTab,
                            createdAt = item.optLong("created_at", System.currentTimeMillis()),
                            completedAt = if (item.has("completed_at") && !item.isNull("completed_at")) {
                                item.optLong("completed_at")
                            } else null
                        )
                    )
                }

                if (parsedTasks.isNotEmpty()) {
                    taskDao.upsertAll(parsedTasks)
                }

                // Se a nuvem continha tarefas que foram excluídas neste dispositivo, limpa a nuvem
                if (foundTombstonedInRemote) {
                    pushTasksToCloud(prefs)
                }

                prefsRepo.recordSyncSuccess()
                return@withContext true
            }
        } catch (e: Exception) {
            Log.w(tag, "Supabase pull failed (working offline with Room): ${e.message}")
            return@withContext false
        }
    }
}
