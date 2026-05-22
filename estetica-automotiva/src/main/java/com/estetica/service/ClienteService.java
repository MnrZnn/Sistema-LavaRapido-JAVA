package com.estetica.service;

import com.estetica.model.Cliente;
import com.estetica.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final PasswordEncoder encoder;

    public ClienteService(ClienteRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void cadastrar(Cliente cliente) {
        if (repo.existsByEmail(cliente.getEmail()))
            throw new RuntimeException("E-mail já cadastrado.");
        if (repo.existsByCpf(cliente.getCpf()))
            throw new RuntimeException("CPF já cadastrado.");
        cliente.setSenha(encoder.encode(cliente.getSenha()));
        repo.save(cliente);
    }

    public void atualizar(String id, Cliente dados) {
        Cliente existente = repo.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        existente.setNome(dados.getNome());
        existente.setTelefone(dados.getTelefone());
        existente.setPlacas(dados.getPlacas());
        repo.save(existente);
    }

    public void alterarSenha(String id, String senhaAtual, String novaSenha) {
        Cliente c = repo.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
        if (!encoder.matches(senhaAtual, c.getSenha()))
            throw new RuntimeException("Senha atual incorreta.");
        c.setSenha(encoder.encode(novaSenha));
        repo.save(c);
    }

    public void deletar(String id) {
        repo.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) { return repo.findById(id); }
    public Optional<Cliente> buscarPorEmail(String email) { return repo.findByEmail(email); }
    public List<Cliente> listarAtivos() { return repo.findAll().stream().filter(Cliente::isAtivo).toList(); }
    public List<Cliente> listarTodos() { return repo.findAll(); }
}
