package com.auth_service.controller;

import com.auth_service.dto.request.CreatePermissionRequest;
import com.auth_service.dto.request.CreateRoleRequest;
import com.auth_service.dto.response.ApiResponse;
import com.auth_service.dto.response.PermissionResponse;
import com.auth_service.dto.response.RoleResponse;
import com.auth_service.dto.response.UserProfileResponse;
import com.auth_service.service.PermissionService;
import com.auth_service.service.RoleService;
import com.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin", description = "User management, role and permission administration")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final PermissionService permissionService;

    public AdminController(
            final UserService userService,
            final RoleService roleService,
            final PermissionService permissionService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
    }

    // ── User Management ───────────────────────────────────────────────────────

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("User retrieved", userService.getCurrentUser(id)));
    }

    // ── Role Assignment ───────────────────────────────────────────────────────

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign a role to a user")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable String userId,
            @RequestBody AssignRoleRequest request) {
        roleService.assignRoleToUser(userId, request.roleId());
        return ResponseEntity.ok(ApiResponse.success("Role assigned successfully"));
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove a role from a user")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable String userId,
            @PathVariable String roleId) {
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("Role removed successfully"));
    }

    // ── Role Management ───────────────────────────────────────────────────────

    @PostMapping("/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created", roleService.createRole(request)));
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all roles")
    public ResponseEntity<ApiResponse<Page<RoleResponse>>> getAllRoles(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Roles retrieved", roleService.getAllRoles(pageable)));
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete a custom role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("Role deleted"));
    }

    // ── Permission Management ─────────────────────────────────────────────────

    @PostMapping("/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Permission created", permissionService.createPermission(request)));
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get all permissions")
    public ResponseEntity<ApiResponse<Page<PermissionResponse>>> getAllPermissions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Permissions retrieved",
                permissionService.getAllPermissions(pageable)));
    }

    @PostMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Assign a permission to a role")
    public ResponseEntity<ApiResponse<Void>> assignPermission(
            @PathVariable String roleId,
            @RequestBody AssignPermissionRequest request) {
        permissionService.assignPermissionToRole(roleId, request.permissionId());
        return ResponseEntity.ok(ApiResponse.success("Permission assigned to role"));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove a permission from a role")
    public ResponseEntity<ApiResponse<Void>> removePermission(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        permissionService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission removed from role"));
    }

    // ── Inner request records (no separate DTO file needed for one-field bodies)
    record AssignRoleRequest(String roleId) {}
    record AssignPermissionRequest(String permissionId) {}
}
