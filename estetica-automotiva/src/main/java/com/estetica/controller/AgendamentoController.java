package com.estetica.controller;

import com.estetica.model.Agendamento;
import com.estetica.model.Cliente;
import com.estetica.service.AgendamentoService;
import com.estetica.service.ClienteService;
import com.estetica.service.ServicoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/agendamento")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public AgendamentoController(AgendamentoService agendamentoService,
                                  ClienteService clienteService,
                                  ServicoService servicoService) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UserDetails user, Model model) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        model.addAttribute("agendamentos", agendamentoService.buscarPorCliente(cliente.getId()));
        return "agendamento/lista";
    }

    @GetMapping("/novo")
    public String novoForm(@AuthenticationPrincipal UserDetails user, Model model) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        model.addAttribute("agendamento", new Agendamento());
        model.addAttribute("servicos", servicoService.listarAtivos());
        model.addAttribute("placas", cliente.getPlacas());
        model.addAttribute("hoje", LocalDate.now());
        model.addAttribute("horarios", Collections.emptyList());
        return "agendamento/form";
    }

    // endpoint AJAX para buscar horários disponíveis dado serviço + data
    @GetMapping("/horarios-disponiveis")
    @ResponseBody
    public List<String> horariosDisponiveis(@RequestParam String servicoId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agendamentoService.horariosDisponiveis(data, servicoId);
    }

    @PostMapping("/novo")
    public String salvar(@AuthenticationPrincipal UserDetails user,
                          @ModelAttribute Agendamento agendamento,
                          @RequestParam String data,
                          @RequestParam String horario,
                          RedirectAttributes ra) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        try {
            agendamento.setClienteId(cliente.getId());
            agendamento.setClienteNome(cliente.getNome());
            agendamento.setData(LocalDate.parse(data));
            agendamento.setHorario(LocalTime.parse(horario));
            servicoService.buscarPorId(agendamento.getServicoId())
                    .ifPresent(s -> agendamento.setServicoNome(s.getNome()));
            agendamentoService.agendar(agendamento);
            ra.addFlashAttribute("sucesso", "Agendamento realizado com sucesso!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/agendamento/novo";
        }
        return "redirect:/agendamento";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id,
                              @AuthenticationPrincipal UserDetails user,
                              Model model) {
        Agendamento agendamento = agendamentoService.buscarPorId(id).orElseThrow();
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        if (!agendamento.getClienteId().equals(cliente.getId())) return "redirect:/agendamento";
        List<String> horarios = agendamentoService
                .horariosDisponiveisExcluindo(agendamento.getData(), agendamento.getServicoId(), id);
        model.addAttribute("agendamento", agendamento);
        model.addAttribute("servicos", servicoService.listarAtivos());
        model.addAttribute("placas", cliente.getPlacas());
        model.addAttribute("horarios", horarios);
        model.addAttribute("hoje", LocalDate.now());
        return "agendamento/form";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable String id,
                          @AuthenticationPrincipal UserDetails user,
                          @ModelAttribute Agendamento dados,
                          @RequestParam String data,
                          @RequestParam String horario,
                          RedirectAttributes ra) {
        Agendamento existente = agendamentoService.buscarPorId(id).orElseThrow();
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        if (!existente.getClienteId().equals(cliente.getId())) return "redirect:/agendamento";
        try {
            dados.setData(LocalDate.parse(data));
            dados.setHorario(LocalTime.parse(horario));
            agendamentoService.atualizar(id, dados);
            ra.addFlashAttribute("sucesso", "Agendamento atualizado.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/agendamento";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable String id,
                            @AuthenticationPrincipal UserDetails user,
                            RedirectAttributes ra) {
        Agendamento agendamento = agendamentoService.buscarPorId(id).orElseThrow();
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        if (!agendamento.getClienteId().equals(cliente.getId())) return "redirect:/agendamento";
        agendamentoService.cancelar(id);
        ra.addFlashAttribute("sucesso", "Agendamento cancelado.");
        return "redirect:/agendamento";
    }
}