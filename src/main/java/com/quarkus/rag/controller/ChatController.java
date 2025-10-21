package com.quarkus.rag.controller;

import com.quarkus.rag.dto.chat.ChatRequest;
import com.quarkus.rag.dto.chat.ChatResponse;
import com.quarkus.rag.service.RagService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatController {

    @Inject
    RagService ragService;

    @POST
    public ChatResponse chat(ChatRequest request) {
        String answer = ragService.ask(
            request.question(),
            request.maxResults() != null ? request.maxResults() : 5
        );
        return new ChatResponse(answer);
    }
}

