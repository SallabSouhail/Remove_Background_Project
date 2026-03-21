package com.removebg.dto.mapper;

import com.removebg.dto.UserDTO;
import com.removebg.entity.UserEntity;
import org.apache.catalina.User;

public class UserMapper {
    public static UserEntity mapToEntity(UserDTO dto) {
        UserEntity userEntity = UserEntity.builder()
                .clerkId(dto.getClerkId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .credits(dto.getCredits())
                .photoUrl(dto.getPhotoUrl())
                .build();
        return userEntity;
    }

    public static UserDTO mapToDTO(UserEntity userEntity) {
        UserDTO dto = UserDTO.builder()
                .clerkId(userEntity.getClerkId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .credits(userEntity.getCredits())
                .photoUrl(userEntity.getPhotoUrl())
                .build();
        return dto;
    }
}
