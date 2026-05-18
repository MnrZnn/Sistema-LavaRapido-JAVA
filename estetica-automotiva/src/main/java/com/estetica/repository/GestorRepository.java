package com.estetica.repository;

import com.estetica.model.Gestor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GestorRepository extends MongoRepository<Gestor, String> {
    Optional<Gestor> findByEmail(String email);
    boolean existsByEmail(String email);
}
