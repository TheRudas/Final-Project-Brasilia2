package co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums;

public enum NotificationType {
    // Ticket notifications
    TICKET_CONFIRMATION,
    TICKET_CANCELLED,

    // Parcel notifications
    PARCEL_CONFIRMATION,
    PARCEL_IN_TRANSIT,
    PARCEL_READY_FOR_PICKUP,
    PARCEL_DELIVERED,
    PARCEL_DELIVERY_FAIL,
}

