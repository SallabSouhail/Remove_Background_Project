package com.removebg.service;

import com.removebg.dto.UserDTO;
import com.removebg.entity.UserEntity;

import java.util.Optional;

public interface UserService {
    UserDTO saveUser(UserDTO userDTO);
    Integer getUserCredits(String clerkId);
    boolean existsByClerkId(String clerkId);
    Optional<UserEntity> getUserByClerkId(String clerkId);
}
