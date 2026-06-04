package com.auth_service.unit.repository;

import com.auth_service.entity.User;
import com.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("UserRepository JPA Slice Tests")
class UserRepositoryTest {

    private final UserRepository userRepository;
    private final TestEntityManager entityManager;

    UserRepositoryTest(UserRepository userRepository, TestEntityManager entityManager) {
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    private User activeUser;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("active@example.com");
        activeUser.setUsername("activeuser");
        activeUser.setPasswordHash("hashedpassword");
        activeUser.setFirstName("Active");
        activeUser.setLastName("User");
        activeUser.setDeleted(false);
        entityManager.persistAndFlush(activeUser);

        deletedUser = new User();
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setUsername("deleteduser");
        deletedUser.setPasswordHash("hashedpassword");
        deletedUser.setFirstName("Deleted");
        deletedUser.setLastName("User");
        deletedUser.setDeleted(true);
        entityManager.persistAndFlush(deletedUser);
    }

    @Test
    @DisplayName("findByEmail — Should return user when email matches and active")
    void findByEmail_ShouldReturnUser_WhenExists() {
        Optional<User> found = userRepository.findByEmail("active@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("activeuser");
    }

    @Test
    @DisplayName("findByIdAndDeletedFalse — Should not return user if soft-deleted")
    void findByIdAndDeletedFalse_ShouldReturnEmpty_WhenUserIsSoftDeleted() {
        Optional<User> foundActive = userRepository.findByIdAndDeletedFalse(activeUser.getId());
        Optional<User> foundDeleted = userRepository.findByIdAndDeletedFalse(deletedUser.getId());

        assertThat(foundActive).isPresent();
        assertThat(foundDeleted).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail — Should return true for existing email")
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        boolean activeExists = userRepository.existsByEmail("active@example.com");
        boolean fakeExists = userRepository.existsByEmail("fake@example.com");

        assertThat(activeExists).isTrue();
        assertThat(fakeExists).isFalse();
    }

    @Test
    @DisplayName("searchUsers — Should return matching users case-insensitively")
    void searchUsers_ShouldReturnMatchingUsers() {
        Page<User> result = userRepository.searchUsers("ACTIVE", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("active@example.com");
    }
}
