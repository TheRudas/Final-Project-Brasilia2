package co.edu.unimagdalena.finalproject_brasilia2.services.impl;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.ConfigRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.ConfigService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.ConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {
    private final ConfigRepository configRepository;
    private final ConfigMapper mapper;

    @Override
    @Transactional
    public ConfigResponse create(ConfigCreateRequest request) {
        if (configRepository.existsByKey(request.key())) {
            throw new IllegalStateException("Config with key %s already exists".formatted(request.key()));
        }
        var config = mapper.toEntity(request);
        return mapper.toResponse(configRepository.save(config));
    }

    @Override
    @Transactional
    public ConfigResponse update(String key, ConfigUpdateRequest request) {
        var config = configRepository.findById(key)
                .orElseThrow(() -> new NotFoundException("Config with key %s not found".formatted(key)));

        mapper.patch(config, request);
        return mapper.toResponse(configRepository.save(config));
    }

    @Override
    public ConfigResponse get(String key) {
        return configRepository.findById(key)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Config with key %s not found".formatted(key)));
    }

    @Override
    @Transactional
    public void delete(String key) {
        var config = configRepository.findById(key)
                .orElseThrow(() -> new NotFoundException("Config with key %s not found".formatted(key)));
        configRepository.delete(config);
    }

    @Override
    public BigDecimal getValue(String key) {
        var value = configRepository.findById(key).orElseThrow(() -> new NotFoundException("Config with key %s not found".formatted(key))).getValue();
        return new BigDecimal(value);
    }

    @Override
    public List<ConfigResponse> listAll() {
        return configRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}