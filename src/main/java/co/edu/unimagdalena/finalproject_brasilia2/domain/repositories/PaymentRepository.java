package co.edu.unimagdalena.finalproject_brasilia2.domain.repositories;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByTicketId(Long ticketId);

    List<Payment> findByConfirmed(boolean confirmed);

    @Query("SELECT p FROM Payment p WHERE p.confirmed = false AND p.failureReason IS NULL")
    List<Payment> findPendingPayments();

    @Query("SELECT p FROM Payment p WHERE p.failureReason IS NOT NULL")
    List<Payment> findFailedPayments();

    @Query("SELECT p FROM Payment p WHERE DATE(p.createdAt) = :date")
    List<Payment> findByDate(LocalDate date);

    boolean existsByTransactionId(String transactionId);
}