package com.estetica.controller;

import com.estetica.model.Agendamento;
import com.estetica.model.Servico;
import com.estetica.service.AgendamentoService;
import com.estetica.service.ClienteService;
import com.estetica.service.ServicoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gestor")
public class GestorController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public GestorController(AgendamentoService agendamentoService,
                              ClienteService clienteService,
                              ServicoService servicoService) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate hoje = LocalDate.now();
        model.addAttribute("agendaHoje", agendamentoService.agendaDia(hoje));
        model.addAttribute("totalClientes", clienteService.listarAtivos().size());
        model.addAttribute("totalServicos", servicoService.listarAtivos().size());
        model.addAttribute("totalAgendamentos", agendamentoService.listarTodos().size());
        return "gestor/dashboard";
    }

    // ---- Clientes ----

    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("clientes", clienteService.listarAtivos());
        return "gestor/clientes";
    }

    @PostMapping("/clientes/{id}/excluir")
    public String excluirCliente(@PathVariable String id, RedirectAttributes ra) {
        clienteService.deletar(id);
        ra.addFlashAttribute("sucesso", "Cliente removido.");
        return "redirect:/gestor/clientes";
    }

    // ---- Serviços ----

    @GetMapping("/servicos")
    public String servicos(Model model) {
        model.addAttribute("servicos", servicoService.listarTodos());
        if (!model.containsAttribute("servico")) model.addAttribute("servico", new Servico());
        return "gestor/servicos";
    }

    @PostMapping("/servicos/salvar")
    public String salvarServico(@ModelAttribute Servico servico, RedirectAttributes ra) {
        servicoService.salvar(servico);
        ra.addFlashAttribute("sucesso", "Serviço salvo com sucesso.");
        return "redirect:/gestor/servicos";
    }

    @GetMapping("/servicos/{id}/editar")
    public String editarServicoForm(@PathVariable String id, Model model) {
        Servico s = servicoService.buscarPorId(id).orElseThrow();
        model.addAttribute("servico", s);
        model.addAttribute("servicos", servicoService.listarTodos());
        return "gestor/servicos";
    }

    @PostMapping("/servicos/{id}/desativar")
    public String desativarServico(@PathVariable String id, RedirectAttributes ra) {
        servicoService.desativar(id);
        ra.addFlashAttribute("sucesso", "Serviço desativado.");
        return "redirect:/gestor/servicos";
    }

    @PostMapping("/servicos/{id}/reativar")
    public String reativarServico(@PathVariable String id, RedirectAttributes ra) {
        servicoService.reativar(id);
        ra.addFlashAttribute("sucesso", "Serviço reativado.");
        return "redirect:/gestor/servicos";
    }

    @PostMapping("/servicos/{id}/excluir")
    public String excluirServico(@PathVariable String id, RedirectAttributes ra) {
        servicoService.deletar(id);
        ra.addFlashAttribute("sucesso", "Serviço excluído definitivamente.");
        return "redirect:/gestor/servicos";
    }

    // ---- Agenda ----

    @GetMapping("/agenda/dia")
    public String agendaDia(@RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
                             Model model) {
        if (data == null) data = LocalDate.now();
        model.addAttribute("agendamentos", agendamentoService.agendaDia(data));
        model.addAttribute("data", data);
        return "gestor/agenda-dia";
    }

    @GetMapping("/agenda/semana")
    public String agendaSemana(@RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                Model model) {
        if (inicio == null) inicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate fim = inicio.plusDays(6);
        model.addAttribute("agendamentos", agendamentoService.agendaSemana(inicio, fim));
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        return "gestor/agenda-semana";
    }

    @PostMapping("/agendamentos/{id}/confirmar")
    public String confirmar(@PathVariable String id, RedirectAttributes ra) {
        agendamentoService.confirmar(id);
        ra.addFlashAttribute("sucesso", "Agendamento confirmado.");
        return "redirect:/gestor/agenda/dia";
    }

    @PostMapping("/agendamentos/{id}/concluir")
    public String concluir(@PathVariable String id, RedirectAttributes ra) {
        agendamentoService.concluir(id);
        ra.addFlashAttribute("sucesso", "Agendamento concluído.");
        return "redirect:/gestor/agenda/dia";
    }

    @PostMapping("/agendamentos/{id}/cancelar")
    public String cancelar(@PathVariable String id, RedirectAttributes ra) {
        agendamentoService.cancelar(id);
        ra.addFlashAttribute("sucesso", "Agendamento cancelado.");
        return "redirect:/gestor/agenda/dia";
    }

    @GetMapping("/agendamentos/{id}/editar")
    public String editarAgendamento(@PathVariable String id, Model model) {
        Agendamento a = agendamentoService.buscarPorId(id).orElseThrow();
        List<String> horarios = agendamentoService
                .horariosDisponiveisExcluindo(a.getData(), a.getServicoId(), id);
        model.addAttribute("agendamento", a);
        model.addAttribute("servicos", servicoService.listarAtivos());
        model.addAttribute("horarios", horarios);
        model.addAttribute("hoje", LocalDate.now());
        return "gestor/agendamento-form";
    }

    @PostMapping("/agendamentos/{id}/editar")
    public String salvarEdicaoAgendamento(@PathVariable String id,
                                           @ModelAttribute Agendamento dados,
                                           @RequestParam String data,
                                           @RequestParam String horario,
                                           RedirectAttributes ra) {
        try {
            dados.setData(LocalDate.parse(data));
            dados.setHorario(java.time.LocalTime.parse(horario));
            agendamentoService.atualizar(id, dados);
            ra.addFlashAttribute("sucesso", "Agendamento atualizado.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/gestor/agenda/dia";
    }

    // ---- Agendamento Avulso ----

    @GetMapping("/agendamentos/avulso")
    public String avulsoForm(Model model) {
        model.addAttribute("agendamento", new Agendamento());
        model.addAttribute("servicos", servicoService.listarAtivos());
        model.addAttribute("hoje", LocalDate.now());
        return "gestor/agendamento-avulso";
    }

    @PostMapping("/agendamentos/avulso")
    public String salvarAvulso(@ModelAttribute Agendamento agendamento,
                                @RequestParam String data,
                                @RequestParam String horario,
                                RedirectAttributes ra) {
        try {
            agendamento.setData(LocalDate.parse(data));
            agendamento.setHorario(java.time.LocalTime.parse(horario));
            servicoService.buscarPorId(agendamento.getServicoId()).ifPresent(s -> {
                agendamento.setServicoNome(s.getNome());
                if (agendamento.getValorCobrado() == null) agendamento.setValorCobrado(s.getPreco());
            });
            agendamentoService.agendarAvulso(agendamento);
            ra.addFlashAttribute("sucesso", "Agendamento avulso criado com sucesso.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/gestor/agendamentos/avulso";
        }
        return "redirect:/gestor/agenda/dia";
    }

    // ---- Estatísticas ----

    @GetMapping("/estatisticas")
    public String estatisticas(@RequestParam(required = false) Integer ano,
                                @RequestParam(required = false) Integer mes,
                                Model model) {
        if (ano == null) ano = LocalDate.now().getYear();
        if (mes == null) mes = LocalDate.now().getMonthValue();

        LocalDate inicioMes = LocalDate.of(ano, mes, 1);
        LocalDate fimMes = YearMonth.of(ano, mes).atEndOfMonth();
        LocalDate inicioSemana = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate fimSemana = inicioSemana.plusDays(6);

        Map<String, BigDecimal> receitaMensal = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String chave = ym.getYear() + "-" + String.format("%02d", ym.getMonthValue());
            receitaMensal.put(chave, agendamentoService.receitaPeriodo(ym.atDay(1), ym.atEndOfMonth()));
        }

        model.addAttribute("receitaMes", agendamentoService.receitaPeriodo(inicioMes, fimMes));
        model.addAttribute("receitaSemana", agendamentoService.receitaPeriodo(inicioSemana, fimSemana));
        model.addAttribute("totalMes", agendamentoService.concluidos(inicioMes, fimMes).size());
        model.addAttribute("totalSemana", agendamentoService.concluidos(inicioSemana, fimSemana).size());
        model.addAttribute("totalClientes", clienteService.listarAtivos().size());
        model.addAttribute("totalGeral", agendamentoService.listarTodos().size());
        model.addAttribute("receitaMensal", receitaMensal);
        model.addAttribute("lavagensNaSemana", agendamentoService.contagemPorDia(inicioSemana, fimSemana));
        model.addAttribute("anoSel", ano);
        model.addAttribute("mesSel", mes);
        model.addAttribute("inicioSemana", inicioSemana);
        model.addAttribute("fimSemana", fimSemana);
        return "gestor/estatisticas";
    }
}