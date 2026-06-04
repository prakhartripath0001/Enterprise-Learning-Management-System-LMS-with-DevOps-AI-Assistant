package com.auth_service.unit.repository;

import com.auth_service.entity.Role;
import com.auth_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("RoleRepository JPA Slice Tests")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setName("ROLE_MANAGER");
        testRole.setDescription("Manager of courses");
        testRole.setSystemRole(false);
        entityManager.persistAndFlush(testRole);
    }

    @Test
    @DisplayName("findByName — Should return role when name matches")
    void findByName_ShouldReturnRole_WhenExists() {
        Optional<Role> found = roleRepository.findByName("ROLE_MANAGER");
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Manager of courses");
    }

    @Test
    @DisplayName("existsByName — Should return true for existing role name")
    void existsByName_ShouldReturnTrue_WhenNameExists() {
        boolean managerExists = roleRepository.existsByName("ROLE_MANAGER");
        boolean adminExists = roleRepository.existsByName("ROLE_ADMIN");

        assertThat(managerExists).isTrue();
        assertThat(adminExists).isFalse();
    }
}
