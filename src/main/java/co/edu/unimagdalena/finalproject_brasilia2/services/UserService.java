package co.edu.unimagdalena.finalproject_brasilia2.services;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse update(Long id, UserUpdateRequest request);
    UserResponse get(Long id);
    UserResponse getByEmail(String email);
    List<UserResponse> getByRole(UserRole role);
    UserResponse getByPhone(String phone);
    List<UserResponse> getActiveByRole(UserRole role);
    Page<UserResponse> getByStatus(boolean status, Pageable pageable);

    //I think user cannot be deleted for the referential integrity, so I could do this...
    void deactivate(Long id);
    void activate(Long id);
}
