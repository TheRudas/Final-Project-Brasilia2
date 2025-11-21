package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.services.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("Notification Service Tests")
class NotificationServiceTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private String testPhone;
    private String testPassengerName;
    private String testSenderName;
    private String testReceiverName;
    private String testReceiverPhone;

    @BeforeEach
    void setUp() {
        testPhone = "1234567890";
        testPassengerName = "John Doe";
        testSenderName = "Alice Sender";
        testReceiverName = "Bob Receiver";
        testReceiverPhone = "0987654321";
    }

    // ==================== TICKET NOTIFICATION TESTS ====================

    @Test
    @DisplayName("Should send ticket confirmation without errors")
    void shouldSendTicketConfirmation() {
        assertThatCode(() -> notificationService.sendTicketConfirmation(
                testPhone,
                testPassengerName,
                1L,
                "A1",
                "QR-12345678",
                "Bogotá - Medellín"
        )).doesNotThrowAnyException();
    }


    @Test
    @DisplayName("Should send ticket cancellation without errors")
    void shouldSendTicketCancellation() {
        assertThatCode(() -> notificationService.sendTicketCancellation(
                testPhone,
                testPassengerName,
                1L,
                new BigDecimal("50000"),
                PaymentMethod.CARD
        )).doesNotThrowAnyException();
    }


    // ==================== PARCEL NOTIFICATION TESTS ====================

    @Test
    @DisplayName("Should send parcel confirmation without errors")
    void shouldSendParcelConfirmation() {
        assertThatCode(() -> notificationService.sendParcelConfirmation(
                testPhone,
                testSenderName,
                "PARCEL-001",
                testReceiverName,
                testReceiverPhone
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should send parcel in transit notification without errors")
    void shouldSendParcelInTransit() {
        assertThatCode(() -> notificationService.sendParcelInTransit(
                testReceiverPhone,
                testReceiverName,
                "PARCEL-001"
        )).doesNotThrowAnyException();
    }



    @Test
    @DisplayName("Should send parcel delivered notification without errors")
    void shouldSendParcelDelivered() {
        assertThatCode(() -> notificationService.sendParcelDelivered(
                testReceiverPhone,
                testReceiverName,
                "PARCEL-001"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should send parcel delivery failed without errors")
    void shouldSendParcelDeliveryFailed() {
        assertThatCode(() -> notificationService.sendParcelDeliveryFailed(
                testReceiverPhone,
                testReceiverName,
                "PARCEL-001",
                1L,
                "Receiver not found"
        )).doesNotThrowAnyException();
    }



    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle null phone number gracefully")
    void shouldHandleNullPhoneNumber() {
        assertThatCode(() -> notificationService.sendTicketConfirmation(
                null,
                testPassengerName,
                1L,
                "A1",
                "QR-12345678",
                "Bogotá - Medellín"
        )).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty strings gracefully")
    void shouldHandleEmptyStrings() {
        assertThatCode(() -> notificationService.sendTicketConfirmation(
                "",
                "",
                1L,
                "",
                "",
                ""
        )).doesNotThrowAnyException();
    }

}

