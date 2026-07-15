package com.feedback.feedback360.dto;

import jakarta.validation.constraints.*;

public record FeedbackSubmitRequest(
    @NotNull @Min(0) @Max(10) Short rating,
    @NotBlank String comment
) {}
