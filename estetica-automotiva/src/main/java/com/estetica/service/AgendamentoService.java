package com.estetica.service;

import com.estetica.model.Agendamento;
import com.estetica.model.Agendamento.StatusAgendamento;
import com.estetica.model.Servico;
import com.estetica.repository.AgendamentoRepository;
import com.estetica.repository.ServicoRepository;
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
    private final ServicoRepository servicoRepo;

    public AgendamentoService(AgendamentoRepository repo, ServicoRepository servicoRepo) {
        this.repo = repo;
        this.servicoRepo = servicoRepo;
    }

    public void agendar(Agendamento agendamento) {
        int duracao = getDuracao(agendamento.getServicoId());
        validarDisponibilidade(agendamento.getData(), agendamento.getHorario(), duracao, null);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        repo.save(agendamento);
    }

    public void agendarAvulso(Agendamento agendamento) {
        int duracao = getDuracao(agendamento.getServicoId());
        validarDisponibilidade(agendamento.getData(), agendamento.getHorario(), duracao, null);
        agendamento.setAvulso(true);
        agendamento.setClienteId(null);
        agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        repo.save(agendamento);
    }

    public void atualizar(String id, Agendamento dados) {
        Agendamento existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));
        boolean horarioMudou = !existente.getData().equals(dados.getData())
                || !existente.getHorario().equals(dados.getHorario());
        if (horarioMudou) {
            int duracao = getDuracao(existente.getServicoId());
            validarDisponibilidade(dados.getData(), dados.getHorario(), duracao, id);
        }
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

    // retorna horários livres do dia considerando duração do serviço informado
    public List<String> horariosDisponiveis(LocalDate data, String servicoId) {
        int duracaoNovo = getDuracao(servicoId);
        List<Agendamento> ocupados = repo.findByDataAndStatusNot(data, StatusAgendamento.CANCELADO);

        List<String> todos = List.of(
                "08:00","09:00","10:00","11:00","12:00",
                "13:00","14:00","15:00","16:00","17:00"
        );

        return todos.stream().filter(slot -> {
            LocalTime inicio = LocalTime.parse(slot);
            LocalTime fim = inicio.plusHours(duracaoNovo);
            // verifica se o novo intervalo [inicio, fim) conflita com algum agendamento existente
            for (Agendamento a : ocupados) {
                int durExist = getDuracao(a.getServicoId());
                LocalTime eInicio = a.getHorario();
                LocalTime eFim = eInicio.plusHours(durExist);
                if (inicio.isBefore(eFim) && fim.isAfter(eInicio)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    // mesma lógica mas excluindo um agendamento específico (para edição)
    public List<String> horariosDisponiveisExcluindo(LocalDate data, String servicoId, String idIgnorar) {
        int duracaoNovo = getDuracao(servicoId);
        List<Agendamento> ocupados = repo.findByDataAndStatusNot(data, StatusAgendamento.CANCELADO)
                .stream()
                .filter(a -> !a.getId().equals(idIgnorar))
                .collect(Collectors.toList());

        List<String> todos = List.of(
                "08:00","09:00","10:00","11:00","12:00",
                "13:00","14:00","15:00","16:00","17:00"
        );

        return todos.stream().filter(slot -> {
            LocalTime inicio = LocalTime.parse(slot);
            LocalTime fim = inicio.plusHours(duracaoNovo);
            for (Agendamento a : ocupados) {
                int durExist = getDuracao(a.getServicoId());
                LocalTime eInicio = a.getHorario();
                LocalTime eFim = eInicio.plusHours(durExist);
                if (inicio.isBefore(eFim) && fim.isAfter(eInicio)) return false;
            }
            return true;
        }).collect(Collectors.toList());
    }

    private void validarDisponibilidade(LocalDate data, LocalTime horario, int duracaoNovo, String idIgnorar) {
        LocalTime fimNovo = horario.plusHours(duracaoNovo);
        List<Agendamento> ocupados = repo.findByDataAndStatusNot(data, StatusAgendamento.CANCELADO)
                .stream()
                .filter(a -> idIgnorar == null || !a.getId().equals(idIgnorar))
                .collect(Collectors.toList());

        for (Agendamento a : ocupados) {
            int durExist = getDuracao(a.getServicoId());
            LocalTime eInicio = a.getHorario();
            LocalTime eFim = eInicio.plusHours(durExist);
            if (horario.isBefore(eFim) && fimNovo.isAfter(eInicio)) {
                throw new RuntimeException("Horário conflita com agendamento existente das "
                        + eInicio + " às " + eFim + ".");
            }
        }
    }

    private int getDuracao(String servicoId) {
        if (servicoId == null) return 1;
        return servicoRepo.findById(servicoId)
                .map(Servico::getDuracaoHoras)
                .orElse(1);
    }

    public BigDecimal receitaPeriodo(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO)
                .stream()
                .map(a -> a.getValorCobrado() != null ? a.getValorCobrado() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<LocalDate, Long> contagemPorDia(LocalDate inicio, LocalDate fim) {
        return repo.findByDataBetweenAndStatus(inicio, fim, StatusAgendamento.CONCLUIDO)
                .stream()
                .collect(Collectors.groupingBy(Agendamento::getData, Collectors.counting()));
    }

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