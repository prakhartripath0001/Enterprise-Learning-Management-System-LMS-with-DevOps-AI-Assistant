package com.auth_service.service;

import com.auth_service.dto.request.CreatePermissionRequest;
import com.auth_service.dto.response.PermissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PermissionService {

    PermissionResponse createPermission(CreatePermissionRequest request);

    Page<PermissionResponse> getAllPermissions(Pageable pageable);

    void assignPermissionToRole(String roleId, String permissionId);

    void removePermissionFromRole(String roleId, String permissionId);
}
