package org.cybersaarthi.awareness.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cybersaarthi.awareness.dto.TopicDetailDto;
import org.cybersaarthi.awareness.dto.TopicSummaryDto;
import org.cybersaarthi.awareness.service.AwarenessService;
import org.cybersaarthi.category.entity.IncidentCategory;
import org.cybersaarthi.category.repository.IncidentCategoryRepository;
import org.cybersaarthi.common.response.ApiResponse;
import org.cybersaarthi.security.user.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/awareness")
@Tag(name = "Awareness Catalog", description = "Public endpoints to browse educational cybercrime threat guides")
public class AwarenessController {

    private final AwarenessService awarenessService;
    private final IncidentCategoryRepository categoryRepository;

    public AwarenessController(AwarenessService awarenessService, IncidentCategoryRepository categoryRepository) {
        this.awarenessService = awarenessService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/topics")
    @Operation(summary = "List published awareness topics", description = "Returns all published educational topics with optional category filter and keyword search.")
    public ResponseEntity<ApiResponse<List<TopicSummaryDto>>> listTopics(
            @Parameter(description = "Taxonomy category code filter") @RequestParam(required = false) String category,
            @Parameter(description = "Keyword search in title or summary") @RequestParam(required = false) String q) {

        List<TopicSummaryDto> topics = awarenessService.getPublishedTopics(category, q);
        return ResponseEntity.ok(ApiResponse.success("Published awareness topics retrieved", topics));
    }

    @GetMapping("/topics/{slug}")
    @Operation(summary = "Get awareness topic detail", description = "Retrieves full educational guide and markdown content by slug.")
    public ResponseEntity<ApiResponse<TopicDetailDto>> getTopicBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        TopicDetailDto topic = awarenessService.getTopicBySlug(slug, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Awareness topic detail retrieved", topic));
    }

    @GetMapping("/categories")
    @Operation(summary = "List active taxonomy categories", description = "Returns active Taxonomy v1.0 categories for frontend filters.")
    public ResponseEntity<ApiResponse<List<IncidentCategory>>> listCategories() {
        List<IncidentCategory> categories = categoryRepository.findAllByTaxonomyVersionAndIsActiveTrue("v1.0");
        return ResponseEntity.ok(ApiResponse.success("Taxonomy categories retrieved", categories));
    }
}
