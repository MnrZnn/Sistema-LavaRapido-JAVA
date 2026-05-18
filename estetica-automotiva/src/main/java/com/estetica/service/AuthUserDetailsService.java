package com.estetica.service;

import com.estetica.model.Cliente;
import com.estetica.model.Gestor;
import com.estetica.repository.ClienteRepository;
import com.estetica.repository.GestorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepo;
    private final GestorRepository gestorRepo;

    public AuthUserDetailsService(ClienteRepository clienteRepo, GestorRepository gestorRepo) {
        this.clienteRepo = clienteRepo;
        this.gestorRepo = gestorRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Gestor> gestor = gestorRepo.findByEmail(email);
        if (gestor.isPresent()) {
            return new User(
                    gestor.get().getEmail(),
                    gestor.get().getSenha(),
                    List.of(new SimpleGrantedAuthority("ROLE_GESTOR"))
            );
        }

        Optional<Cliente> cliente = clienteRepo.findByEmail(email);
        if (cliente.isPresent()) {
            return new User(
                    cliente.get().getEmail(),
                    cliente.get().getSenha(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
            );
        }

        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }
}
