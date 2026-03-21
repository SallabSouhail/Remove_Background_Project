package com.removebg.payment.repository;

import com.removebg.payment.entity.PaymentEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentEntity p WHERE p.id = :paymentId")
    Optional<PaymentEntity> findByIdForUpdate(@Param("paymentId") Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentEntity p WHERE p.stripeCheckoutSessionId = :sessionId")
    Optional<PaymentEntity> findByStripeCheckoutSessionIdForUpdate(@Param("sessionId") String sessionId);
}
