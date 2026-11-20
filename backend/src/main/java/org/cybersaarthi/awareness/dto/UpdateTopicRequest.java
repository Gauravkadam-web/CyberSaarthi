package org.cybersaarthi.awareness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin request payload to update an awareness educational topic")
public record UpdateTopicRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    @NotBlank(message = "Summary is required")
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    String summary,

    @NotBlank(message = "Body markdown content is required")
    String bodyMarkdown,

    @NotBlank(message = "Topic category is required")
    @Size(max = 80, message = "Topic category code cannot exceed 80 characters")
    String topicCategory
) {}
