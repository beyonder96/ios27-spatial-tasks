package com.example.ios27tasks

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Tela de Apresentação / Onboarding de Primeiro Uso para Hotelaria Hospitalar
 * Estética espacial iOS 27 com superfícies translúcidas, física elástica e vinculação da Conta Google.
 */
@Composable
fun HospitalityOnboardingScreen(
    colors: Ios27ThemeColors,
    initialEmail: String? = null,
    initialDisplayName: String? = null,
    onFinish: () -> Unit,
    onLinkGoogle: (email: String, displayName: String?) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })

    var googleEmail by remember { mutableStateOf(initialEmail ?: "") }
    var googleDisplayName by remember { mutableStateOf(initialDisplayName ?: "") }
    var accountChooserError by remember { mutableStateOf<String?>(null) }
    var showManualInput by remember { mutableStateOf(false) }

    // Launcher do seletor nativo de Contas Google do Android
    val googleAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                googleEmail = accountName
                accountChooserError = null
                val guessedName = accountName.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .joinToString(" ") { part ->
                        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                if (googleDisplayName.isBlank()) {
                    googleDisplayName = guessedName
                }
            }
        }
    }

    // Cor de destaque da página atual
    val currentAccent = when (pagerState.currentPage) {
        0 -> Color(0xFF00C7BE) // Glacial Teal
        1 -> Color(0xFF6E6BF5) // Electric Indigo
        2 -> Color(0xFF32ADE6) // Cyan Tátil
        3 -> Color(0xFF30D158) // Emerald Security
        4 -> Color(0xFF5856D6) // Royal Google Purple
        else -> Color(0xFF00C7BE)
    }

    val animatedAccent by animateColorAsState(
        targetValue = currentAccent,
        animationSpec = tween(450),
        label = "OnboardingAccent"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundBase)
    ) {
        // Brilho espacial ambiental no topo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            animatedAccent.copy(alpha = if (colors.isDark) 0.28f else 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(x = 550f, y = 220f),
                        radius = 1100f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra Superior: Badge + Botão Pular
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(animatedAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOSPITALITY ONBOARDING",
                        color = animatedAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                if (pagerState.currentPage < 4) {
                    Text(
                        text = "Pular",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(4)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conteúdo dos Slides em HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingWelcomeSlide(colors = colors, accent = animatedAccent)
                    1 -> OnboardingWorkPersonaSlide(colors = colors, accent = animatedAccent)
                    2 -> OnboardingGesturesSlide(colors = colors, accent = animatedAccent)
                    3 -> OnboardingIsolationSecuritySlide(colors = colors, accent = animatedAccent)
                    4 -> OnboardingGoogleAccountSlide(
                        colors = colors,
                        accent = animatedAccent,
                        email = googleEmail,
                        displayName = googleDisplayName,
                        showManualInput = showManualInput,
                        errorMessage = accountChooserError,
                        onSelectGoogleClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                val intent = AccountManager.newChooseAccountIntent(
                                    null,
                                    null,
                                    arrayOf("com.google"),
                                    null,
                                    null,
                                    null,
                                    null
                                )
                                googleAccountLauncher.launch(intent)
                            } catch (e: ActivityNotFoundException) {
                                accountChooserError = "Seletor de contas não encontrado. Digite seu e-mail abaixo."
                                showManualInput = true
                            } catch (e: Exception) {
                                accountChooserError = "Erro ao abrir contas Google: ${e.message}"
                                showManualInput = true
                            }
                        },
                        onToggleManualInput = { showManualInput = !showManualInput },
                        onEmailChanged = { googleEmail = it },
                        onDisplayNameChanged = { googleDisplayName = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Indicador de Pílulas iOS 27
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val isSelected = pagerState.currentPage == index
                    val pillWidth by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "PillIndicatorWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(pillWidth)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) animatedAccent else colors.textSecondary.copy(alpha = 0.28f)
                            )
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra de Navegação Inferior (Voltar / Próximo / Concluir)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    Row(
                        modifier = Modifier
                            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Voltar",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                if (pagerState.currentPage < 4) {
                    Row(
                        modifier = Modifier
                            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(animatedAccent, animatedAccent.copy(alpha = 0.85f))
                                )
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Continuar",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Continuar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    // Página Final (Google Account): Botão de Entrada
                    Row(
                        modifier = Modifier
                            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = true)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(animatedAccent, Color(0xFF00C7BE))
                                )
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (googleEmail.isNotBlank()) {
                                    onLinkGoogle(googleEmail.trim(), googleDisplayName.trim().ifBlank { null })
                                }
                                onFinish()
                            }
                            .padding(horizontal = 26.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (googleEmail.isNotBlank()) "Entrar na Rotina" else "Começar sem Vincular",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Concluir",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SLIDE 1: BOAS-VINDAS ---
@Composable
private fun OnboardingWelcomeSlide(
    colors: Ios27ThemeColors,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(92.dp)
                .ios27Glass(colors = colors, cornerRadius = 36.dp, elevated = true)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Apartment,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hotelaria Hospitalar",
            color = colors.textPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.8).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Espaço tátil e fluido desenvolvido exclusivamente para a sua rotina de governança, leitos e bem-estar no hospital.",
            color = colors.textSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Pílulas de Destaques
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FeatureHighlightRow(
                icon = Icons.Rounded.LocalHospital,
                title = "Rotina Assistencial & Leitos",
                subtitle = "Higienização, rouparia, auditorias e passagens de plantão.",
                colors = colors,
                accent = accent
            )
            FeatureHighlightRow(
                icon = Icons.Rounded.WaterDrop,
                title = "Autocuidado no Plantão",
                subtitle = "Lembretes de hidratação, pausas estratégicas e descanso.",
                colors = colors,
                accent = Color(0xFF6E6BF5)
            )
            FeatureHighlightRow(
                icon = Icons.Rounded.AutoAwesome,
                title = "Design Espacial & Tátil",
                subtitle = "Vidro lapidado, física elástica e ultra-responsividade ao toque.",
                colors = colors,
                accent = Color(0xFF32ADE6)
            )
        }
    }
}

