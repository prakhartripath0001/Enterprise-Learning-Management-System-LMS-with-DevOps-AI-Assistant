package com.auth_service.service.impl;

import com.auth_service.dto.request.UpdateProfileRequest;
import com.auth_service.dto.response.UserProfileResponse;
import com.auth_service.entity.User;
import com.auth_service.exception.InvalidCredentialsException;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.exception.UsernameAlreadyExistsException;
import com.auth_service.repository.RefreshTokenRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            final UserRepository userRepository,
            final RefreshTokenRepository refreshTokenRepository,
            final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String userId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return toProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (userRepository.existsByUsernameAndIdNot(request.username(), userId)) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setUsername(request.username());
        user.setUpdatedBy(userId);

        return toProfileResponse(userRepository.save(user));
    }

    @Override
    public void deactivateAccount(String userId, String password) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        user.setDeleted(true);
        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedBy(userId);
        userRepository.save(user);

        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private UserProfileResponse toProfileResponse(User user) {
        var roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        var permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                roles,
                permissions,
                user.getEmailVerifiedAt() != null,
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }
}
