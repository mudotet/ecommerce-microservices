package com.mudotet.ecommerce.auth.mapper;

import com.mudotet.ecommerce.auth.dto.request.UserRegistrationRequest;
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
  UserRepresentation toUserRepresentation(UserRegistrationRequest userRegistrationRequest);
}
