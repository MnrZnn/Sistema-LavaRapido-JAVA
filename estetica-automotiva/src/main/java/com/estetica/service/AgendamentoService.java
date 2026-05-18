package com.estetica.service;

import com.estetica.model.Agendamento;
import com.estetica.model.Agendamento.StatusAgendamento;
import com.estetica.repository.AgendamentoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repo;

    public AgendamentoService(AgendamentoRepository repo) { this.repo = repo; }

    public void agendar(Agendamento agendamento) {
        validarDisponibilidade(agendamento.getData(), agendamento.getHorario(), null);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        repo.save(agendamento);
    }

    // agendamento avulso criado pelo gestor — dados do cliente não são persistidos
    public void agendarAvulso(Agendamento agendamento) {
        validarDisponibilidade(agendamento.getData(), agendamento.getHorario(), null);
        agendamento.setAvulso(true);
        agendamento.setClienteId(null);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        repo.save(agendamento);
    }

    public void atualizar(String id, Agendamento dados) {
        Agendamento existente = repo.findById(id).orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
        boolean horarioMudou = !existente.getData().equals(dados.getData())
                || !existente.getHorario().equals(dados.getHorario());
        if (horarioMudou) validarDisponibilidade(dados.getData(), dados.getHorario(), id);
        existente.setData(dados.getData());
        existente.setHorario(dados.getHorario());
        existente.setPlaca(dados.getPlaca());
        existente.setObservacao(dados.getObservacao());
        if (dados.getValorCobrado() != null) existente.setValorCobrado(dados.getValorCobrado());
        repo.save(existente);
    }

    public void cancelar(String id) {
        repo.findById(id).ifPresent(a -> { a.setStatus(StatusAgendamento.CANCELADO); repo.save(a); });
    }

    public void confirmar(String id) {
        repo.findById(id).ifPresent(a -> { a.setStatus(StatusAgendamento.CONFIRMADO); repo.save(a); });
    }

    public void concluir(String id) {
        repo.findById(id).ifPresent(a -> { a.setStatus(StatusAgendamento.CONCLUIDO); repo.save(a); });
    }

    // receita de um período (apenas concluídos)
    public BigDecimal receitaPeriodo(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO)
                .stream()
                .map(a -> a.getValorCobrado() != null ? a.getValorCobrado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // contagem de agendamentos concluídos por dia em um período
    public Map<LocalDate, Long> contagemPorDia(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO)
                .stream()
                .collect(Collectors.groupingBy(Agendamento::getData, Collectors.counting()));
    }

    // receita agrupada por mês: chave = "yyyy-MM"
    public Map<String, BigDecimal> receitaPorMes(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO)
                .stream()
                .collect(Collectors.groupingBy(
                        a -> a.getData().getYear() + "-" + String.format("%02d", a.getData().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO,
                                a -> a.getValorCobrado() != null ? a.getValorCobrado() : BigDecimal.ZERO,
                                BigDecimal::add)
                ));
    }

    private void validarDisponibilidade(LocalDate data, LocalTime horario, String idIgnorar) {
        boolean ocupado = repo.existsByDataAndHorarioAndStatusNot(data, horario, StatusAgendamento.CANCELADO);
        if (ocupado) {
            if (idIgnorar == null) throw new RuntimeException("Horário já ocupado.");
            Optional<Agendamento> conflito = repo.findByDataOrderByHorarioAsc(data)
                    .stream()
                    .filter(a -> a.getHorario().equals(horario)
                            && !a.getId().equals(idIgnorar)
                            && a.getStatus() != StatusAgendamento.CANCELADO)
                    .findFirst();
            if (conflito.isPresent()) throw new RuntimeException("Horário já ocupado.");
        }
    }

    public Optional<Agendamento> buscarPorId(String id) { return repo.findById(id); }
    public List<Agendamento> buscarPorCliente(String clienteId) { return repo.findByClienteId(clienteId); }
    public List<Agendamento> agendaDia(LocalDate data) { return repo.findByDataOrderByHorarioAsc(data); }
    public List<Agendamento> agendaSemana(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenOrderByDataAscHorarioAsc(inicio, fim);
    }
    public List<Agendamento> listarTodos() { return repo.findAll(); }
    public List<Agendamento> concluidos(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO);
    }
}