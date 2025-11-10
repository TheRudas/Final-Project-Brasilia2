package co.edu.unimagdalena.finalproject_brasilia2.services.mappers;

import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.UserCreateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.UserResponse;
import co.edu.unimagdalena.finalproject_brasilia2.api.dto.UserDtos.UserUpdateRequest;
import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true) //Ignore for now
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "true")
    User toEntity(UserCreateRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phone", source = "phone")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    void patch(@MappingTarget User target, UserUpdateRequest changes);
}