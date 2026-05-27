package com.estetica.controller;

import com.estetica.model.Cliente;
import com.estetica.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final ClienteService clienteService;

    public AuthController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastroPage(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@Valid @ModelAttribute("cliente") Cliente cliente,
                            BindingResult result,
                            RedirectAttributes ra,
                            Model model) {
        if (result.hasErrors()) {
            return "auth/cadastro";
        }
        try {
            clienteService.cadastrar(cliente);
            ra.addFlashAttribute("sucesso", "Conta criada com sucesso! Faça o login.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            return "auth/cadastro";
        }
    }

    @GetMapping("/termos")
    public String termos() {
        return "termos";
    }
}