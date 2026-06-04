package com.auth_service.service.impl;

import com.auth_service.dto.request.CreatePermissionRequest;
import com.auth_service.dto.response.PermissionResponse;
import com.auth_service.entity.Permission;
import com.auth_service.entity.Role;
import com.auth_service.exception.PermissionNotFoundException;
import com.auth_service.exception.RoleNotFoundException;
import com.auth_service.repository.PermissionRepository;
import com.auth_service.repository.RoleRepository;
import com.auth_service.service.PermissionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionServiceImpl(
            final PermissionRepository permissionRepository,
            final RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if (permissionRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Permission '" + request.name() + "' already exists");
        }
        Permission permission = new Permission();
        permission.setName(request.name());
        permission.setDescription(request.description());
        permission.setResource(request.resource());
        permission.setAction(request.action());
        permission.setCreatedBy("SYSTEM");
        return toResponse(permissionRepository.save(permission));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionResponse> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public void assignPermissionToRole(String roleId, String permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Override
    public void removePermissionFromRole(String roleId, String permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        role.getPermissions().remove(permission);
        roleRepository.save(role);
    }

    private PermissionResponse toResponse(Permission p) {
        return new PermissionResponse(p.getId(), p.getName(), p.getDescription(), p.getResource(), p.getAction());
    }
}
