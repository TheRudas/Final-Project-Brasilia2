package co.edu.unimagdalena.finalproject_brasilia2.services.impl;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.*;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.enums.UserRole;
import co.edu.unimagdalena.finalproject_brasilia2.domain.repositories.UserRepository;
import co.edu.unimagdalena.finalproject_brasilia2.exceptions.NotFoundException;
import co.edu.unimagdalena.finalproject_brasilia2.services.UserService;
import co.edu.unimagdalena.finalproject_brasilia2.services.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if(userRepository.findByEmail(request.email()).isPresent()){
            throw new IllegalArgumentException("User with this email already exists");
        }

        if(userRepository.findByPhone(request.phone()).isPresent()){
            throw new IllegalArgumentException("User with phone already exists");
        }

        User user = userMapper.toEntity(request);
        user.setCreatedAt(OffsetDateTime.now());
        user.setPasswordHash(request.password()); // Only for tests, hash later with security layer
        return userMapper.toResponse(userRepository.save(user));

    }

    @Override @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User %d not found".formatted(id)));
        userMapper.patch(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse get(Long id) {
        return userRepository.findById(id).map(userMapper::toResponse).orElseThrow(
                () -> new NotFoundException("User %d not found".formatted(id)));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User %d not found".formatted(id)));
        userRepository.delete(user);
    }

    @Override
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::toResponse).orElseThrow(
                () -> new NotFoundException("User with email \"%s\" not found".formatted(email)));
    }

    @Override
    public List<UserResponse> getByRole(UserRole role) {
        List<User> users = userRepository.findByRole(role);
        if(users.isEmpty())
            throw new NotFoundException("Any user has the \"%s\" role".formatted(role));
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    public UserResponse getByPhone(String phone) {
        return userRepository.findByPhone(phone).map(userMapper::toResponse).orElseThrow(
                () -> new NotFoundException("User with phone \"%s\" not found".formatted(phone)));
    }

    @Override
    public List<UserResponse> getActiveByRole(UserRole role) {
        List<User> users = userRepository.findByRoleAndStatus(role, true);
        if(users.isEmpty())
            throw new NotFoundException("No active users with this role");
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    public Page<UserResponse> getByStatus(boolean status, Pageable pageable) {
        Page<User> users = userRepository.findByStatus(status, pageable);
        if (users.isEmpty())
        {
            String replaceStatus = status ? "Active" : "Inactive";
            throw new NotFoundException("No users with status: \"%s\"".formatted(replaceStatus));
        }
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User with id: %d not found".formatted(id))
        );
        user.setStatus(false);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void activate(Long id) {
        var user = userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("User with id: %d not found".formatted(id))
        );
        user.setStatus(true);
        userRepository.save(user);
    }
}
