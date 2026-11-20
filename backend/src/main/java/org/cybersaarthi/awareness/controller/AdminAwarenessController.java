package org.cybersaarthi.awareness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cybersaarthi.awareness.dto.CreateTopicRequest;
import org.cybersaarthi.awareness.dto.TopicDetailDto;
import org.cybersaarthi.awareness.dto.UpdateTopicRequest;
import org.cybersaarthi.awareness.dto.UpdateTopicStatusRequest;
import org.cybersaarthi.awareness.service.AwarenessService;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/awareness/topics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Awareness Management", description = "Privileged administrator endpoints to author and publish educational topics")
public class AdminAwarenessController {

    private final AwarenessService awarenessService;

    public AdminAwarenessController(AwarenessService awarenessService) {
        this.awarenessService = awarenessService;
    }

    @PostMapping
    @Operation(summary = "Create awareness topic", description = "Creates a new topic in DRAFT state.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<TopicDetailDto>> createTopic(
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TopicDetailDto created = awarenessService.createTopic(request, userDetails.getId());
        return new ResponseEntity<>(ApiResponse.created("Awareness topic created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update awareness topic", description = "Updates topic title, summary, markdown, and category.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<TopicDetailDto>> updateTopic(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTopicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TopicDetailDto updated = awarenessService.updateTopic(id, request, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Awareness topic updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update topic publication status", description = "Transitions topic status between DRAFT, PUBLISHED, and ARCHIVED.", security = @SecurityRequirement(name = "BearerAuth"))
    public ResponseEntity<ApiResponse<TopicDetailDto>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTopicStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TopicDetailDto updated = awarenessService.updateTopicStatus(id, request.publicationStatus(), userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Topic publication status updated successfully", updated));
    }
}
