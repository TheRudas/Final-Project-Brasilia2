package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ParcelDtos;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.Parcel;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.ParcelStatus;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.ParcelRepository;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.StopRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.ParcelService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ParcelMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository repository;
    private final StopRepository stopRepository;
    private final ParcelMapper mapper;

    // ========================= CREATE =========================
    @Override
    @Transactional
    public ParcelDtos.ParcelResponse create(ParcelDtos.ParcelCreateRequest request) {

        Parcel parcel = mapper.toEntity(request);

        // Asignar stops
        var fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new NotFoundException("Origin stop not found"));
        var toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new NotFoundException("Destination stop not found"));

        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);

        // Lógica de negocio: precio inicial base
        parcel.setPrice(BigDecimal.valueOf(5.00)); // ejemplo

        // Estado inicial del paquete
        parcel.setStatus(ParcelStatus.CREATED);

        // Generar OTP de 8 dígitos
        parcel.setDeliveryOtp(generateOtp());

        Parcel saved = repository.save(parcel);

        return mapper.toResponse(saved);
    }

    // ========================= UPDATE =========================
    @Override
    @Transactional
    public ParcelDtos.ParcelResponse update(Long id, ParcelDtos.ParcelUpdateRequest request) {

        Parcel parcel = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));

        // Patch automático
        mapper.patch(parcel, request);

        // Actualizar stops si vienen en el request
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

    // ========================= GET =========================
    @Override
    public ParcelDtos.ParcelResponse get(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));
    }

    // ========================= DELETE =========================
    @Override
    @Transactional
    public void delete(Long id) {
        Parcel parcel = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Parcel %d not found".formatted(id)));

        repository.delete(parcel);
    }

    // ========================= FILTERS =========================
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

    // ========================= UTILS =========================
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(100_000_000); // 8 dígitos
        return String.format("%08d", number);
    }
}
