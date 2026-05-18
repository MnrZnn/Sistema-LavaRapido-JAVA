package com.estetica.service;

import com.estetica.model.Servico;
import com.estetica.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository repo;

    public ServicoService(ServicoRepository repo) { this.repo = repo; }

    public void salvar(Servico servico) { repo.save(servico); }

    public void deletar(String id) { repo.deleteById(id); }

    public void desativar(String id) {
        repo.findById(id).ifPresent(s -> {
            s.setAtivo(false);
            repo.save(s);
        });
    }

    public Optional<Servico> buscarPorId(String id) { return repo.findById(id); }
    public List<Servico> listarAtivos() { return repo.findByAtivoTrue(); }
    public List<Servico> listarTodos() { return repo.findAll(); }
}
