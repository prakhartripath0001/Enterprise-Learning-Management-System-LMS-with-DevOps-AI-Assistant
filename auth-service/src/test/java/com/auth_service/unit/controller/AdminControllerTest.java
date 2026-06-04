package com.auth_service.unit.controller;

import com.auth_service.controller.AdminController;
import com.auth_service.dto.request.CreatePermissionRequest;
import com.auth_service.dto.request.CreateRoleRequest;
import com.auth_service.dto.response.PermissionResponse;
import com.auth_service.dto.response.RoleResponse;
import com.auth_service.dto.response.UserProfileResponse;
import com.auth_service.exception.GlobalExceptionHandler;
import com.auth_service.service.PermissionService;
import com.auth_service.service.RoleService;
import com.auth_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController MockMvc Tests")
class AdminControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private UserService userService;
    @Mock private RoleService roleService;
    @Mock private PermissionService permissionService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/admin/users/{id} — Success")
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        UserProfileResponse response = new UserProfileResponse("user-123", "john.doe@example.com", "johndoe", "John", "Doe", Set.of("ROLE_STUDENT"), true);

        when(userService.getCurrentUser("user-123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/users/{userId}/roles — Success")
    void assignRole_ShouldReturn200_WhenRoleAssigned() throws Exception {
        doNothing().when(roleService).assignRoleToUser("user-123", "role-456");

        mockMvc.perform(post("/api/v1/admin/users/user-123/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleId\":\"role-456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Role assigned successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/roles — Success")
    void createRole_ShouldReturn21_WhenPayloadIsValid() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("ROLE_MODERATOR", "Content Moderator");
        RoleResponse response = new RoleResponse("role-456", "ROLE_MODERATOR", "Content Moderator", false);

        when(roleService.createRole(any(CreateRoleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("ROLE_MODERATOR"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/roles — Success")
    void getAllRoles_ShouldReturn200() throws Exception {
        RoleResponse response = new RoleResponse("role-123", "ROLE_ADMIN", "Admin role", true);
        Page<RoleResponse> page = new PageImpl<>(Collections.singletonList(response));

        when(roleService.getAllRoles(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].name").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/permissions — Success")
    void createPermission_ShouldReturn201() throws Exception {
        CreatePermissionRequest request = new CreatePermissionRequest("course:write", "Can write courses", "course", "write");
        PermissionResponse response = new PermissionResponse("perm-123", "course:write", "Can write courses", "course", "write");

        when(permissionService.createPermission(any(CreatePermissionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("course:write"));
    }
}
