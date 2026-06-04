package com.auth_service.service;

import com.auth_service.dto.request.UpdateProfileRequest;
import com.auth_service.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUser(String userId);

    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);

    void deactivateAccount(String userId, String password);
}
