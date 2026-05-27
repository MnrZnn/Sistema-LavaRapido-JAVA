package com.estetica.repository;

import com.estetica.model.Servico;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServicoRepository extends MongoRepository<Servico, String> {
    List<Servico> findByAtivoTrue();
}
