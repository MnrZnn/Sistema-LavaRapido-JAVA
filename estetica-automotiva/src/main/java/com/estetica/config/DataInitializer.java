package com.estetica.config;

import com.estetica.model.Gestor;
import com.estetica.model.Servico;
import com.estetica.repository.GestorRepository;
import com.estetica.repository.ServicoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final GestorRepository gestorRepo;
    private final ServicoRepository servicoRepo;
    private final PasswordEncoder encoder;

    public DataInitializer(GestorRepository gestorRepo, ServicoRepository servicoRepo, PasswordEncoder encoder) {
        this.gestorRepo = gestorRepo;
        this.servicoRepo = servicoRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (gestorRepo.findByEmail("gestor@estetica.com").isEmpty()) {
            Gestor g = new Gestor();
            g.setNome("Administrador");
            g.setEmail("gestor@estetica.com");
            g.setSenha(encoder.encode("gestor123"));
            gestorRepo.save(g);
        }

        if (servicoRepo.findByAtivoTrue().isEmpty()) {
            criarServico("Lavagem Simples",       "Lavagem externa com água e sabão.",                    new BigDecimal("40.00"),  1);
            criarServico("Lavagem Completa",      "Lavagem interna e externa com aspiração.",             new BigDecimal("80.00"),  2);
            criarServico("Lavagem com Cera",      "Lavagem completa com aplicação de cera protetora.",   new BigDecimal("120.00"), 3);
            criarServico("Polimento",             "Polimento para remoção de riscos superficiais.",      new BigDecimal("200.00"), 1);
            criarServico("Higienização Interna",  "Limpeza profunda do interior do veículo.",            new BigDecimal("180.00"), 2);
        }
    }

    private void criarServico(String nome, String desc, BigDecimal preco, int duracaoHoras) {
        Servico s = new Servico();
        s.setNome(nome);
        s.setDescricao(desc);
        s.setPreco(preco);
        s.setDuracaoHoras(duracaoHoras);
        servicoRepo.save(s);
    }
}