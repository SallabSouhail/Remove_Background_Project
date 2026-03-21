package com.removebg.controller;


import com.removebg.exception.CreditInsufficientException;
import com.removebg.exception.UnsupportedFormatException;
import com.removebg.exception.UserNotFoundException;
import com.removebg.response.RemoveBgResponse;
import com.removebg.service.BackgroundRemovalService;
import com.removebg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BackgroundRemovalController {

    private final BackgroundRemovalService backgroundRemovalService;

    @PostMapping(value = "/remove-bg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> removeBackground(@RequestParam("image") MultipartFile image, Authentication authentication) throws IOException {

        String clerkId = authentication.getName();

        String filename = backgroundRemovalService.getProcessedImageName(image);

        byte[] removedBgImage = backgroundRemovalService.processAndRemoveBackground(image, clerkId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(removedBgImage);
    }
}
