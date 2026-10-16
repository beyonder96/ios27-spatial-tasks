package com.example.ios27tasks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ios27tasks.data.CloudSyncStatus
import com.example.ios27tasks.data.SpatialUserPreferences
import com.example.ios27tasks.data.TaskEntity
import com.example.ios27tasks.viewmodel.SpatialTasksViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HospitalNotificationHelper.createNotificationChannels(this)
        enableEdgeToEdge()
        setContent {
            Ios27TaskApp()
        }
    }
}

// --- 1. MODELOS DE DADOS ---

enum class TaskTab(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentDark: Color,
    val accentLight: Color,
    val presetTags: List<String>
) {
    WORK(
        title = "Work",
        subtitle = "Hotelaria, governança de leitos & rotina assistencial",
        icon = Icons.Rounded.Apartment,
        accentDark = Color(0xFF00C7BE),  // Glacial Teal Hospitalar
        accentLight = Color(0xFF009688),
        presetTags = listOf(
            "Higienização Terminal",
            "Higienização Concorrente",
            "Rouparia & Enxoval",
            "Governança & Leitos",
            "Auditoria de Quarto",
            "Manutenção & Chamados",
            "Passagem de Plantão"
        )
    ),
    PERSONA(
        title = "Persona",
        subtitle = "Autocuidado, pausas no plantão & bem-estar",
        icon = Icons.Rounded.PersonOutline,
        accentDark = Color(0xFF6E6BF5),  // Electric Indigo iOS
        accentLight = Color(0xFF5856D6),
        presetTags = listOf(
            "Hidratação & Saúde",
            "Pausa & Alimentação",
            "Estudos & Protocolos",
            "Autocuidado",
            "Pessoal & Família"
        )
    )
}

// --- 2. PALETA E TEMA ESPACIAL iOS 27 ---

@Immutable
data class Ios27ThemeColors(
    val isDark: Boolean,
    val backgroundBase: Color,
    val glassSurface: Color,
    val glassElevated: Color,
    val borderHighlight: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val pillTrack: Color,
    val pillThumb: Color
)

@Composable
fun rememberIos27Colors(isDark: Boolean): Ios27ThemeColors {
    return remember(isDark) {
        if (isDark) {
            Ios27ThemeColors(
                isDark = true,
                backgroundBase = Color(0xFF07080C),
                glassSurface = Color(0xFF181B24).copy(alpha = 0.68f),
                glassElevated = Color(0xFF232734).copy(alpha = 0.82f),
                borderHighlight = Color.White.copy(alpha = 0.22f),
                borderSubtle = Color.White.copy(alpha = 0.05f),
                textPrimary = Color(0xFFF5F7FA),
                textSecondary = Color(0xFF8E95A5),
                pillTrack = Color(0xFF12141C).copy(alpha = 0.85f),
                pillThumb = Color(0xFF262B3A)
            )
        } else {
            Ios27ThemeColors(
                isDark = false,
                backgroundBase = Color(0xFFEEF2F7),
                glassSurface = Color.White.copy(alpha = 0.76f),
                glassElevated = Color.White.copy(alpha = 0.94f),
                borderHighlight = Color.White.copy(alpha = 0.95f),
                borderSubtle = Color(0xFFD0D7E2).copy(alpha = 0.45f),
                textPrimary = Color(0xFF11141C),
                textSecondary = Color(0xFF636B78),
                pillTrack = Color(0xFFDDE3EC).copy(alpha = 0.75f),
                pillThumb = Color.White
            )
        }
    }
}

/**
 * Modificador Liquid Glass avançado inspirado no iOS 27 / visionOS:
 * - Preenchimento multicamada com leve gradiente de brilho especular interno
 * - Chanfro de borda lapidada com gradiente linear em 3 paradas de luz
 * - Sombra ótica dupla (ambiente + especular direcional)
 * - Suporte a suave coloração (accentTint) líquida
 */
fun Modifier.ios27Glass(
    colors: Ios27ThemeColors,
    cornerRadius: Dp = 26.dp,
    elevated: Boolean = false,
    accentTint: Color? = null
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    val baseSurface = if (elevated) colors.glassElevated else colors.glassSurface
    val surfaceBrush = if (accentTint != null) {
        Brush.linearGradient(
            colors = listOf(
                accentTint.copy(alpha = if (colors.isDark) 0.24f else 0.16f),
                baseSurface
            ),
            start = Offset(0f, 0f),
            end = Offset(320f, 320f)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                baseSurface.copy(alpha = (baseSurface.alpha + 0.08f).coerceAtMost(1f)),
                baseSurface
            )
        )
    }

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            colors.borderHighlight,
            colors.borderHighlight.copy(alpha = 0.35f),
            colors.borderSubtle
        ),
        start = Offset(0f, 0f),
        end = Offset(450f, 450f)
    )

    return this
        .shadow(
            elevation = if (colors.isDark) 16.dp else 10.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (colors.isDark) 0.55f else 0.08f),
            spotColor = Color.Black.copy(alpha = if (colors.isDark) 0.45f else 0.12f)
        )
        .clip(shape)
        .background(surfaceBrush)
        .border(
            width = 1.dp,
            brush = borderBrush,
            shape = shape
        )
}

