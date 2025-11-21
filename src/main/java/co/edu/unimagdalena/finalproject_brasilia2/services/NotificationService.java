package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.PaymentMethod;

import java.math.BigDecimal;

public interface NotificationService {

    // Ticket notifications
    void sendTicketConfirmation(String phoneNumber, String passengerName, Long ticketId, String seatNumber, String qrCode, String route);

    void sendTicketCancellation(String phoneNumber, String passengerName, Long ticketId, BigDecimal refundAmount, PaymentMethod paymentMethod);

    // Parcel notifications
    void sendParcelConfirmation(String phoneNumber, String senderName, String parcelCode, String receiverName, String receiverPhone);

    void sendParcelInTransit(String phoneNumber, String receiverName, String parcelCode);

    void sendParcelCreated(String phoneNumber, String receiverName, String parcelCode);

    void sendParcelDelivered(String phoneNumber, String receiverName, String parcelCode);

    void sendParcelDeliveryFailed(String phoneNumber, String receiverName, String parcelCode, Long parcelId, String reason);

}

