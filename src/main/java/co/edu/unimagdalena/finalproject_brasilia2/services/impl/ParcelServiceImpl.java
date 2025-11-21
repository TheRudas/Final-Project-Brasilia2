package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Incident;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentEntityType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.IncidentType;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.ParcelRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.IncidentRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ParcelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final IncidentRepository incidentRepository;
    private final ParcelMapper mapper;
    private final ConfigService configService;

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse create(ParcelDtos.ParcelCreateRequest request) {
        Parcel parcel = mapper.toEntity(request);

        var fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("Origin stop not found"));
        var toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("Destination stop not found"));

        // deben pertenecer a la misma ruta
        if (!fromStop.getRoute().getId().equals(toStop.getRoute().getId())) {
            throw new IllegalArgumentException(
                    "Origin and destination stops must belong to the same route. " +
                    "Origin route: %d, Destination route: %d"
                    .formatted(fromStop.getRoute().getId(), toStop.getRoute().getId())
            );
        }

        if (fromStop.getOrder() >= toStop.getOrder()) {
            throw new IllegalArgumentException(
                    "Origin stop (order: %d) must come before destination stop (order: %d)"
                    .formatted(fromStop.getOrder(), toStop.getOrder())
            );
        }

        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);


        parcel.setCode(generateUniqueParcelCode());


        BigDecimal dynamicPrice = calculateParcelPrice(fromStop, toStop);
        parcel.setPrice(dynamicPrice);

        parcel.setStatus(ParcelStatus.CREATED);
        parcel.setDeliveryOtp(generateOtp());

        Parcel saved = repository.save(parcel);


        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse update(Long id, ParcelDtos.ParcelUpdateRequest request) {
        var parcel = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));

        mapper.patch(parcel, request);

        if (request.fromStopId() != null) {
            var fs = stopRepository.findById(request.fromStopId())
                    .orElseThrow(() -> new NotFoundException("Origin stop not found"));
            parcel.setFromStop(fs);
        }

        if (request.toStopId() != null) {
            var ts = stopRepository.findById(request.toStopId())
                    .orElseThrow(() -> new NotFoundException("Destination stop not found"));
            parcel.setToStop(ts);
        }

        return mapper.toResponse(repository.save(parcel));
    }

    @Override
    public ParcelDtos.ParcelResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var parcel = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
        repository.delete(parcel);
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getBySenderName(String senderName) {
        return mapper.toResponseList(repository.findBySenderName(senderName));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getBySenderPhone(String phone) {
        return mapper.toResponseList(repository.findBySenderPhone(phone));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getByReceiverName(String name) {
        return mapper.toResponseList(repository.findByReceiverName(name));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getByReceiverPhone(String phone) {
        return mapper.toResponseList(repository.findByReceiverPhone(phone));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getByFromStopId(Long stopId) {
        return mapper.toResponseList(repository.findByFromStopId(stopId));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getByToStopId(Long stopId) {
        return mapper.toResponseList(repository.findByToStopId(stopId));
    }

    @Override
    public ParcelDtos.ParcelResponse getByCode(String code) {
        return repository.findByCode(code)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel with code %s not found".formatted(code)));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> getByStatus(ParcelStatus status) {
        var parcels = repository.findByStatus(status);
        if (parcels.isEmpty()) {
            throw new NotFoundException("No parcels found with status: " + status);
        }
        return mapper.toResponseList(parcels);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse deliverParcel(Long parcelId, String otp) {
        var parcel = repository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(parcelId)));

        if (parcel.getStatus() != ParcelStatus.IN_TRANSIT) {
            throw new IllegalStateException(
                    "Only parcels IN_TRANSIT can be delivered. Current status: %s"
                            .formatted(parcel.getStatus())
            );
        }

        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("OTP cannot be empty");
        }

        if (!otp.equals(parcel.getDeliveryOtp())) {

            // Mark as FAILED and create incident
            parcel.setStatus(ParcelStatus.FAILED);
            repository.save(parcel);

            var incident = Incident.builder()
                    .entityType(IncidentEntityType.PARCEL)
                    .entityId(parcelId)
                    .type(IncidentType.DELIVERY_FAIL)
                    .note("Delivery failed: Invalid OTP provided. Expected: [HIDDEN], Received: " + otp)
                    .createdAt(OffsetDateTime.now())
                    .build();
            incidentRepository.save(incident);


            throw new IllegalArgumentException("Invalid OTP - Parcel marked as FAILED and incident created");
        }

        parcel.setStatus(ParcelStatus.DELIVERED);

        log.info("✅ Parcel delivered: ID={}, Code={}, Receiver={}",
                parcelId, parcel.getCode(), parcel.getReceiverName());

        return mapper.toResponse(repository.save(parcel));
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse assignToTrip(Long parcelId, Long tripId) {
        var parcel = repository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(parcelId)));

        tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("Trip %d not found".formatted(tripId)));

        if (parcel.getStatus() != ParcelStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED parcels can be assigned. Current status: %s"
                            .formatted(parcel.getStatus())
            );
        }

        parcel.setStatus(ParcelStatus.IN_TRANSIT);

        log.info("Parcel assigned to trip: ParcelID={}, TripID={}", parcelId, tripId);

        return mapper.toResponse(repository.save(parcel));
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse updateStatus(Long parcelId, ParcelStatus newStatus) {
        var parcel = repository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(parcelId)));

        var oldStatus = parcel.getStatus();
        validateStatusTransition(oldStatus, newStatus);

        parcel.setStatus(newStatus);

        log.info("Parcel status updated: ID={}, {} -> {}", parcelId, oldStatus, newStatus);

        return mapper.toResponse(repository.save(parcel));
    }

    @Override
    public List<ParcelDtos.ParcelResponse> listParcelsForDelivery(Long stopId) {
        var parcels = repository.findByToStopIdAndStatus(stopId, ParcelStatus.IN_TRANSIT);

        if (parcels.isEmpty()) {
            log.info("No parcels pending delivery at stop {}", stopId);
            return List.of();
        }

        log.info("Found {} parcels for delivery at stop {}", parcels.size(), stopId);
        return mapper.toResponseList(parcels);
    }

    private void validateStatusTransition(ParcelStatus from, ParcelStatus to) {
        if (from == to) {
            throw new IllegalStateException("Parcel is already in status " + to);
        }

        switch (from) {
            case CREATED:
                if (to != ParcelStatus.IN_TRANSIT && to != ParcelStatus.FAILED) {
                    throw new IllegalStateException(
                            "CREATED can only go to IN_TRANSIT or FAILED"
                    );
                }
                break;

            case IN_TRANSIT:
                if (to != ParcelStatus.DELIVERED && to != ParcelStatus.FAILED) {
                    throw new IllegalStateException(
                            "IN_TRANSIT can only go to DELIVERED or FAILED"
                    );
                }
                break;

            case DELIVERED:
                throw new IllegalStateException("DELIVERED is a final state");

            case FAILED:
                throw new IllegalStateException("FAILED is a final state");

            default:
                throw new IllegalStateException("Unknown status: " + from);
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(100_000_000);
        return String.format("%08d", number);
    }


    private String generateUniqueParcelCode() {
        var random = new SecureRandom();
        var today = java.time.LocalDate.now();
        var datePrefix = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String code;
        do {
            var randomSuffix = random.nextInt(100000); // 0-99999
            code = "PCL-%s-%05d".formatted(datePrefix, randomSuffix);
        } while (repository.existsByCode(code));

        return code;
    }

    /**
     * Calculate dynamic price based on distance between stops
     * Base price per stop: $2.50
     */
    private BigDecimal calculateParcelPrice(co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop fromStop,
                                           co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Stop toStop) {
        var basePricePerStop = configService.getValue("PARCEL_BASE_PRICE_PER_STOP");
        var stopsDistance = Math.abs(toStop.getOrder() - fromStop.getOrder());

        if (stopsDistance == 0) stopsDistance = 1; // Minimum

        var totalPrice = basePricePerStop.multiply(BigDecimal.valueOf(stopsDistance));

        log.debug("💰 Parcel price calculated: {} stops × ${} = ${}",
                stopsDistance, basePricePerStop, totalPrice);

        return totalPrice;
    }
}
