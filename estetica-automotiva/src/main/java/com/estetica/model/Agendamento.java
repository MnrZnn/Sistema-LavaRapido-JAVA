package com.estetica.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "agendamentos")
public class Agendamento {

    @Id
    private String id;

    private String clienteId;

    @NotBlank
    private String clienteNome;

    @NotBlank
    private String servicoId;

    private String servicoNome;

    @NotNull
    private LocalDate data;

    @NotNull
    private LocalTime horario;

    @NotBlank
    private String placa;

    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    private String observacao;

    // agendamento criado pelo gestor sem cadastro de cliente
    private boolean avulso = false;

    // valor efetivamente cobrado (pode diferir do preço do serviço)
    private BigDecimal valorCobrado;

    public enum StatusAgendamento {
        PENDENTE, CONFIRMADO, CONCLUIDO, CANCELADO
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getServicoId() { return servicoId; }
    public void setServicoId(String servicoId) { this.servicoId = servicoId; }

    public String getServicoNome() { return servicoNome; }
    public void setServicoNome(String servicoNome) { this.servicoNome = servicoNome; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public boolean isAvulso() { return avulso; }
    public void setAvulso(boolean avulso) { this.avulso = avulso; }

    public BigDecimal getValorCobrado() { return valorCobrado; }
    public void setValorCobrado(BigDecimal valorCobrado) { this.valorCobrado = valorCobrado; }
}