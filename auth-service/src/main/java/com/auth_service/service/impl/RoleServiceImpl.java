package com.auth_service.service.impl;

import com.auth_service.dto.request.CreateRoleRequest;
import com.auth_service.dto.response.RoleResponse;
import com.auth_service.entity.Role;
import com.auth_service.entity.User;
import com.auth_service.exception.RoleNotFoundException;
import com.auth_service.exception.UserNotFoundException;
import com.auth_service.repository.RoleRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleServiceImpl(
            final RoleRepository roleRepository,
            final UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Role '" + request.name() + "' already exists");
        }
        Role role = new Role();
        role.setName(request.name().toUpperCase());
        role.setDescription(request.description());
        role.setCreatedBy("SYSTEM");
        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public void assignRoleToUser(String userId, String roleId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    public void removeRoleFromUser(String userId, String roleId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    public void deleteRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        if (role.isSystemRole()) {
            throw new IllegalStateException("System roles cannot be deleted");
        }
        roleRepository.delete(role);
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.isSystemRole());
    }
}
