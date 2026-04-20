package com.acme.adapters.in.rest.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePersonRequest(@NotBlank String name, @NotBlank String personType) {
}
