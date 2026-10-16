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
import java.util.UUID
import java.util.concurrent.TimeUnit

data class AiPlannedTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val tag: String,
    val tab: TaskTab,
    val scheduledTime: String? = null,
    var isSelected: Boolean = true
)

sealed interface GeminiPlannerResult {
    data class Success(val tasks: List<AiPlannedTask>) : GeminiPlannerResult
    data class Error(val message: String, val needsKey: Boolean = false) : GeminiPlannerResult
}

/**
 * Serviço de Inteligência Artificial para Planejamento de Turno e Hotelaria Hospitalar via Google Gemini 3.8 Flash.
 * Estrutura tarefas específicas para a rotina de governança hospitalar (Work) e pausas de autocuidado (Persona).
 */
class GeminiPlannerService {

    companion object {
        private const val TAG = "GeminiPlannerService"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"
        private const val FALLBACK_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

        private val SYSTEM_INSTRUCTION = """
            Você é uma IA especialista em Hotelaria Hospitalar, Governança Clínica e Bem-Estar de Plantonistas.
            Seu papel é criar um checklist de tarefas organizado, tático e de fácil execução para uma profissional de hotelaria hospitalar.
            
            DIRETRIZES DE HOTELARIA HOSPITALAR:
            1. As tarefas da aba 'WORK' devem cobrir:
               - Higienização Terminal (leitos de alta, isolamento de contato/respiratório, desinfecção de superfícies de alto toque).
               - Higienização Concorrente (leitos ocupados, banheiros, recolhimento de resíduos).
               - Rouparia & Enxoval (conferência de estoque no DML/rouparia, distribuição de enxoval limpo, recolhimento de hampel).
               - Governança & Leitos (inspeção de quartos vagos, checagem de frigobar/cortinas, liberação no sistema).
               - Auditoria de Quarto (conferência de funcionamento de ar condicionado, TV, chamada de enfermagem, vazamentos).
               - Manutenção & Chamados (abertura de ordens de serviço para manutenção predial).
               - Passagem de Plantão (relatório de pendências, leitos bloqueados e pendências de leitos com enfermagem).
            
            2. As tarefas da aba 'PERSONA' devem equilibrar o turno com autocuidado essencial:
               - Hidratação constante (copo d'água a cada 2h).
               - Pausa para descanso das pernas e pés pós-giro de leitos.
               - Lanche ou refeição nutritiva no refeitório/copa.
               - Higienização relaxante das mãos, troca de máscara/EPIs.
               - Organização de pertences pessoais ao fim do plantão.
            
            FORMATO OBRIGATÓRIO DE RESPOSTA:
            Retorne EXCLUSIVAMENTE um array JSON puro (sem markdown, sem ```json, sem comentários), contendo objetos com:
            - "title": Descrição concisa, acionável e profissional da tarefa (máximo 80 caracteres).
            - "tag": Uma das tags válidas (ex: "Higienização Terminal", "Rouparia & Enxoval", "Governança & Leitos", "Hidratação & Saúde", etc).
            - "tab": "WORK" ou "PERSONA"
            - "time": Horário sugerido no plantão (ex: "08:00", "09:30", "14:00", "19:00") ou vazio se não aplicável.
            
            Gere entre 4 a 8 tarefas bem dosadas, priorizando a solicitação do usuário.
        """.trimIndent()
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    suspend fun generateHospitalRoutine(
        prompt: String,
        apiKey: String
    ): GeminiPlannerResult = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim().removeSurrounding("\"").removeSurrounding("'")
        if (cleanKey.isBlank()) {
            return@withContext GeminiPlannerResult.Error(
                message = "Chave da API do Gemini não configurada. Toque em 'Configurar Chave' para colar sua chave do Google AI Studio ou a mesma do Tessera.",
                needsKey = true
            )
        }

        val cleanPrompt = prompt.trim().ifBlank {
            "Plantão padrão de 12 horas: organizar giro de leitos, conferência de enxoval e pausas de autocuidado."
        }

        try {
            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$SYSTEM_INSTRUCTION\n\nSOLICITAÇÃO DA PLANTONISTA: $cleanPrompt")
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyString = requestBodyJson.toString()

            var endpoint = "$BASE_URL?key=$cleanKey"
            var request = Request.Builder()
                .url(endpoint)
                .post(requestBodyString.toRequestBody(mediaType))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()

            var response = httpClient.newCall(request).execute()
            var responseCode = response.code
            var responseBody = response.body?.string()

            // Se o endpoint principal 3.8 retornar 404 ou 400 (ex: modelo específico), tenta o fallback 2.5
            if (!response.isSuccessful && (responseCode == 404 || responseCode == 400)) {
                val fallbackRequest = Request.Builder()
                    .url("$FALLBACK_URL?key=$cleanKey")
                    .post(requestBodyString.toRequestBody(mediaType))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                val fallbackCall = httpClient.newCall(fallbackRequest).execute()
                if (fallbackCall.isSuccessful) {
                    response = fallbackCall
                    responseCode = fallbackCall.code
                    responseBody = fallbackCall.body?.string()
                }
            }

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                val isKeyError = responseCode == 400 || responseCode == 401 || responseCode == 403
                val errorMsg = extractErrorMessage(responseBody)
                Log.w(TAG, "Erro Gemini HTTP $responseCode: $errorMsg")
                return@withContext GeminiPlannerResult.Error(
                    message = if (isKeyError) {
                        "Chave de API do Gemini inválida ou sem permissão ($responseCode)."
                    } else if (responseCode == 429) {
                        "Cota temporária da API excedida. Aguarde alguns segundos."
                    } else {
                        "Falha ao conectar com o Gemini: ${errorMsg ?: "Código HTTP $responseCode"}"
                    },
                    needsKey = isKeyError
                )
            }

            val tasks = parseGeneratedTasks(responseBody)
            if (tasks.isEmpty()) {
                return@withContext GeminiPlannerResult.Error("Nenhuma tarefa pôde ser extraída da resposta da IA.")
            }

            return@withContext GeminiPlannerResult.Success(tasks)
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao gerar rotina com IA: ${e.message}", e)
            return@withContext GeminiPlannerResult.Error(
                message = "Erro de conexão: ${e.localizedMessage ?: "Verifique sua internet."}"
            )
        }
    }

    private fun parseGeneratedTasks(rawJsonResponse: String): List<AiPlannedTask> {
        val resultList = mutableListOf<AiPlannedTask>()
        try {
            val root = JSONObject(rawJsonResponse)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            if (candidates.length() == 0) return emptyList()

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts") ?: return emptyList()
            if (parts.length() == 0) return emptyList()

            val rawText = parts.getJSONObject(0).optString("text", "").trim()
            if (rawText.isBlank()) return emptyList()

            // Remove marcação de código markdown se houver
            val cleanJson = rawText
                .removePrefix("```json")
                .removePrefix("```JSON")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = if (cleanJson.startsWith("[")) {
                JSONArray(cleanJson)
            } else if (cleanJson.startsWith("{")) {
                val obj = JSONObject(cleanJson)
                obj.optJSONArray("tasks") ?: obj.optJSONArray("tarefas") ?: JSONArray()
            } else {
                JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val title = item.optString("title", item.optString("titulo", "")).trim()
                if (title.isBlank()) continue

                val tabStr = item.optString("tab", item.optString("aba", "WORK")).uppercase().trim()
                val tab = if (tabStr.contains("PERSONA") || tabStr.contains("PESSOAL")) {
                    TaskTab.PERSONA
                } else {
                    TaskTab.WORK
                }

                val tag = item.optString("tag", item.optString("categoria", "")).trim().ifBlank {
                    if (tab == TaskTab.WORK) "Governança & Leitos" else "Autocuidado"
                }

                val rawTime = item.optString("time", item.optString("horario", "")).trim()
                val scheduledTime = rawTime.ifBlank { null }

                resultList.add(
                    AiPlannedTask(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        tag = tag,
                        tab = tab,
                        scheduledTime = scheduledTime,
                        isSelected = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao analisar tarefas JSON geradas: ${e.message}", e)
        }
        return resultList
    }

    private fun extractErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val root = JSONObject(errorJson)
            root.optJSONObject("error")?.optString("message")
        } catch (_: Exception) {
            null
        }
    }
}
