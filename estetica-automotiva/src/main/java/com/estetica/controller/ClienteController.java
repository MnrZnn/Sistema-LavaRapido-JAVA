package com.estetica.controller;

import com.estetica.model.Agendamento;
import com.estetica.model.Cliente;
import com.estetica.service.AgendamentoService;
import com.estetica.service.ClienteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;
    private final AgendamentoService agendamentoService;

    public ClienteController(ClienteService clienteService, AgendamentoService agendamentoService) {
        this.clienteService = clienteService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        List<Agendamento> agendamentos = agendamentoService.buscarPorCliente(cliente.getId());
        long ativos = agendamentos.stream()
                .filter(a -> a.getStatus() != Agendamento.StatusAgendamento.CANCELADO
                        && a.getStatus() != Agendamento.StatusAgendamento.CONCLUIDO)
                .count();
        model.addAttribute("cliente", cliente);
        model.addAttribute("totalAgendamentos", agendamentos.size());
        model.addAttribute("agendamentosAtivos", ativos);
        model.addAttribute("ultimos", agendamentos.stream().limit(5).toList());
        return "cliente/dashboard";
    }

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal UserDetails user, Model model) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow();
        model.addAttribute("cliente", cliente);
        return "cliente/perfil";
    }

    @PostMapping("/perfil")
    public String atualizarPerfil(@AuthenticationPrincipal UserDetails user,
                                   @ModelAttribute Cliente dados,
                                   RedirectAttributes ra) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        try {
            clienteService.atualizar(cliente.getId(), dados);
            ra.addFlashAttribute("sucesso", "Dados atualizados com sucesso.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/cliente/perfil";
    }

    @PostMapping("/perfil/senha")
    public String alterarSenha(@AuthenticationPrincipal UserDetails user,
                                @RequestParam String senhaAtual,
                                @RequestParam String novaSenha,
                                RedirectAttributes ra) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername()).orElseThrow();
        try {
            clienteService.alterarSenha(cliente.getId(), senhaAtual, novaSenha);
            ra.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/cliente/perfil";
    }
}
