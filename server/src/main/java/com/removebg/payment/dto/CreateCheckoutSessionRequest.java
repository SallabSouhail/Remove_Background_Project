package com.removebg.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {
    @NotBlank(message = "Package id is required.")
    private String packageId;
}
