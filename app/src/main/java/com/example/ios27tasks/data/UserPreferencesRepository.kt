package com.example.ios27tasks.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ios27tasks.TaskTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ios27_spatial_prefs")

@Immutable
data class SpatialUserPreferences(
    val isDarkMode: Boolean = true,
    val selectedTab: TaskTab = TaskTab.WORK,
    val supabaseUrl: String = DEFAULT_SUPABASE_URL,
    val supabaseAnonKey: String = DEFAULT_SUPABASE_KEY,
    val supabaseHubId: String = DEFAULT_HUB_ID,
    val autoSyncEnabled: Boolean = true,
    val lastSyncedAt: Long = 0L,
    val hasCompletedOnboarding: Boolean = false,
    val googleAccountEmail: String? = null,
    val googleAccountName: String? = null,
    val googleAccountPhotoUrl: String? = null
) {
    companion object {
        const val DEFAULT_SUPABASE_URL = "https://hyoveowiisbigcpxzoro.supabase.co"
        const val DEFAULT_SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh5b3Zlb3dpaXNiaWdjcHh6b3JvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODY5MTg2OTcsImV4cCI6MjEwMjQ5NDY5N30.FFHC_1r0d2buwOgElxp99RP10NrmjrOiE74F0zTW1T4"
        const val DEFAULT_HUB_ID = "hospitality_tasks_hub_v1"
    }
}

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SELECTED_TAB = stringPreferencesKey("selected_tab")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
        val SUPABASE_HUB_ID = stringPreferencesKey("supabase_hub_id")
        val AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val GOOGLE_ACCOUNT_EMAIL = stringPreferencesKey("google_account_email")
        val GOOGLE_ACCOUNT_NAME = stringPreferencesKey("google_account_name")
        val GOOGLE_ACCOUNT_PHOTO_URL = stringPreferencesKey("google_account_photo_url")
    }

    val preferencesFlow: Flow<SpatialUserPreferences> = context.dataStore.data.map { prefs ->
        val tabStr = prefs[Keys.SELECTED_TAB] ?: TaskTab.WORK.name
        val tab = runCatching { TaskTab.valueOf(tabStr) }.getOrDefault(TaskTab.WORK)
        SpatialUserPreferences(
            isDarkMode = prefs[Keys.IS_DARK_MODE] ?: true,
            selectedTab = tab,
            supabaseUrl = prefs[Keys.SUPABASE_URL] ?: SpatialUserPreferences.DEFAULT_SUPABASE_URL,
            supabaseAnonKey = prefs[Keys.SUPABASE_ANON_KEY] ?: SpatialUserPreferences.DEFAULT_SUPABASE_KEY,
            supabaseHubId = prefs[Keys.SUPABASE_HUB_ID] ?: SpatialUserPreferences.DEFAULT_HUB_ID,
            autoSyncEnabled = prefs[Keys.AUTO_SYNC_ENABLED] ?: true,
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L,
            hasCompletedOnboarding = prefs[Keys.HAS_COMPLETED_ONBOARDING] ?: false,
            googleAccountEmail = prefs[Keys.GOOGLE_ACCOUNT_EMAIL],
            googleAccountName = prefs[Keys.GOOGLE_ACCOUNT_NAME],
            googleAccountPhotoUrl = prefs[Keys.GOOGLE_ACCOUNT_PHOTO_URL]
        )
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun setSelectedTab(tab: TaskTab) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_TAB] = tab.name
        }
    }

    suspend fun updateSupabaseConfig(
        url: String,
        anonKey: String,
        hubId: String,
        autoSync: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUPABASE_URL] = url.trim().trimEnd('/')
            prefs[Keys.SUPABASE_ANON_KEY] = anonKey.trim()
            prefs[Keys.SUPABASE_HUB_ID] = hubId.trim().ifBlank { SpatialUserPreferences.DEFAULT_HUB_ID }
            prefs[Keys.AUTO_SYNC_ENABLED] = autoSync
        }
    }

    suspend fun recordSyncSuccess(timestampMillis: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNCED_AT] = timestampMillis
        }
    }

    suspend fun setCompletedOnboarding(completed: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setGoogleAccount(email: String?, displayName: String?, photoUrl: String?) {
        context.dataStore.edit { prefs ->
            if (email != null) {
                prefs[Keys.GOOGLE_ACCOUNT_EMAIL] = email
            } else {
                prefs.remove(Keys.GOOGLE_ACCOUNT_EMAIL)
            }
            if (displayName != null) {
                prefs[Keys.GOOGLE_ACCOUNT_NAME] = displayName
            } else {
                prefs.remove(Keys.GOOGLE_ACCOUNT_NAME)
            }
            if (photoUrl != null) {
                prefs[Keys.GOOGLE_ACCOUNT_PHOTO_URL] = photoUrl
            } else {
                prefs.remove(Keys.GOOGLE_ACCOUNT_PHOTO_URL)
            }
        }
    }

    suspend fun unlinkGoogleAccount() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.GOOGLE_ACCOUNT_EMAIL)
            prefs.remove(Keys.GOOGLE_ACCOUNT_NAME)
            prefs.remove(Keys.GOOGLE_ACCOUNT_PHOTO_URL)
        }
    }
}
