package org.example.auth_server.mapper;

import org.example.auth_server.dto.request.UserRegistrationDto;
import org.mapstruct.Mapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.keycloak.representations.idm.UserRepresentation;

@Mapper(componentModel = "spring")
public interface UserMapper {
  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "firstName", source = "firstName")
  @Mapping(target = "lastName", source = "lastName")
  UserRepresentation toUserRepresentation(UserRegistrationDto userRegistrationDto);
}
