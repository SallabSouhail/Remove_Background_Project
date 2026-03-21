package com.removebg.service.impl;

import com.removebg.exception.UserNotFoundException;
import com.removebg.repository.UserRepository;
import com.removebg.service.UserCreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCreditServiceImpl implements UserCreditService {

    private final UserRepository userRepository;

    @Override
    public boolean consumeCredits(String clerkId, int credits) {
        return userRepository.deductCreditsIfEnough(clerkId, credits) > 0;
    }

    @Override
    public void refundCredits(String clerkId, int credits) {
        if (userRepository.refundCredits(clerkId, credits) == 0) {
            throw new UserNotFoundException("User not found");
        }
    }

    @Override
    public void addCreditsOrThrow(String clerkId, int credits) {
        if (userRepository.addCredits(clerkId, credits) == 0) {
            throw new UserNotFoundException("User not found");
        }
    }
}
