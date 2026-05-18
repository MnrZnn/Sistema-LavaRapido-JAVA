package com.estetica.service;

import com.estetica.model.Gestor;
import com.estetica.repository.GestorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GestorService {

    private final GestorRepository repo;
    private final PasswordEncoder encoder;

    public GestorService(GestorRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void cadastrar(Gestor gestor) {
        if (repo.existsByEmail(gestor.getEmail()))
            throw new RuntimeException("E-mail já cadastrado.");
        gestor.setSenha(encoder.encode(gestor.getSenha()));
        repo.save(gestor);
    }

    public Optional<Gestor> buscarPorEmail(String email) { return repo.findByEmail(email); }
    public Optional<Gestor> buscarPorId(String id) { return repo.findById(id); }
}
