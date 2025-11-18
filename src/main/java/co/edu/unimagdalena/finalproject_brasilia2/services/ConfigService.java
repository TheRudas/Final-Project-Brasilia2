package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.ConfigDtos.*;

import java.util.List;

public interface ConfigService {
    ConfigResponse create(ConfigCreateRequest request);
    ConfigResponse update(String key, ConfigUpdateRequest request);
    ConfigResponse get(String key);
    void delete(String key);

    List<ConfigResponse> listAll();
}