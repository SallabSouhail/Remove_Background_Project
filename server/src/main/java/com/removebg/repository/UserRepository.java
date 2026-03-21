package com.removebg.repository;

import com.removebg.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByClerkId(String clerkId);
    boolean existsByClerkId(String clerkId);

    @Query("SELECT u.credits FROM UserEntity u WHERE u.clerkId = :clerkId")
    Optional<Integer> findCreditsByClerkId(@Param("clerkId") String clerkId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.credits = u.credits - :cost WHERE u.clerkId = :clerkId AND u.credits >= :cost")
    int deductCreditsIfEnough(@Param("clerkId") String clerkId, @Param("cost") Integer cost);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.credits = u.credits + :cost WHERE u.clerkId = :clerkId")
    int refundCredits(@Param("clerkId") String clerkId, @Param("cost") Integer cost);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.credits = u.credits + :credits WHERE u.clerkId = :clerkId")
    int addCredits(@Param("clerkId") String clerkId, @Param("credits") Integer credits);
}
