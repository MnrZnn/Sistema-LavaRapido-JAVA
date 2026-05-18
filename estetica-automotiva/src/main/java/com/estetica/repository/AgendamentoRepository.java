package com.estetica.repository;

import com.estetica.model.Agendamento;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AgendamentoRepository extends MongoRepository<Agendamento, String> {
    List<Agendamento> findByClienteId(String clienteId);
    List<Agendamento> findByData(LocalDate data);
    List<Agendamento> findByDataBetween(LocalDate inicio, LocalDate fim);
    boolean existsByDataAndHorarioAndStatusNot(LocalDate data, LocalTime horario, Agendamento.StatusAgendamento status);
    List<Agendamento> findByDataBetweenOrderByDataAscHorarioAsc(LocalDate inicio, LocalDate fim);
    List<Agendamento> findByDataOrderByHorarioAsc(LocalDate data);
    List<Agendamento> findByClienteIdAndStatusNot(String clienteId, Agendamento.StatusAgendamento status);
    List<Agendamento> findByStatus(Agendamento.StatusAgendamento status);
    List<Agendamento> findByDataBetweenAndStatus(LocalDate inicio, LocalDate fim, Agendamento.StatusAgendamento status);
}