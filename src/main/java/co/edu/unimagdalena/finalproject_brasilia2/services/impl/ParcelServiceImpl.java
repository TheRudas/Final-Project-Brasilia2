package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.ParcelRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.TripRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ParcelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final ParcelMapper mapper;

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse create(ParcelDtos.ParcelCreateRequest request) {
        Parcel parcel = mapper.toEntity(request);

        var fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("Origin stop not found"));
        var toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("Destination stop not found"));

        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);
        parcel.setPrice(BigDecimal.valueOf(5.00));
        parcel.setStatus(ParcelStatus.CREATED);
        parcel.setDeliveryOtp(generateOtp());

        Parcel saved = repository.save(parcel);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse update(Long id, ParcelDtos.ParcelUpdateRequest request) {
        Parcel parcel = repository.findById(id)
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

        Parcel updated = repository.save(parcel);
        return mapper.toResponse(updated);
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
        Parcel parcel = repository.findById(id)
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
        List<Parcel> parcels = repository.findByStatus(status);
        if (parcels.isEmpty()) {
            throw new NotFoundException("No parcels found with status: " + status);
        }
        return mapper.toResponseList(parcels);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse deliverParcel(Long parcelId, String otp) {
        Parcel parcel = repository.findById(parcelId)
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
            log.warn("❌ Failed delivery attempt for Parcel {}: Invalid OTP", parcelId);
            throw new IllegalArgumentException("Invalid OTP");
        }

        parcel.setStatus(ParcelStatus.DELIVERED);
        Parcel delivered = repository.save(parcel);

        log.info("✅ Parcel delivered: ID={}, Code={}, Receiver={}",
                parcelId, parcel.getCode(), parcel.getReceiverName());

        return mapper.toResponse(delivered);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse assignToTrip(Long parcelId, Long tripId) {
        Parcel parcel = repository.findById(parcelId)
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
        Parcel updated = repository.save(parcel);

        log.info("📦 Parcel assigned to trip: ParcelID={}, TripID={}", parcelId, tripId);

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ParcelDtos.ParcelResponse updateStatus(Long parcelId, ParcelStatus newStatus) {
        Parcel parcel = repository.findById(parcelId)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(parcelId)));

        ParcelStatus oldStatus = parcel.getStatus();
        validateStatusTransition(oldStatus, newStatus);

        parcel.setStatus(newStatus);
        Parcel updated = repository.save(parcel);

        log.info("Parcel status updated: ID={}, {} -> {}", parcelId, oldStatus, newStatus);

        return mapper.toResponse(updated);
    }

    @Override
    public List<ParcelDtos.ParcelResponse> listParcelsForDelivery(Long stopId) {
        List<Parcel> parcels = repository.findByToStopIdAndStatus(stopId, ParcelStatus.IN_TRANSIT);

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
}
