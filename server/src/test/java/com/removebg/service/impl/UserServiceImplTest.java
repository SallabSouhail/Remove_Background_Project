package com.removebg.service.impl;

import com.removebg.dto.UserDTO;
import com.removebg.entity.UserEntity;
import com.removebg.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @DisplayName("saveUser updates an existing user and marks the response as not created")
    void saveUserUpdatesExistingUser() {
        UserEntity existingUser = existingUser();
        UserDTO updateRequest = UserDTO.builder()
                .clerkId("user-123")
                .email("updated.email@gmail.com")
                .firstName("Updated")
                .lastName("User")
                .photoUrl("new-photo")
                .build();

        when(userRepository.findByClerkId(updateRequest.getClerkId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.saveUser(updateRequest);

        ArgumentCaptor<UserEntity> savedUserCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).findByClerkId(updateRequest.getClerkId());
        verify(userRepository).save(savedUserCaptor.capture());
        verifyNoMoreInteractions(userRepository);

        UserEntity savedUser = savedUserCaptor.getValue();
        assertSame(existingUser, savedUser);
        assertEquals("updated.email@gmail.com", savedUser.getEmail());
        assertEquals("Updated", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertEquals("new-photo", savedUser.getPhotoUrl());
        assertEquals(8, savedUser.getCredits());

        assertEquals("user-123", result.getClerkId());
        assertEquals("updated.email@gmail.com", result.getEmail());
        assertEquals("Updated", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("new-photo", result.getPhotoUrl());
        assertEquals(8, result.getCredits());
        assertFalse(result.getIsCreated());
    }

    @Test
    @DisplayName("saveUser creates a new user and marks the response as created")
    void saveUserCreatesNewUser() {
        UserDTO createRequest = UserDTO.builder()
                .clerkId("user-999")
                .email("new.user@gmail.com")
                .firstName("New")
                .lastName("User")
                .photoUrl("profile-photo")
                .build();

        UserEntity savedUser = UserEntity.builder()
                .id(99L)
                .clerkId("user-999")
                .email("new.user@gmail.com")
                .firstName("New")
                .lastName("User")
                .photoUrl("profile-photo")
                .credits(5)
                .build();

        when(userRepository.findByClerkId(createRequest.getClerkId())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserDTO result = userService.saveUser(createRequest);

        ArgumentCaptor<UserEntity> savedUserCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).findByClerkId(createRequest.getClerkId());
        verify(userRepository).save(savedUserCaptor.capture());
        verifyNoMoreInteractions(userRepository);

        UserEntity newUser = savedUserCaptor.getValue();
        assertEquals("user-999", newUser.getClerkId());
        assertEquals("new.user@gmail.com", newUser.getEmail());
        assertEquals("New", newUser.getFirstName());
        assertEquals("User", newUser.getLastName());
        assertEquals("profile-photo", newUser.getPhotoUrl());
        assertNull(newUser.getCredits());

        assertEquals("user-999", result.getClerkId());
        assertEquals("new.user@gmail.com", result.getEmail());
        assertEquals("New", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("profile-photo", result.getPhotoUrl());
        assertEquals(5, result.getCredits());
        assertTrue(result.getIsCreated());
    }

    @Test
    @DisplayName("saveUser propagates repository failures")
    void saveUserPropagatesRepositoryFailure() {
        UserDTO createRequest = UserDTO.builder()
                .clerkId("user-999")
                .email("new.user@gmail.com")
                .firstName("New")
                .lastName("User")
                .build();
        RuntimeException repositoryFailure = new RuntimeException("Database unavailable");

        when(userRepository.findByClerkId(createRequest.getClerkId())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenThrow(repositoryFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.saveUser(createRequest));

        assertSame(repositoryFailure, thrown);
        verify(userRepository).findByClerkId(createRequest.getClerkId());
        verify(userRepository).save(any(UserEntity.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserCredits returns the stored credit amount")
    void getUserCreditsReturnsStoredCredits() {
        when(userRepository.findCreditsByClerkId("user-123")).thenReturn(Optional.of(12));

        Integer credits = userService.getUserCredits("user-123");

        assertEquals(12, credits);
        verify(userRepository).findCreditsByClerkId("user-123");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserCredits returns zero when the user is missing")
    void getUserCreditsReturnsZeroWhenUserDoesNotExist() {
        when(userRepository.findCreditsByClerkId("missing-user")).thenReturn(Optional.empty());

        Integer credits = userService.getUserCredits("missing-user");

        assertEquals(0, credits);
        verify(userRepository).findCreditsByClerkId("missing-user");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("existsByClerkId delegates to the repository")
    void existsByClerkIdDelegatesToRepository() {
        when(userRepository.existsByClerkId("user-123")).thenReturn(true);

        boolean exists = userService.existsByClerkId("user-123");

        assertTrue(exists);
        verify(userRepository).existsByClerkId("user-123");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("getUserByClerkId returns the repository result")
    void getUserByClerkIdReturnsRepositoryResult() {
        UserEntity existingUser = existingUser();
        when(userRepository.findByClerkId("user-123")).thenReturn(Optional.of(existingUser));

        Optional<UserEntity> result = userService.getUserByClerkId("user-123");

        assertTrue(result.isPresent());
        assertSame(existingUser, result.get());
        verify(userRepository).findByClerkId("user-123");
        verifyNoMoreInteractions(userRepository);
    }

    private UserEntity existingUser() {
        return UserEntity.builder()
                .id(123L)
                .clerkId("user-123")
                .email("john.doe@gmail.com")
                .firstName("John")
                .lastName("Doe")
                .credits(8)
                .photoUrl("old-photo")
                .build();
    }
}