// --- 3. TELA PRINCIPAL ---

@Composable
fun Ios27TaskApp(
    viewModel: SpatialTasksViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val lastDeletedTask by viewModel.lastDeletedTask.collectAsStateWithLifecycle()

    val isDarkMode = prefs.isDarkMode
    val selectedTab = prefs.selectedTab

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskTime by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember(selectedTab) { mutableStateOf(selectedTab.presetTags.first()) }
    var showCloudConfigDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showOnboardingTutorial by remember { mutableStateOf(false) }
    var showWhatsAppShareDialog by remember { mutableStateOf(false) }
    var showAiPlannerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var taskBeingEditedForTime by remember { mutableStateOf<TaskEntity?>(null) }

    var hasNotificationPermission by remember {
        mutableStateOf(HospitalNotificationHelper.hasNotificationPermission(context))
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notificações do plantão ativadas com sucesso!", Toast.LENGTH_SHORT).show()
            HospitalNotificationHelper.sendTestNotification(context)
        } else {
            Toast.makeText(context, "Permissão de notificações não concedida.", Toast.LENGTH_SHORT).show()
        }
    }

    val colors = rememberIos27Colors(isDarkMode)

    // Apresentação de Primeiro Uso / Tutorial Espacial iOS 27
    if (!prefs.hasCompletedOnboarding || showOnboardingTutorial) {
        HospitalityOnboardingScreen(
            colors = colors,
            initialEmail = prefs.googleAccountEmail,
            initialDisplayName = prefs.googleAccountName,
            onFinish = {
                if (showOnboardingTutorial) {
                    showOnboardingTutorial = false
                } else {
                    viewModel.completeOnboarding()
                }
            },
            onLinkGoogle = { email, displayName ->
                viewModel.linkGoogleAccount(email, displayName, null)
                showOnboardingTutorial = false
            }
        )
        return
    }

    val activeAccent by animateColorAsState(
        targetValue = if (isDarkMode) selectedTab.accentDark else selectedTab.accentLight,
        animationSpec = tween(400),
        label = "AccentColor"
    )

    val filteredTasks by remember(tasks, selectedTab) {
        derivedStateOf { tasks.filter { it.tab == selectedTab } }
    }
    val completedCount by remember(filteredTasks) {
        derivedStateOf { filteredTasks.count { it.isCompleted } }
    }
    val totalCount by remember(filteredTasks) {
        derivedStateOf { filteredTasks.size }
    }
    val progress by remember(completedCount, totalCount) {
        derivedStateOf { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f }
    }
    val personaPendingCount by remember(tasks) {
        derivedStateOf { tasks.count { it.tab == TaskTab.PERSONA && !it.isCompleted } }
    }
    val workPendingCount by remember(tasks) {
        derivedStateOf { tasks.count { it.tab == TaskTab.WORK && !it.isCompleted } }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
    ) {
        // Brilho ambiental dinâmico de fundo (Spatial Aura iOS 27)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            activeAccent.copy(alpha = if (isDarkMode) 0.22f else 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(x = 500f, y = 250f),
                        radius = 950f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Cabeçalho Espacial Clean com Menu Suspenso (Dropdown)
            HeaderBar(
                selectedTab = selectedTab,
                colors = colors,
                activeAccent = activeAccent,
                syncStatus = syncStatus,
                googleAccountEmail = prefs.googleAccountEmail,
                googleAccountName = prefs.googleAccountName,
                onSyncClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.syncWithCloud()
                },
                onOpenSyncSettings = { showCloudConfigDialog = true },
                onOpenAccountProfile = { showAccountDialog = true },
                onShareWhatsApp = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showWhatsAppShareDialog = true
                },
                onOpenAiPlanner = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAiPlannerDialog = true
                },
                onSortByTime = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.sortByScheduledTime(selectedTab)
                    Toast.makeText(context, "Tarefas ordenadas por horário de plantão!", Toast.LENGTH_SHORT).show()
                },
                onToggleTheme = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.toggleTheme()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pílula Deslizante (Work <-> Persona)
            SlidingPillTabs(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.selectTab(tab)
                },
                colors = colors,
                activeAccent = activeAccent,
                workCount = workPendingCount,
                personaCount = personaPendingCount
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Card Espacial de Progresso do Contexto com Botão de Compartilhar no WhatsApp
            ProgressSummaryPill(
                completed = completedCount,
                total = totalCount,
                progress = progress,
                activeAccent = activeAccent,
                colors = colors,
                onShareWhatsApp = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showWhatsAppShareDialog = true
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Lista de Checklist com Gestos Swipe, Reordenação ⬆️/⬇️ e Horários ⏰
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = filteredTasks,
                    key = { it.id }
                ) { task ->
                    SwipeableChecklistPillRow(
                        task = task,
                        colors = colors,
                        activeAccent = activeAccent,
                        onToggle = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleTaskCompleted(task)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteTask(task)
                        },
                        onMoveUp = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.moveTaskUp(task)
                        },
                        onMoveDown = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.moveTaskDown(task)
                        },
                        onEditTime = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            taskBeingEditedForTime = task
                            showTimePickerDialog = true
                        },
                        onShareWhatsApp = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            launchWhatsAppShare(context, formatSingleTaskWhatsAppText(task))
                        }
                    )
                }
            }

            // Pílula Flutuante de Desfazer Exclusão (Undo)
            AnimatedVisibility(
                visible = lastDeletedTask != null,
                enter = fadeIn(tween(180)) + slideInVertically { it / 2 },
                exit = fadeOut(tween(150)) + slideOutVertically { it / 2 }
            ) {
                lastDeletedTask?.let { removed ->
                    FloatingUndoPill(
                        taskTitle = removed.title,
                        colors = colors,
                        activeAccent = activeAccent,
                        onUndo = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.undoDelete()
                        }
                    )
                }
            }

            // Pílula de Ação Rápida: Planejar com IA (Gemini 3.8 Flash)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 18.dp, elevated = true)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showAiPlannerDialog = true
                    }
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD60A).copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD60A),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Planejar rotina do plantão com IA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                }
                Text(
                    text = "Gemini 3.8 Flash ✨",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra Flutuante em Pílula para Adicionar Nova Tarefa com Horário
            FloatingAddTaskPill(
                value = newTaskTitle,
                onValueChange = { newTaskTitle = it },
                selectedTab = selectedTab,
                selectedTag = selectedTag,
                scheduledTime = newTaskTime,
                onPickTime = {
                    taskBeingEditedForTime = null
                    showTimePickerDialog = true
                },
                onClearTime = {
                    newTaskTime = null
                },
                colors = colors,
                activeAccent = activeAccent,
                onAdd = {
                    if (newTaskTitle.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addTask(
                            title = newTaskTitle,
                            tag = selectedTag,
                            tab = selectedTab,
                            scheduledTime = newTaskTime
                        )
                        newTaskTitle = ""
                        newTaskTime = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showCloudConfigDialog) {
            SupabaseConfigDialog(
                prefs = prefs,
                activeAccent = activeAccent,
                onDismiss = { showCloudConfigDialog = false },
                onSave = { url, key, hubId, autoSync ->
                    viewModel.saveSupabaseSettings(url, key, hubId, autoSync)
                    showCloudConfigDialog = false
                }
            )
        }

        if (showAccountDialog) {
            AccountProfileDialog(
                googleEmail = prefs.googleAccountEmail,
                googleName = prefs.googleAccountName,
                geminiApiKey = prefs.geminiApiKey,
                activeAccent = activeAccent,
                hasNotificationPermission = hasNotificationPermission,
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotificationPermission = HospitalNotificationHelper.hasNotificationPermission(context)
                        if (hasNotificationPermission) {
                            HospitalNotificationHelper.sendTestNotification(context)
                            Toast.makeText(context, "Notificações de plantão já estão ativas!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Ative as notificações nas configurações do aparelho.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onSendTestNotification = {
                    val sent = HospitalNotificationHelper.sendTestNotification(context)
                    if (sent) {
                        Toast.makeText(context, "Notificação de teste enviada! Verifique a barra de status.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Permissão necessária para exibir notificações.", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { showAccountDialog = false },
                onReviewTutorial = {
                    showAccountDialog = false
                    showOnboardingTutorial = true
                },
                onConfigureGemini = {
                    showAccountDialog = false
                    showAiPlannerDialog = true
                },
                onUnlinkAccount = {
                    viewModel.unlinkGoogleAccount()
                    showAccountDialog = false
                },
                onLinkAccount = {
                    showAccountDialog = false
                    showOnboardingTutorial = true
                }
            )
        }

        if (showWhatsAppShareDialog) {
            WhatsAppShareDialog(
                tab = selectedTab,
                tasks = filteredTasks,
                colors = colors,
                activeAccent = activeAccent,
                onDismiss = { showWhatsAppShareDialog = false },
                onSendWhatsApp = { text ->
                    launchWhatsAppShare(context, text)
                    showWhatsAppShareDialog = false
                },
                onCopyText = { text ->
                    copyToClipboard(context, text)
                    Toast.makeText(context, "Texto copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showAiPlannerDialog) {
            AiPlannerDialog(
                currentApiKey = prefs.geminiApiKey,
                colors = colors,
                activeAccent = activeAccent,
                onSaveApiKey = { key ->
                    viewModel.setGeminiApiKey(key)
                },
                onGenerate = { prompt ->
                    viewModel.planRoutineWithAi(prompt)
                },
                onAddPlannedTasks = { planned ->
                    viewModel.addPlannedTasks(planned)
                    if (HospitalNotificationHelper.hasNotificationPermission(context)) {
                        HospitalNotificationHelper.sendHospitalNotification(
                            context = context,
                            title = "✨ Roteiro IA Adicionado ao Plantão",
                            message = "${planned.size} rotinas estruturadas foram organizadas na sua lista de tarefas.",
                            notificationId = 1003
                        )
                    }
                },
                onDismiss = { showAiPlannerDialog = false }
            )
        }

        if (showTimePickerDialog) {
            TaskTimePickerDialog(
                initialTime = taskBeingEditedForTime?.scheduledTime ?: newTaskTime,
                colors = colors,
                activeAccent = activeAccent,
                onDismiss = {
                    showTimePickerDialog = false
                    taskBeingEditedForTime = null
                },
                onConfirmTime = { time ->
                    val editing = taskBeingEditedForTime
                    if (editing != null) {
                        viewModel.updateTaskTime(editing, time)
                    } else {
                        newTaskTime = time
                    }
                    showTimePickerDialog = false
                    taskBeingEditedForTime = null
                }
            )
        }
    }
}

// --- 4. COMPONENTES VISUAIS ---

@Composable
fun HeaderBar(
    selectedTab: TaskTab,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    syncStatus: CloudSyncStatus,
    googleAccountEmail: String? = null,
    googleAccountName: String? = null,
    onSyncClick: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenAccountProfile: () -> Unit,
    onShareWhatsApp: () -> Unit = {},
    onOpenAiPlanner: () -> Unit = {},
    onSortByTime: () -> Unit = {},
    onToggleTheme: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activeAccent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HOTELARIA HOSPITALAR",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "HeaderTitle"
            ) { tab ->
                Column {
                    Text(
                        text = tab.title,
                        color = colors.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp
                    )
                    Text(
                        text = tab.subtitle,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botão Pílula de Modo Claro / Escuro (Compacto)
            Row(
                modifier = Modifier
                    .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                    .clickable(onClick = onToggleTheme)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (colors.isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = "Alternar tema",
                    tint = activeAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Menu Suspenso Espacial iOS 27 (Dropdown Menu)
            Box {
                Row(
                    modifier = Modifier
                        .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                        .clickable { menuExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Menu de Ações",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(colors.glassElevated)
                        .border(1.dp, colors.borderHighlight, RoundedCornerShape(16.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD60A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Planejar com IA (Gemini 3.8)",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenAiPlanner()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = null,
                                    tint = Color(0xFF25D366),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Compartilhar no WhatsApp",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onShareWhatsApp()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9F0A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Ordenar por Horário de Plantão",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onSortByTime()
                        }
                    )

                    HorizontalDivider(color = colors.borderSubtle.copy(alpha = 0.5f))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = activeAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (!googleAccountEmail.isNullOrBlank()) "Perfil: $googleAccountEmail" else "Perfil & Alertas",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenAccountProfile()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Nuvem Própria (Opcional)",
                                    color = colors.textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenSyncSettings()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Seletor de Abas em Pílulas Deslizantes (Work vs Persona) com física de mola
 */
@Composable
fun SlidingPillTabs(
    selectedTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    workCount: Int,
    personaCount: Int
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .ios27Glass(colors = colors, cornerRadius = 50.dp)
            .padding(4.dp)
    ) {
        val segmentWidth = maxWidth / 2
        val indicatorOffset by animateDpAsState(
            targetValue = if (selectedTab == TaskTab.WORK) 0.dp else segmentWidth,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "PillSlideOffset"
        )

        // Pílula Indicadora Deslizante (Thumb)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50.dp),
                    ambientColor = activeAccent.copy(alpha = 0.4f),
                    spotColor = activeAccent.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(50.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            activeAccent.copy(alpha = if (colors.isDark) 0.35f else 0.18f),
                            colors.pillThumb
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = activeAccent.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(50.dp)
                )
        )

        // Botões das Abas
        Row(modifier = Modifier.fillMaxSize()) {
            TaskTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val count = if (tab == TaskTab.WORK) workCount else personaCount
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) colors.textPrimary else colors.textSecondary,
                    label = "TabContentColor"
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) activeAccent else contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tab.title,
                        color = contentColor,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Pílula contadora de pendências
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) activeAccent else colors.textSecondary.copy(alpha = 0.18f)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (isSelected) Color.White else contentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryPill(
    completed: Int,
    total: Int,
    progress: Float,
    activeAccent: Color,
    colors: Ios27ThemeColors,
    onShareWhatsApp: () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "ProgressValue"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .ios27Glass(colors = colors, cornerRadius = 24.dp)
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Checklist concluído",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$completed de $total (${(animatedProgress * 100).toInt()}%)",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF25D366).copy(alpha = 0.16f))
                        .clickable(onClick = onShareWhatsApp)
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = "Compartilhar no WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WhatsApp",
                        color = Color(0xFF25D366),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(colors.textSecondary.copy(alpha = 0.16f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(activeAccent, activeAccent.copy(alpha = 0.7f))
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Deslize → concluir/reabrir • ← excluir • Hotelaria Hospitalar",
            color = colors.textSecondary.copy(alpha = 0.72f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Linha de Checklist em Pílula com suporte a Gestos Táteis:
 * - Swipe para a direita (→): Alterna concluído/pendente (Swipe-to-Complete) com física de mola
 * - Swipe para a esquerda (←): Exclui a tarefa (Swipe-to-Delete) com opção de desfazer
 */
@Composable
fun SwipeableChecklistPillRow(
    task: TaskEntity,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onEditTime: () -> Unit = {},
    onShareWhatsApp: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { 96.dp.toPx() } }
    val maxDragPx = remember(density) { with(density) { 150.dp.toPx() } }

    val offsetX = remember(task.id) { Animatable(0f) }
    var thresholdCrossedHaptic by remember(task.id) { mutableStateOf(false) }

    val checkScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "CheckScale"
    )

    val swipeProgress = (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1.25f)
    val isSwipingRight = offsetX.value > 8f
    val isSwipingLeft = offsetX.value < -8f

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Camada traseira revelada durante o gesto de Swipe
        if (isSwipingRight || isSwipingLeft) {
            val actionColor = if (isSwipingRight) {
                if (task.isCompleted) activeAccent else Color(0xFF30D158)
            } else {
                Color(0xFFFF453A)
            }

            Row(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        actionColor.copy(alpha = (0.18f + 0.35f * swipeProgress).coerceAtMost(0.55f))
                    )
                    .border(
                        width = 1.dp,
                        color = actionColor.copy(alpha = (0.4f * swipeProgress).coerceAtMost(0.8f)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 22.dp),
                horizontalArrangement = if (isSwipingRight) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconScale = (0.75f + 0.35f * swipeProgress).coerceAtMost(1.18f)
                if (isSwipingRight) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.AutoMirrored.Rounded.Undo else Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (task.isCompleted) "Reabrir" else "Concluir",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Excluir",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .scale(iconScale)
                            .size(22.dp)
                    )
                }
            }
        }

        // Pílula Frontal Deslizante
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .ios27Glass(colors = colors, cornerRadius = 24.dp)
                .pointerInput(task.id, task.isCompleted) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            thresholdCrossedHaptic = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val nextVal = (offsetX.value + dragAmount).coerceIn(-maxDragPx, maxDragPx)
                            val crossedNow = abs(nextVal) >= thresholdPx
                            if (crossedNow && !thresholdCrossedHaptic) {
                                thresholdCrossedHaptic = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            } else if (!crossedNow && thresholdCrossedHaptic) {
                                thresholdCrossedHaptic = false
                            }
                            scope.launch {
                                offsetX.snapTo(nextVal)
                            }
                        },
                        onDragEnd = {
                            val finalOffset = offsetX.value
                            when {
                                finalOffset >= thresholdPx -> {
                                    onToggle()
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                }
                                finalOffset <= -thresholdPx -> {
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = -maxDragPx * 1.4f,
                                            animationSpec = tween(150)
                                        )
                                        onDelete()
                                        offsetX.snapTo(0f)
                                    }
                                }
                                else -> {
                                    scope.launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    )
                }
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Circular estilo iOS 27
            Box(
                modifier = Modifier
                    .scale(checkScale)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) activeAccent else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) activeAccent else colors.textSecondary.copy(alpha = 0.45f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Concluído",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.tag,
                        color = activeAccent.copy(alpha = if (task.isCompleted) 0.5f else 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Chip de Horário de Plantão
                    if (!task.scheduledTime.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(0xFFFF9F0A).copy(alpha = 0.16f))
                                .border(
                                    1.dp,
                                    Color(0xFFFF9F0A).copy(alpha = 0.45f),
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable(onClick = onEditTime)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Editar Horário",
                                tint = Color(0xFFFF9F0A),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = task.scheduledTime,
                                color = Color(0xFFFF9F0A),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(colors.textSecondary.copy(alpha = 0.12f))
                                .clickable(onClick = onEditTime)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Adicionar Horário",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+ Horário",
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Botões Táteis de Reordenação (⬆️ / ⬇️)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onMoveUp,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Subir tarefa",
                        tint = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Descer tarefa",
                        tint = colors.textSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                onClick = onShareWhatsApp,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Compartilhar tarefa no WhatsApp",
                    tint = Color(0xFF25D366).copy(alpha = 0.85f),
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Remover tarefa",
                    tint = colors.textSecondary.copy(alpha = 0.65f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingUndoPill(
    taskTitle: String,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onUndo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Removida: \"$taskTitle\"",
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(activeAccent.copy(alpha = 0.2f))
                .clickable(onClick = onUndo)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Desfazer",
                tint = activeAccent,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "Desfazer",
                color = activeAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickTagSelectorRow(
    tags: List<String>,
    selectedTag: String,
    onTagSelected: (String) -> Unit,
    colors: Ios27ThemeColors,
    activeAccent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.forEach { tag ->
            val isSelected = tag == selectedTag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (isSelected) activeAccent.copy(alpha = if (colors.isDark) 0.28f else 0.18f)
                        else colors.glassSurface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) activeAccent else colors.borderSubtle,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { onTagSelected(tag) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tag,
                    color = if (isSelected) activeAccent else colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FloatingAddTaskPill(
    value: String,
    onValueChange: (String) -> Unit,
    selectedTab: TaskTab,
    selectedTag: String,
    scheduledTime: String? = null,
    onPickTime: () -> Unit = {},
    onClearTime: () -> Unit = {},
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chip de horário configurado ou botão para escolher horário
        if (!scheduledTime.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFFF9F0A).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFFFF9F0A).copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                    .clickable(onClick = onPickTime)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = "Horário da Tarefa",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = scheduledTime,
                    color = Color(0xFFFF9F0A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Limpar horário",
                    tint = Color(0xFFFF9F0A),
                    modifier = Modifier
                        .size(12.dp)
                        .clickable(onClick = onClearTime)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colors.textSecondary.copy(alpha = 0.12f))
                    .clickable(onClick = onPickTime),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = "Definir Horário",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Nova tarefa em ${selectedTab.title}...",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(activeAccent),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(activeAccent)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Adicionar",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Diálogo Espacial Liquid Glass para Seleção de Horário de Rotina e Plantão
 */
@Composable
fun TaskTimePickerDialog(
    initialTime: String? = null,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onDismiss: () -> Unit,
    onConfirmTime: (String?) -> Unit
) {
    val quickPresets = listOf(
        "07:00", "09:00", "11:00", "13:00",
        "15:00", "17:00", "19:00", "21:00"
    )

    var manualTime by remember(initialTime) {
        mutableStateOf(initialTime ?: "07:00")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9F0A).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFFF9F0A),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Horário da Tarefa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Rotina de Plantão e Assistência",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Selecione um horário rápido de turno:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val chunked = quickPresets.chunked(4)
                    chunked.forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPresets.forEach { preset ->
                                val isSelected = manualTime == preset
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFF9F0A).copy(alpha = 0.25f)
                                            else colors.pillTrack
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(0xFFFF9F0A) else colors.borderSubtle,
                                            RoundedCornerShape(50.dp)
                                        )
                                        .clickable { manualTime = preset }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset,
                                        color = if (isSelected) Color(0xFFFF9F0A) else colors.textPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Ou digite o horário (HH:mm):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )

                OutlinedTextField(
                    value = manualTime,
                    onValueChange = { input ->
                        if (input.length <= 5) {
                            manualTime = input
                        }
                    },
                    label = { Text("Horário (ex: 14:30)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sanitized = manualTime.trim()
                    if (sanitized.isNotBlank()) {
                        onConfirmTime(sanitized)
                    } else {
                        onConfirmTime(null)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A))
            ) {
                Text("Confirmar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (initialTime != null) {
                    TextButton(onClick = { onConfirmTime(null) }) {
                        Text("Remover", color = Color(0xFFFF453A))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun SupabaseConfigDialog(
    prefs: SpatialUserPreferences,
    activeAccent: Color,
    onDismiss: () -> Unit,
    onSave: (url: String, key: String, hubId: String, autoSync: Boolean) -> Unit
) {
    var url by remember(prefs) { mutableStateOf(prefs.supabaseUrl) }
    var key by remember(prefs) { mutableStateOf(prefs.supabaseAnonKey) }
    var hubId by remember(prefs) { mutableStateOf(prefs.supabaseHubId) }
    var autoSync by remember(prefs) { mutableStateOf(prefs.autoSyncEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sincronização Hotelaria (Independente)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "As tarefas são salvas no Room local e sincronizadas com o hub exclusivo de hotelaria no Supabase (hospitality_tasks_hub_v1), 100% isolado do Tessera.",
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = hubId,
                    onValueChange = { hubId = it },
                    label = { Text("Hub ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Supabase URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Supabase Anon Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sincronização automática",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = autoSync,
                        onCheckedChange = { autoSync = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(url, key, hubId, autoSync) },
                colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
            ) {
                Text("Salvar e Sincronizar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

/**
 * Diálogo de Gestão de Perfil da Usuária e Conta Google Vinculada
 */
@Composable
fun AccountProfileDialog(
    googleEmail: String?,
    googleName: String?,
    geminiApiKey: String = "",
    activeAccent: Color,
    hasNotificationPermission: Boolean = false,
    onRequestNotificationPermission: () -> Unit = {},
    onSendTestNotification: () -> Unit = {},
    onDismiss: () -> Unit,
    onReviewTutorial: () -> Unit,
    onConfigureGemini: () -> Unit = {},
    onUnlinkAccount: () -> Unit,
    onLinkAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    tint = activeAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Perfil & Configurações",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!googleEmail.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(activeAccent.copy(alpha = 0.1f))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(activeAccent.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (googleName?.firstOrNull() ?: googleEmail.firstOrNull() ?: 'G').uppercase().toString(),
                                color = activeAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = googleName ?: "Plantonista",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = googleEmail,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = "Sua conta Google está vinculada para personalizar sua rotina no hospital.",
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "Nenhuma conta Google vinculada no momento. Você está utilizando o app em modo local/convidada.",
                        fontSize = 13.sp
                    )
                }

                // Card de Status das Notificações Android de Plantão
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (hasNotificationPermission) Color(0xFF30D158).copy(alpha = 0.12f) else Color(0xFFFF9F0A).copy(alpha = 0.12f))
                        .border(
                            width = 1.dp,
                            color = if (hasNotificationPermission) Color(0xFF30D158).copy(alpha = 0.35f) else Color(0xFFFF9F0A).copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (hasNotificationPermission) Color(0xFF30D158).copy(alpha = 0.2f) else Color(0xFFFF9F0A).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasNotificationPermission) Icons.Rounded.NotificationsActive else Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = if (hasNotificationPermission) Color(0xFF30D158) else Color(0xFFFF9F0A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasNotificationPermission) "Alertas de Plantão Ativos" else "Notificações Desativadas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (hasNotificationPermission) Color(0xFF30D158) else Color(0xFFFF9F0A)
                        )
                        Text(
                            text = if (hasNotificationPermission)
                                "Canal ativo para lembretes de rotinas e autocuidado."
                            else
                                "Ative para receber lembretes de rotinas e pausas de plantão.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (!hasNotificationPermission) {
                    Button(
                        onClick = onRequestNotificationPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9F0A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ativar Permissão de Notificações", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                } else {
                    Button(
                        onClick = onSendTestNotification,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158).copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Testar Notificação Agora",
                            color = Color(0xFF30D158),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Atalho de Configuração do Gemini IA
                Button(
                    onClick = onConfigureGemini,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (geminiApiKey.isNotBlank()) activeAccent.copy(alpha = 0.18f) else Color(0xFFFF9F0A).copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = if (geminiApiKey.isNotBlank()) activeAccent else Color(0xFFFF9F0A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (geminiApiKey.isNotBlank()) "Chave Gemini Ativa (••••${geminiApiKey.takeLast(4)})" else "Configurar Chave Gemini IA",
                        color = if (geminiApiKey.isNotBlank()) activeAccent else Color(0xFFFF9F0A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = onReviewTutorial,
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rever Apresentação / Tutorial", fontWeight = FontWeight.Bold)
                }

                if (!googleEmail.isNullOrBlank()) {
                    TextButton(
                        onClick = onUnlinkAccount,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF453A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Desvincular Conta Google", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onLinkAccount,
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vincular Conta Google", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// --- 5. COMPARTILHAMENTO NO WHATSAPP ---

enum class WhatsAppShareMode(val label: String) {
    ALL("Plantão Completo"),
    PENDING_ONLY("Só Pendências"),
    COMPLETED_ONLY("Só Concluídas")
}

fun formatWhatsAppTasksText(
    tab: TaskTab,
    tasks: List<TaskEntity>,
    filterMode: WhatsAppShareMode = WhatsAppShareMode.ALL
): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
    val now = dateFormat.format(Date())

    val pending = tasks.filter { !it.isCompleted }
    val completed = tasks.filter { it.isCompleted }
    val total = tasks.size
    val progressPercent = if (total > 0) (completed.size * 100) / total else 0

    fun formatTaskLine(task: TaskEntity, isDone: Boolean): String {
        val mark = if (isDone) "✔️" else "▫️"
        val timeBadge = if (!task.scheduledTime.isNullOrBlank()) "⏰ [${task.scheduledTime}] " else ""
        return "$mark $timeBadge*[${task.tag}]* ${task.title}"
    }

    val sb = StringBuilder()
    when (filterMode) {
        WhatsAppShareMode.ALL -> {
            sb.appendLine("🏨 *RELATÓRIO DE HOTELARIA HOSPITALAR*")
            sb.appendLine("🗓 *Data:* $now")
            sb.appendLine("🎯 *Contexto:* ${tab.title} (${tab.subtitle})")
            sb.appendLine("📊 *Progresso:* ${completed.size} de $total tarefas concluídas ($progressPercent%)")
            sb.appendLine("───────────────────────")
            if (pending.isNotEmpty()) {
                sb.appendLine("⏳ *PENDÊNCIAS DO TURNO (${pending.size}):*")
                pending.forEach { task ->
                    sb.appendLine(formatTaskLine(task, isDone = false))
                }
                sb.appendLine()
            }
            if (completed.isNotEmpty()) {
                sb.appendLine("✅ *CONCLUÍDAS NO TURNO (${completed.size}):*")
                completed.forEach { task ->
                    sb.appendLine(formatTaskLine(task, isDone = true))
                }
                sb.appendLine()
            }
            sb.appendLine("───────────────────────")
            sb.appendLine("_Enviado pelo App de Hotelaria Hospitalar_")
        }
        WhatsAppShareMode.PENDING_ONLY -> {
            sb.appendLine("🏨 *PENDÊNCIAS DE HOTELARIA HOSPITALAR*")
            sb.appendLine("🗓 *Data:* $now")
            sb.appendLine("🎯 *Contexto:* ${tab.title}")
            sb.appendLine("⏳ *O QUE FALTA FAZER (${pending.size}):*")
            sb.appendLine("───────────────────────")
            if (pending.isEmpty()) {
                sb.appendLine("🎉 Nenhuma pendência no momento! Todas as tarefas estão concluídas.")
            } else {
                pending.forEach { task ->
                    sb.appendLine(formatTaskLine(task, isDone = false))
                }
            }
            sb.appendLine("───────────────────────")
            sb.appendLine("_Enviado pelo App de Hotelaria Hospitalar_")
        }
        WhatsAppShareMode.COMPLETED_ONLY -> {
            sb.appendLine("🏨 *ENTREGAS DE HOTELARIA HOSPITALAR*")
            sb.appendLine("🗓 *Data:* $now")
            sb.appendLine("🎯 *Contexto:* ${tab.title}")
            sb.appendLine("✅ *TAREFAS CONCLUÍDAS NO TURNO (${completed.size}):*")
            sb.appendLine("───────────────────────")
            if (completed.isEmpty()) {
                sb.appendLine("Nenhuma tarefa concluída ainda.")
            } else {
                completed.forEach { task ->
                    sb.appendLine(formatTaskLine(task, isDone = true))
                }
            }
            sb.appendLine("───────────────────────")
            sb.appendLine("_Enviado pelo App de Hotelaria Hospitalar_")
        }
    }
    return sb.toString().trim()
}

fun formatSingleTaskWhatsAppText(task: TaskEntity): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
    val now = dateFormat.format(Date())
    val status = if (task.isCompleted) "✅ Concluída" else "⏳ Pendente"
    val timeInfo = if (!task.scheduledTime.isNullOrBlank()) "\n⏰ *Horário Agendado:* ${task.scheduledTime}" else ""

    return """
🏨 *AVISO DE HOTELARIA HOSPITALAR*
🗓 *Horário de Envio:* $now
📌 *Item:* ${task.title}$timeInfo
🏷️ *Categoria:* ${task.tag}
📍 *Status:* $status

_Enviado pelo App de Hotelaria Hospitalar_
    """.trimIndent()
}

fun launchWhatsAppShare(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    sendIntent.setPackage("com.whatsapp")
    try {
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        try {
            sendIntent.setPackage("com.whatsapp.w4b")
            context.startActivity(sendIntent)
        } catch (e2: Exception) {
            sendIntent.setPackage(null)
            val chooser = Intent.createChooser(sendIntent, "Compartilhar tarefas no WhatsApp")
            context.startActivity(chooser)
        }
    }
}

fun copyToClipboard(context: Context, text: String, label: String = "Hotelaria Tasks") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

/**
 * Diálogo Espacial para Compartilhamento de Tarefas no WhatsApp
 */
@Composable
fun WhatsAppShareDialog(
    tab: TaskTab,
    tasks: List<TaskEntity>,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onDismiss: () -> Unit,
    onSendWhatsApp: (String) -> Unit,
    onCopyText: (String) -> Unit
) {
    val modes = listOf(
        WhatsAppShareMode.ALL,
        WhatsAppShareMode.PENDING_ONLY,
        WhatsAppShareMode.COMPLETED_ONLY
    )
    var selectedMode by remember { mutableStateOf(WhatsAppShareMode.ALL) }
    val formattedMessage = remember(tab, tasks, selectedMode) {
        formatWhatsAppTasksText(tab, tasks, selectedMode)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF25D366).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Compartilhar no WhatsApp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Passagem de plantão e leitos",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Seletor de Modo em Pílulas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(colors.pillTrack)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    modes.forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (isSelected) activeAccent else Color.Transparent)
                                .clickable { selectedMode = mode }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = "Prévia da mensagem:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )

                // Caixa de Prévia com Rolagem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF141720) else Color(0xFFF2F5FA))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = formattedMessage,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.textPrimary
                    )
                }

                // Botão Enviar para o WhatsApp (Verde WhatsApp #25D366)
                Button(
                    onClick = { onSendWhatsApp(formattedMessage) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar para o WhatsApp",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Botão Copiar Texto
                TextButton(
                    onClick = { onCopyText(formattedMessage) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copiar Texto Formatado",
                        color = activeAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = colors.textSecondary)
            }
        }
    )
}


