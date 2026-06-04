package com.auth_service.service;

import com.auth_service.dto.request.CreateRoleRequest;
import com.auth_service.dto.response.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    Page<RoleResponse> getAllRoles(Pageable pageable);

    void assignRoleToUser(String userId, String roleId);

    void removeRoleFromUser(String userId, String roleId);

    void deleteRole(String roleId);
}
