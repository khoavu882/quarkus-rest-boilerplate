package com.github.kaivu.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UpdateEntityDTO(@Size(max = 500) @NotBlank String name, @Size(max = 2000) @NotEmpty String description) {}
