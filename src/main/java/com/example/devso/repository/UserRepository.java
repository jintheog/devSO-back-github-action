package com.example.devso.repository;

import com.example.devso.entity.AuthProvider;
import com.example.devso.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);


//    boolean existsByEmail(String email);
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM users WHERE username = :username", nativeQuery = true)
    long existsByUsername(@Param("username")String username);

    @Query(value = "SELECT * FROM users WHERE provider = :provider AND providerId = :providerId", nativeQuery = true)
    Optional<User> findByProviderAndProviderId(@Param("provider")AuthProvider provider, @Param("providerId")String providerId);


}
