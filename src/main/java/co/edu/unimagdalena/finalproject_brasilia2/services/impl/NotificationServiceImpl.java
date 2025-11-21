package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.NotificationType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;
import co.edu.unimagdalena.finalproject_brasilia2.services.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@Profile({"dev", "test"})
public class NotificationServiceImpl implements NotificationService {

    // ==================== TICKET NOTIFICATIONS ====================

    @Override
    @Async
    public void sendTicketConfirmation(String phoneNumber, String passengerName, Long ticketId, String seatNumber, String qrCode, String route) {
        var message = """
            TICKET BUYING CONFIRMATION
            Hello %s, your ticket #%d has been confirmed!
            Route: %s
            Seat: %s
            QR Code: %s
            Show this QR code when boarding.
        """.formatted(passengerName, ticketId, route, seatNumber, qrCode);

        sendNotification(phoneNumber, NotificationType.TICKET_CONFIRMATION, message);
    }

    @Override
    @Async
    public void sendTicketCancellation(String phoneNumber, String passengerName, Long ticketId, BigDecimal refundAmount, PaymentMethod paymentMethod) {
        var message = """
            TICKET CANCELLATION
            Hello %s, your ticket #%d has been cancelled!
            Refund amount: $%s
            Original payment method: %s
            The refund will be processed in 3-5 business days.
        """.formatted(passengerName, ticketId, refundAmount, paymentMethod);

        sendNotification(phoneNumber, NotificationType.TICKET_CANCELLED, message);
    }

    @Override
    @Async
    public void sendParcelConfirmation(String phoneNumber, String senderName, String parcelCode, String receiverName, String receiverPhone) {
        var message = """
            PARCEL CONFIRMED
            Hello %s, your parcel with code '%s' has been confirmed!
            Receiver: %s (%s)
            You will be notified when it's ready for pickup.
        """.formatted(senderName, parcelCode, receiverName, receiverPhone);

        sendNotification(phoneNumber, NotificationType.PARCEL_CONFIRMATION, message);
    }

    @Override
    @Async
    public void sendParcelInTransit(String phoneNumber, String receiverName, String parcelCode) {
        var message = """
            PARCEL STATUS UPDATED
            Hello %s, your parcel with code '%s' is now in transit!
            We will notify you when it arrives at the destination.
        """.formatted(receiverName, parcelCode);

        sendNotification(phoneNumber, NotificationType.PARCEL_IN_TRANSIT, message);
    }

    @Override
    public void sendParcelCreated(String phoneNumber, String receiverName, String parcelCode) {
        var message = """
            PARCEL CREATED SUCCESSFULLY
            Hello %s, your parcel with code '%s' has been created!
            Thank you for using our service, rate 5 stars our try of web page.
        """.formatted(receiverName, parcelCode);

        sendNotification(phoneNumber, NotificationType.PARCEL_DELIVERED, message);
    }

    @Override
    @Async
    public void sendParcelDelivered(String phoneNumber, String receiverName, String parcelCode) {
        var message = """
            PARCEL DELIVERED
            Hello %s, your parcel with code '%s' has been delivered!
            Thank you for using our service.
        """.formatted(receiverName, parcelCode);

        sendNotification(phoneNumber, NotificationType.PARCEL_DELIVERED, message);
    }

    @Override
    @Async
    public void sendParcelDeliveryFailed(String phoneNumber, String receiverName, String parcelCode, Long parcelId, String reason) {
        var message = """
            PARCEL DELIVERY FAILED
            Hello %s, your parcel with code '%s' couldn't be delivered!
            Reason: %s
            For more info, you should search a PARCEL incident for parcel #%d
            Please contact us for assistance, please do not demand us.
        """.formatted(receiverName, parcelCode, reason, parcelId);

        sendNotification(phoneNumber, NotificationType.PARCEL_DELIVERY_FAIL, message);
    }

    // ==================== PRIVATE HELPER METHOD ====================

    private void sendNotification(String phone, NotificationType type, String message) {
        log.info("Sending WhatsApp Notification:");
        log.info("""
        ─────────────────────────────────────────────────
        To: {}
        Type: {}
        Message:
        {}
        ─────────────────────────────────────────────────
        """, phone, type, message);
    }
}

