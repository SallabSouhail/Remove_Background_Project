package com.removebg.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotBlank(message = "Clerk id is required.")
    private String clerkId;
    private String firstName;
    private String lastName;
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    private String email;
    private Integer credits;
    private String photoUrl;
    private Boolean isCreated;
}
