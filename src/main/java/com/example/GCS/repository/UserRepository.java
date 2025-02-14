package com.example.GCS.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.GCS.model.User;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);
} 