package com.ifba.sipapi.user;

import com.ifba.sipapi.user.DTO.UserRegisterRequest;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component("authUserMapper")
public interface UserMapper {
    UserModel toModel(UserRegisterRequest dto);
}
