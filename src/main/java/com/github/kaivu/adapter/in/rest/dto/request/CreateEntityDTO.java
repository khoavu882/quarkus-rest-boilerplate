package com.github.kaivu.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateEntityDTO(@Size(max = 500) @NotBlank String name, @Size(max = 2000) @NotEmpty String description) {}
