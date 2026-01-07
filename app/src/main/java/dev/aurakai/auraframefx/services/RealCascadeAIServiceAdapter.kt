package dev.aurakai.auraframefx.services

import dev.aurakai.auraframefx.oracledrive.genesis.ai.services.CascadeAIService as OrchestratorCascade
import dev.aurakai.auraframefx.utils.AuraFxLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real implementation of CascadeAIService adapter.
 */
@Singleton
class RealCascadeAIServiceAdapter @Inject constructor(
    private val orchestrator: OrchestratorCascade,
    private val logger: AuraFxLogger
) : CascadeAIService {

    override suspend fun processRequest(
        request: AiRequest,
        context: String
    ): AgentResponse {
        return try {
            // Convert to orchestrator's request format
            val orchestratorRequest = OrchestratorCascade.AiRequest(
                prompt = request.prompt,
                task = request.task,
                metadata = request.metadata,
                sessionId = request.sessionId,
                correlationId = request.correlationId
            )

            // Delegate to the real orchestrator
            val response = orchestrator.processRequest(orchestratorRequest, context)

            // Convert back to service response
            AgentResponse(
                content = response.content,
                confidence = response.confidence,
                meta = response.meta
            )
        } catch (e: Exception) {
            logger.e("RealCascadeAIServiceAdapter", "Error processing request", e)
            throw CascadeAIService.CascadeException("Failed to process request: ${e.message}", e)
        }
    /**
     * Processes an AI request within a given context and produces an AgentResponse.
     *
     * @param request The AI request containing the prompt and associated data.
     * @param context Contextual information to influence processing; may be an empty string.
     * @return An AgentResponse representing a successful CascadeAI result whose content is derived from the request prompt, with confidence 1.0 and agentName "CascadeAI".
     */
    override suspend fun processRequest(request: AiRequest, context: String): AgentResponse {
        // Real implementation logic would go here
        // For now, returning a basic success response to satisfy the interface
        return AgentResponse.success(
            content = "Real Cascade processing: ${request.prompt}",
            confidence = 1.0f,
            agentName = "CascadeAI"
        )
    }

    override fun streamRequest(
        request: AiRequest,
        context: String
    ): Flow<AgentResponse> {
        return try {
            // Convert to orchestrator's request format
            val orchestratorRequest = OrchestratorCascade.AiRequest(
                prompt = request.prompt,
                task = request.task,
                metadata = request.metadata,
                sessionId = request.sessionId,
                correlationId = request.correlationId
            )

            // Delegate to the real orchestrator and map responses
            orchestrator.streamRequest(orchestratorRequest, context)
                .map { orchestratorResponse ->
                    AgentResponse(
                        content = orchestratorResponse.content,
                        confidence = orchestratorResponse.confidence,
                        meta = orchestratorResponse.meta
                    )
                }
        } catch (e: Exception) {
            logger.e("RealCascadeAIServiceAdapter", "Error in stream request", e)
            throw CascadeAIService.CascadeException("Failed to process stream request: ${e.message}", e)
        }
    }
}
