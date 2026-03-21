package com.removebg.service.impl;

import com.removebg.exception.UserNotFoundException;
import com.removebg.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCreditServiceImpl")
class UserCreditServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserCreditServiceImpl userCreditService;

    @BeforeEach
    void setUp() {
        userCreditService = new UserCreditServiceImpl(userRepository);
    }

    @Test
    @DisplayName("consumeCredits returns true when the repository updates a row")
    void consumeCreditsReturnsTrueWhenCreditsWereDeducted() {
        when(userRepository.deductCreditsIfEnough("user-123", 2)).thenReturn(1);

        boolean consumed = userCreditService.consumeCredits("user-123", 2);

        assertTrue(consumed);
        verify(userRepository).deductCreditsIfEnough("user-123", 2);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("consumeCredits returns false when the repository does not update a row")
    void consumeCreditsReturnsFalseWhenCreditsWereNotDeducted() {
        when(userRepository.deductCreditsIfEnough("user-123", 2)).thenReturn(0);

        boolean consumed = userCreditService.consumeCredits("user-123", 2);

        assertFalse(consumed);
        verify(userRepository).deductCreditsIfEnough("user-123", 2);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("refundCredits delegates successfully when the user exists")
    void refundCreditsDelegatesSuccessfully() {
        when(userRepository.refundCredits("user-123", 1)).thenReturn(1);

        userCreditService.refundCredits("user-123", 1);

        verify(userRepository).refundCredits("user-123", 1);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("refundCredits throws when the user does not exist")
    void refundCreditsThrowsWhenUserDoesNotExist() {
        when(userRepository.refundCredits("missing-user", 1)).thenReturn(0);

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userCreditService.refundCredits("missing-user", 1)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).refundCredits("missing-user", 1);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("addCreditsOrThrow delegates successfully when the user exists")
    void addCreditsOrThrowDelegatesSuccessfully() {
        when(userRepository.addCredits("user-123", 50)).thenReturn(1);

        userCreditService.addCreditsOrThrow("user-123", 50);

        verify(userRepository).addCredits("user-123", 50);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("addCreditsOrThrow throws when the user does not exist")
    void addCreditsOrThrowThrowsWhenUserDoesNotExist() {
        when(userRepository.addCredits("missing-user", 50)).thenReturn(0);

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userCreditService.addCreditsOrThrow("missing-user", 50)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).addCredits("missing-user", 50);
        verifyNoMoreInteractions(userRepository);
    }
}
