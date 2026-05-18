package com.estetica.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "clientes")
public class Cliente {

    @Id
    private String id;

    @NotBlank
    private String nome;

    @NotBlank
    @Indexed(unique = true)
    private String cpf;

    @NotBlank
    private String telefone;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String senha;

    private List<String> placas = new ArrayList<>();

    private boolean ativo = true;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public List<String> getPlacas() { return placas; }
    public void setPlacas(List<String> placas) { this.placas = placas; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getCpfMascarado() {
        if (cpf == null || cpf.length() < 11) return "***.***.***-**";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() < 11) return "***.***.***-**";
        return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
    }
}