package com.platform.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.platform.user_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA auto-generates the SQL for these methods
    // You just define the method name — no SQL needed!

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) FROM users WHERE email = ? (returns true/false)
    boolean existsByEmail(String email);
}
