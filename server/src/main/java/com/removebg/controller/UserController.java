package com.removebg.controller;

import com.removebg.dto.UserDTO;
import com.removebg.exception.UserNotFoundException;
import com.removebg.response.RemoveBgResponse;
import com.removebg.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<RemoveBgResponse> createOrUpdateUser(@Valid @RequestBody UserDTO userDTO, Authentication authentication) {
        if (!authentication.getName().equals(userDTO.getClerkId())) {
            throw new AccessDeniedException("User does not have permission to access this resource");
        }

        UserDTO savedUser = userService.saveUser(userDTO);
        return ResponseEntity.ok(RemoveBgResponse.builder()
                .success(true)
                .statusCode(HttpStatus.OK)
                .data(savedUser)
                .build());
    }

    @GetMapping("/credits")
    public ResponseEntity<RemoveBgResponse> getUserCredit(Authentication authentication) {
        String clerkId = authentication.getName();
        if (!userService.existsByClerkId(clerkId)) {
            throw new UserNotFoundException("User not found");
        }

        int credits = userService.getUserCredits(clerkId);
        return ResponseEntity.ok(RemoveBgResponse.builder()
                .success(true)
                .statusCode(HttpStatus.OK)
                .data(Map.of("credits", credits))
                .build());
    }
}
