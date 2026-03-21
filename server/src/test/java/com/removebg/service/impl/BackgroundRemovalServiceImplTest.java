package com.removebg.service.impl;

import com.removebg.entity.UserEntity;
import com.removebg.exception.CreditInsufficientException;
import com.removebg.exception.OperationFailedException;
import com.removebg.exception.UnsupportedFormatException;
import com.removebg.exception.UserNotFoundException;
import com.removebg.service.RemoveBgClient;
import com.removebg.service.UserCreditService;
import com.removebg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BackgroundRemovalServiceImpl")
class BackgroundRemovalServiceImplTest {

    private static final String CLERK_ID = "clerk-123";
    private static final int REMOVE_BG_CREDIT_COST = 1;

    @Mock
    private RemoveBgClient removeBgClient;

    @Mock
    private UserService userService;

    @Mock
    private UserCreditService userCreditService;

    private BackgroundRemovalServiceImpl backgroundRemovalService;
    private MultipartFile image;

    @BeforeEach
    void setUp() {
        backgroundRemovalService = new BackgroundRemovalServiceImpl(removeBgClient, userService, userCreditService);
        image = new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    @Test
    @DisplayName("processAndRemoveBackground returns the processed image when the request succeeds")
    void processAndRemoveBackgroundReturnsProcessedImage() throws IOException {
        byte[] processedImage = new byte[]{5, 6, 7};

        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.of(existingUser()));
        when(userCreditService.consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST)).thenReturn(true);
        when(removeBgClient.removeBackground(image)).thenReturn(processedImage);

        byte[] result = backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID);

        assertArrayEquals(processedImage, result);
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(userCreditService).consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(userCreditService, never()).refundCredits(anyString(), anyInt());
        verify(removeBgClient).removeBackground(image);
        verifyNoMoreInteractions(userService, userCreditService, removeBgClient);
    }

    @Test
    @DisplayName("processAndRemoveBackground throws when the clerk id does not match any user")
    void processAndRemoveBackgroundThrowsWhenUserDoesNotExist() {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userService).getUserByClerkId(CLERK_ID);
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(userCreditService, removeBgClient);
    }

    @Test
    @DisplayName("processAndRemoveBackground throws when the user has no remaining credits")
    void processAndRemoveBackgroundThrowsWhenCreditsAreInsufficient() {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.of(existingUser()));
        when(userCreditService.consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST)).thenReturn(false);

        CreditInsufficientException exception = assertThrows(
                CreditInsufficientException.class,
                () -> backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID)
        );

        assertEquals("Credit insufficient, please charge your credits", exception.getMessage());
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(userCreditService).consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(userCreditService, never()).refundCredits(anyString(), anyInt());
        verifyNoInteractions(removeBgClient);
        verifyNoMoreInteractions(userService, userCreditService);
    }

    @Test
    @DisplayName("processAndRemoveBackground refunds the credit when the remove.bg client throws")
    void processAndRemoveBackgroundRefundsCreditsWhenClientThrows() throws IOException {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.of(existingUser()));
        when(userCreditService.consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST)).thenReturn(true);
        when(removeBgClient.removeBackground(image)).thenThrow(new IOException("remove.bg failed"));

        OperationFailedException exception = assertThrows(
                OperationFailedException.class,
                () -> backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID)
        );

        assertEquals("Removing background has been failed, please try again", exception.getMessage());
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(userCreditService).consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(userCreditService).refundCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(removeBgClient).removeBackground(image);
        verifyNoMoreInteractions(userService, userCreditService, removeBgClient);
    }

    @Test
    @DisplayName("processAndRemoveBackground refunds the credit when the remove.bg response is null")
    void processAndRemoveBackgroundRefundsCreditsWhenClientReturnsNull() throws IOException {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.of(existingUser()));
        when(userCreditService.consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST)).thenReturn(true);
        when(removeBgClient.removeBackground(image)).thenReturn(null);

        OperationFailedException exception = assertThrows(
                OperationFailedException.class,
                () -> backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID)
        );

        assertEquals("Removing background has been failed, please try again", exception.getMessage());
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(userCreditService).consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(userCreditService).refundCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(removeBgClient).removeBackground(image);
        verifyNoMoreInteractions(userService, userCreditService, removeBgClient);
    }

    @Test
    @DisplayName("processAndRemoveBackground refunds the credit when the remove.bg response is empty")
    void processAndRemoveBackgroundRefundsCreditsWhenClientReturnsEmptyArray() throws IOException {
        when(userService.getUserByClerkId(CLERK_ID)).thenReturn(Optional.of(existingUser()));
        when(userCreditService.consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST)).thenReturn(true);
        when(removeBgClient.removeBackground(image)).thenReturn(new byte[0]);

        OperationFailedException exception = assertThrows(
                OperationFailedException.class,
                () -> backgroundRemovalService.processAndRemoveBackground(image, CLERK_ID)
        );

        assertEquals("Removing background has been failed, please try again", exception.getMessage());
        verify(userService).getUserByClerkId(CLERK_ID);
        verify(userCreditService).consumeCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(userCreditService).refundCredits(CLERK_ID, REMOVE_BG_CREDIT_COST);
        verify(removeBgClient).removeBackground(image);
        verifyNoMoreInteractions(userService, userCreditService, removeBgClient);
    }

    @Test
    @DisplayName("getProcessedImageName keeps the original extension for supported images")
    void getProcessedImageNameReturnsTheExpectedName() {
        MultipartFile pngImage = new MockMultipartFile("image", "avatar.png", "image/png", new byte[]{1, 2, 3});

        String processedImageName = backgroundRemovalService.getProcessedImageName(pngImage);

        assertEquals("avatarno-bg.png", processedImageName);
    }

    @Test
    @DisplayName("getProcessedImageName rejects images with an unsupported content type")
    void getProcessedImageNameRejectsUnsupportedContentType() {
        MultipartFile unsupportedImage = new MockMultipartFile(
                "image",
                "avatar.png",
                "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        UnsupportedFormatException exception = assertThrows(
                UnsupportedFormatException.class,
                () -> backgroundRemovalService.getProcessedImageName(unsupportedImage)
        );

        assertEquals("The image format is not supported", exception.getMessage());
    }

    @Test
    @DisplayName("getProcessedImageName rejects files without an image extension")
    void getProcessedImageNameRejectsMissingExtension() {
        MultipartFile imageWithoutExtension = new MockMultipartFile("image", "avatar", "image/jpeg", new byte[]{1, 2, 3});

        UnsupportedFormatException exception = assertThrows(
                UnsupportedFormatException.class,
                () -> backgroundRemovalService.getProcessedImageName(imageWithoutExtension)
        );

        assertEquals("The image format is not supported", exception.getMessage());
    }

    @Test
    @DisplayName("getProcessedImageName rejects null input")
    void getProcessedImageNameRejectsNullImage() {
        UnsupportedFormatException exception = assertThrows(
                UnsupportedFormatException.class,
                () -> backgroundRemovalService.getProcessedImageName(null)
        );

        assertEquals("The image format is not supported", exception.getMessage());
    }

    private UserEntity existingUser() {
        return UserEntity.builder()
                .id(1L)
                .clerkId(CLERK_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .credits(5)
                .photoUrl("photo-url")
                .build();
    }
}
