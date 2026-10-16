package com.example.ios27tasks.data

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ios27tasks.TaskTab
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Immutable
@Entity(tableName = "spatial_tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val tag: String,
    val isCompleted: Boolean = false,
    val tabName: String = TaskTab.WORK.name,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val tab: TaskTab
        get() = runCatching { TaskTab.valueOf(tabName) }.getOrDefault(TaskTab.WORK)
}

@Immutable
@Entity(tableName = "deleted_task_tombstones")
data class DeletedTaskTombstone(
    @PrimaryKey
    val taskId: String,
    val deletedAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM spatial_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM spatial_tasks ORDER BY createdAt DESC")
    suspend fun getAllTasksSnapshot(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM spatial_tasks")
    suspend fun countTasks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM spatial_tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("DELETE FROM spatial_tasks WHERE isCompleted = 1 AND tabName = :tabName")
    suspend fun clearCompletedByTab(tabName: String)

    // Tombstone Management (Garante que tarefas excluídas nunca ressuscitem)
    @Query("SELECT taskId FROM deleted_task_tombstones")
    suspend fun getAllDeletedIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTombstone(tombstone: DeletedTaskTombstone)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTombstones(tombstones: List<DeletedTaskTombstone>)

    @Query("DELETE FROM deleted_task_tombstones WHERE taskId = :taskId")
    suspend fun removeTombstone(taskId: String)

    @Query("DELETE FROM deleted_task_tombstones")
    suspend fun clearAllTombstones()
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS deleted_task_tombstones (" +
            "taskId TEXT NOT NULL PRIMARY KEY, " +
            "deletedAt INTEGER NOT NULL DEFAULT 0)"
        )
    }
}

@Database(
    entities = [TaskEntity::class, DeletedTaskTombstone::class],
    version = 2,
    exportSchema = false
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): TaskDatabase {
            return Room.databaseBuilder(
                context,
                TaskDatabase::class.java,
                "ios27_hospitality_tasks.db"
            )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        fun defaultSeedTasks(): List<TaskEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                // Tarefas iniciais Work (Hotelaria Hospitalar & Governança)
                TaskEntity(
                    id = "seed_work_1",
                    title = "Auditoria pós-alta: Terminal do Leito 408 (Ala Cirúrgica)",
                    tag = "Higienização Terminal",
                    isCompleted = true,
                    tabName = TaskTab.WORK.name,
                    createdAt = now - 50_000L,
                    completedAt = now - 15_000L
                ),
                TaskEntity(
                    id = "seed_work_2",
                    title = "Checagem de enxoval e entrega de kits na UTI Adulto",
                    tag = "Rouparia & Enxoval",
                    isCompleted = false,
                    tabName = TaskTab.WORK.name,
                    createdAt = now - 40_000L
                ),
                TaskEntity(
                    id = "seed_work_3",
                    title = "Reposição de dispensers e insumos na Ala Pediátrica",
                    tag = "Governança & Leitos",
                    isCompleted = false,
                    tabName = TaskTab.WORK.name,
                    createdAt = now - 30_000L
                ),
                TaskEntity(
                    id = "seed_work_4",
                    title = "Validação de checklist com equipe de limpeza do 3º andar",
                    tag = "Passagem de Plantão",
                    isCompleted = false,
                    tabName = TaskTab.WORK.name,
                    createdAt = now - 20_000L
                ),
                TaskEntity(
                    id = "seed_work_5",
                    title = "Abertura de OS para reparo de ar condicionado no Quarto 215",
                    tag = "Manutenção & Chamados",
                    isCompleted = false,
                    tabName = TaskTab.WORK.name,
                    createdAt = now - 10_000L
                ),
                // Tarefas iniciais Persona (Autocuidado & Plantão)
                TaskEntity(
                    id = "seed_persona_1",
                    title = "Meta de hidratação: 1.5L de água até metade do plantão",
                    tag = "Hidratação & Saúde",
                    isCompleted = true,
                    tabName = TaskTab.PERSONA.name,
                    createdAt = now - 45_000L,
                    completedAt = now - 20_000L
                ),
                TaskEntity(
                    id = "seed_persona_2",
                    title = "Almoço tranquilo e pausa de descompressão (30 min)",
                    tag = "Pausa & Alimentação",
                    isCompleted = false,
                    tabName = TaskTab.PERSONA.name,
                    createdAt = now - 35_000L
                ),
                TaskEntity(
                    id = "seed_persona_3",
                    title = "Revisar novo POP (Procedimento Operacional Padrão) CCIH",
                    tag = "Estudos & Protocolos",
                    isCompleted = false,
                    tabName = TaskTab.PERSONA.name,
                    createdAt = now - 25_000L
                ),
                TaskEntity(
                    id = "seed_persona_4",
                    title = "Alongamento pós-ronda de leitos",
                    tag = "Autocuidado",
                    isCompleted = false,
                    tabName = TaskTab.PERSONA.name,
                    createdAt = now - 15_000L
                )
            )
        }
    }
}
