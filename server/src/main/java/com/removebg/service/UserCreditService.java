package com.removebg.service;

public interface UserCreditService {
    boolean consumeCredits(String clerkId, int credits);
    void refundCredits(String clerkId, int credits);
    void addCreditsOrThrow(String clerkId, int credits);
}
