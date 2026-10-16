package com.example.ios27tasks

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ios27tasks.data.AiPlannedTask
import com.example.ios27tasks.data.GeminiPlannerResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiPlannerDialog(
    currentApiKey: String,
    colors: Ios27ThemeColors,
    activeAccent: Color,
    onSaveApiKey: (String) -> Unit,
    onGenerate: suspend (String) -> GeminiPlannerResult,
    onAddPlannedTasks: (List<AiPlannedTask>) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showApiKeyInput by remember { mutableStateOf(currentApiKey.isBlank()) }
    var tempApiKey by remember { mutableStateOf(currentApiKey) }

    val generatedTasks = remember { mutableStateListOf<AiPlannedTask>() }

    val quickPresets = remember {
        listOf(
            "Giro Completo: 4 Leitos Terminais, 6 Concorrentes e Enxoval",
            "Isolamento por Contato e Gotículas na UTI",
            "Conferência de Rouparia e Auditoria no DML",
            "Passagem de Plantão Noturno (19h) e Acomodações Vagas",
            "Plantão 12h Equilibrado: Hotelaria + 3 Pausas de Autocuidado"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .imePadding()
                .ios27Glass(colors = colors, cornerRadius = 28.dp, elevated = true)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header com Ícone AutoAwesome, Título e Botão Fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFFD60A), activeAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Planejar com IA",
                                color = colors.textPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Hotelaria Hospitalar & Autocuidado",
                                color = activeAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.pillTrack)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Fechar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Seção da Chave de API do Gemini
                if (currentApiKey.isBlank() || showApiKeyInput) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFFF9F0A).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFF9F0A).copy(alpha = 0.12f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Key,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9F0A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Chave de API do Google Gemini",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Cole sua chave gratuita do Google AI Studio ou a mesma configurada no Tessera para liberar o assistente de plantão.",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = tempApiKey,
                                onValueChange = { tempApiKey = it },
                                placeholder = { Text("AIzaSy...", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = activeAccent,
                                    unfocusedBorderColor = colors.borderSubtle
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (currentApiKey.isNotBlank()) {
                                    TextButton(onClick = { showApiKeyInput = false }) {
                                        Text("Cancelar", fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Button(
                                    onClick = {
                                        if (tempApiKey.isNotBlank()) {
                                            onSaveApiKey(tempApiKey)
                                            showApiKeyInput = false
                                            Toast.makeText(context, "Chave Gemini salva com sucesso!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Salvar Chave", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.pillTrack)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF30D158))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Flash Conectado (••••${currentApiKey.takeLast(4)})",
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        TextButton(
                            onClick = {
                                tempApiKey = currentApiKey
                                showApiKeyInput = true
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = activeAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alterar", fontSize = 11.sp, color = activeAccent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sugestões Rápidas (Chips de Plantão)
                Text(
                    text = "SUGESTÕES RÁPIDAS DE PLANTÃO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeAccent,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.pillTrack)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                                .clickable { promptText = preset }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo de Entrada Livre para o Plantão
                Text(
                    text = "DESCREVA SEU TURNO OU PRIORIDADES:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = {
                        promptText = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = "Ex: Tenho 3 altas no 3º andar, 2 isolamentos na UTI, reposição de enxoval e preciso passar plantão às 19h...",
                            fontSize = 12.sp,
                            color = colors.textSecondary.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeAccent,
                        unfocusedBorderColor = colors.borderSubtle
                    )
                )

                // Mensagem de Erro (se houver)
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF453A).copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontSize = 11.sp,
                            color = Color(0xFFFF453A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botão "Gerar Rotina com IA"
                Button(
                    onClick = {
                        if (currentApiKey.isBlank()) {
                            showApiKeyInput = true
                            errorMessage = "Configure sua chave Gemini antes de gerar a rotina."
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = onGenerate(promptText)
                            isLoading = false
                            when (result) {
                                is GeminiPlannerResult.Success -> {
                                    generatedTasks.clear()
                                    generatedTasks.addAll(result.tasks)
                                }
                                is GeminiPlannerResult.Error -> {
                                    errorMessage = result.message
                                    if (result.needsKey) {
                                        showApiKeyInput = true
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estruturando Turno com Gemini 2.0 Flash...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (generatedTasks.isEmpty()) "Gerar Rotina Inteligente" else "Regenerar Rotina",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Lista de Tarefas Geradas
                if (generatedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    val selectedCount = generatedTasks.count { it.isSelected }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TAREFAS SUGERIDAS ($selectedCount/${generatedTasks.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeAccent,
                            letterSpacing = 1.1.sp
                        )
                        TextButton(
                            onClick = {
                                val allSelected = generatedTasks.all { it.isSelected }
                                val updated = generatedTasks.map { it.copy(isSelected = !allSelected) }
                                generatedTasks.clear()
                                generatedTasks.addAll(updated)
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (generatedTasks.all { it.isSelected }) "Desmarcar Todas" else "Selecionar Todas",
                                fontSize = 11.sp,
                                color = activeAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        generatedTasks.forEachIndexed { index, item ->
                            val isWork = item.tab == TaskTab.WORK
                            val tabAccent = if (isWork) Color(0xFF00C7BE) else Color(0xFF6E6BF5)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.glassElevated)
                                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                                    .clickable {
                                        generatedTasks[index] = item.copy(isSelected = !item.isSelected)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Checkbox redonda
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (item.isSelected) activeAccent else Color.Transparent)
                                        .border(
                                            1.5.dp,
                                            if (item.isSelected) activeAccent else colors.borderHighlight,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (item.isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Badge de Contexto (Work vs Persona)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(tabAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isWork) Icons.Rounded.Apartment else Icons.Rounded.PersonOutline,
                                                    contentDescription = null,
                                                    tint = tabAccent,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = item.tab.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = tabAccent
                                                )
                                            }
                                        }

                                        // Badge da Tag
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(colors.pillTrack)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão Principal para Adicionar as Tarefas ao Dia
                    Button(
                        onClick = {
                            val selectedTasks = generatedTasks.filter { it.isSelected }
                            if (selectedTasks.isNotEmpty()) {
                                onAddPlannedTasks(selectedTasks)
                                Toast.makeText(
                                    context,
                                    "${selectedTasks.size} tarefas adicionadas à sua rotina!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onDismiss()
                            }
                        },
                        enabled = selectedCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adicionar $selectedCount Tarefas ao Meu Dia",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
