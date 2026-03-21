package com.removebg.service.impl;

import com.removebg.dto.UserDTO;
import com.removebg.dto.mapper.UserMapper;
import com.removebg.entity.UserEntity;
import com.removebg.repository.UserRepository;
import com.removebg.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDTO saveUser(UserDTO userDTO) {
        Optional<UserEntity> optionalUser = userRepository.findByClerkId(userDTO.getClerkId());

        if (optionalUser.isPresent()) {
            return updateExistingUser(optionalUser.get(), userDTO);
        }

        return createNewUser(userDTO);
    }

    /**
     * Update an already existing user with values coming from the input DTO.
     * Business rule:
     * - if user already exists, returned DTO must have isCreated = false
     */
    private UserDTO updateExistingUser(UserEntity existingUser, UserDTO userDTO) {
        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhotoUrl(userDTO.getPhotoUrl());

        UserEntity savedUser = userRepository.save(existingUser);
        UserDTO savedUserDto = UserMapper.mapToDTO(savedUser);
        savedUserDto.setIsCreated(false);

        return savedUserDto;
    }

    /**
     * Create a new user from the input DTO.
     * Business rule:
     * - if user did not exist before, returned DTO must have isCreated = true
     */
    private UserDTO createNewUser(UserDTO userDTO) {
        UserEntity userEntity = UserMapper.mapToEntity(userDTO);
        UserEntity savedUser = userRepository.save(userEntity);
        UserDTO savedUserDto = UserMapper.mapToDTO(savedUser);
        savedUserDto.setIsCreated(true);

        return savedUserDto;
    }

    @Override
    public Integer getUserCredits(String clerkId) {
        return userRepository.findCreditsByClerkId(clerkId).orElse(0);
    }

    @Override
    public boolean existsByClerkId(String clerkId) {
        return userRepository.existsByClerkId(clerkId);
    }

    @Override
    public Optional<UserEntity> getUserByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId);
    }
}