// --- SLIDE 2: OS DOIS UNIVERSOS (WORK & PERSONA) ---
@Composable
private fun OnboardingWorkPersonaSlide(
    colors: Ios27ThemeColors,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dois Universos de Foco",
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Alterne instantaneamente na pílula superior entre as demandas do hospital e o seu bem-estar pessoal.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Card Work (Glacial Teal)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .ios27Glass(colors = colors, cornerRadius = 24.dp, elevated = true)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00C7BE).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Apartment,
                        contentDescription = null,
                        tint = Color(0xFF00C7BE),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Work • Hotelaria & Governança",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controle total da operação hospitalar",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChipSample("Higienização Terminal", Color(0xFF00C7BE), colors)
                TagChipSample("Higienização Concorrente", Color(0xFF00C7BE), colors)
                TagChipSample("Rouparia & Enxoval", Color(0xFF00C7BE), colors)
                TagChipSample("Leitos", Color(0xFF00C7BE), colors)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Persona (Electric Indigo)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .ios27Glass(colors = colors, cornerRadius = 24.dp, elevated = true)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF6E6BF5).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonOutline,
                        contentDescription = null,
                        tint = Color(0xFF6E6BF5),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Persona • Saúde & Autocuidado",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cuidar de você durante e após os turnos",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TagChipSample("Hidratação & Água", Color(0xFF6E6BF5), colors)
                TagChipSample("Pausa no Turno", Color(0xFF6E6BF5), colors)
                TagChipSample("Alimentação", Color(0xFF6E6BF5), colors)
                TagChipSample("Descanso", Color(0xFF6E6BF5), colors)
            }
        }
    }
}

// --- SLIDE 3: GESTOS TÁTEIS RÁPIDOS ---
@Composable
private fun OnboardingGesturesSlide(
    colors: Ios27ThemeColors,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Gestos Rápidos de Corredor",
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No ritmo veloz do hospital, tudo é feito com gestos suaves de swipe com uma mão só.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gesto Swipe Right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 22.dp, elevated = true)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF30D158).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Deslizar para a Direita ( → )",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Conclui ou reabre a tarefa com feedback háptico imediato.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Gesto Swipe Left
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 22.dp, elevated = true)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF453A).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF453A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Deslizar para a Esquerda ( ← )",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Exclui a tarefa rapidamente da sua lista ativa.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Undo Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 22.dp, elevated = true)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007AFF).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Undo,
                        contentDescription = null,
                        tint = Color(0xFF007AFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pílula Flutuante de Desfazer",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Excluiu sem querer? Uma pílula temporária permite restaurar em 5s.",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// --- SLIDE 4: ISOLAMENTO & SEGURANÇA OFFLINE ---
@Composable
private fun OnboardingIsolationSecuritySlide(
    colors: Ios27ThemeColors,
    accent: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Privado & Sempre Disponível",
            color = colors.textPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total independência e funcionamento contínuo, mesmo nos setores sem internet do hospital.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FeatureHighlightRow(
                icon = Icons.Rounded.Security,
                title = "100% Independente do Tessera",
                subtitle = "Este app possui banco próprio e canal exclusivo. Nenhuma tarefa se mistura com outros projetos.",
                colors = colors,
                accent = Color(0xFF30D158)
            )
            FeatureHighlightRow(
                icon = Icons.Rounded.WifiOff,
                title = "Funciona 100% Offline (Room)",
                subtitle = "Subsolos, elevadores ou áreas sem rede: crie e conclua tarefas sem nenhuma trava.",
                colors = colors,
                accent = Color(0xFFFF9F0A)
            )
            FeatureHighlightRow(
                icon = Icons.Rounded.CloudDone,
                title = "Hub de Nuvem Hospitalar",
                subtitle = "Sincronização em segundo plano assim que o Wi-Fi ou dados móveis retornarem.",
                colors = colors,
                accent = Color(0xFF32ADE6)
            )
        }
    }
}

// --- SLIDE 5: VINCULAR CONTA GOOGLE ---
@Composable
private fun OnboardingGoogleAccountSlide(
    colors: Ios27ThemeColors,
    accent: Color,
    email: String,
    displayName: String,
    showManualInput: Boolean,
    errorMessage: String?,
    onSelectGoogleClick: () -> Unit,
    onToggleManualInput: () -> Unit,
    onEmailChanged: (String) -> Unit,
    onDisplayNameChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .ios27Glass(colors = colors, cornerRadius = 32.dp, elevated = true)
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Vincular Conta Google",
            color = colors.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.6).sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Conecte sua conta para personalizar seu perfil hospitalar e manter suas preferências salvas.",
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Card da Conta Selecionada (se houver)
        if (email.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 24.dp, elevated = true)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF30D158).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercase() ?: email.firstOrNull()?.uppercase() ?: "G",
                        color = accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName.ifBlank { "Conta Google" },
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Vinculado",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = email,
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "Trocar",
                    color = accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onSelectGoogleClick)
                        .padding(8.dp)
                )
            }
        } else {
            // Botão Principal de Seleção do Android
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .ios27Glass(colors = colors, cornerRadius = 24.dp, elevated = true)
                    .clickable(onClick = onSelectGoogleClick)
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color(0xFF4285F4),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Selecionar Conta Google",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFFF453A),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alternar para inserção manual caso deseje
        Text(
            text = if (showManualInput) "Ocultar inserção manual" else "Ou personalizar nome / e-mail manualmente",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable(onClick = onToggleManualInput)
                .padding(6.dp)
        )

        AnimatedVisibility(visible = showManualInput) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChanged,
                    label = { Text("Nome de Exibição (Ex: Juliana)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = accent,
                        unfocusedBorderColor = colors.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    label = { Text("E-mail Google (Ex: juliana@gmail.com)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = accent,
                        unfocusedBorderColor = colors.borderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: Ios27ThemeColors,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .ios27Glass(colors = colors, cornerRadius = 20.dp, elevated = true)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TagChipSample(
    tag: String,
    accent: Color,
    colors: Ios27ThemeColors
) {
    Box(
        modifier = Modifier
            .ios27Glass(colors = colors, cornerRadius = 50.dp, elevated = false)
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = tag,
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
