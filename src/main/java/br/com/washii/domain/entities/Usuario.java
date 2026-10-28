package br.com.washii.domain.entities;

import java.util.Objects;

import br.com.washii.domain.enums.TipoUsuario;

public abstract class Usuario {
    private Long id;
    private String email;
    private String senha;
    private String nome;
    private Endereco endereco;
    private TipoUsuario tipoUsuario;

    // Construtores (para diferentes situações)
    public Usuario() {}

    public Usuario(String nome, String email, String senha, Endereco endereco, TipoUsuario tipoUsuario) {
        this.id = null;
        setNome(nome);
        setEmail(email);
        setSenha(senha);
        setEndereco(endereco);
        this.tipoUsuario = tipoUsuario;
    }

    public Usuario(Long id, String nome, String email, String senha, Endereco endereco,TipoUsuario tipoUsuario) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.endereco = endereco;
        this.tipoUsuario = tipoUsuario;
    }

    // Getters e Setters
    public Long getId(){ return this.id; }
    public String getEmail() { return this.email; }
    public String getSenha() { return this.senha; }
    public String getNome() { return this.nome; }
    public Endereco getEndereco() { return this.endereco; }
    public TipoUsuario getTipoUsuario() { return this.tipoUsuario; }

    public void setId(Long id){
        this.id = id;
    }
    public void setEmail(String email){
        if (email == null || email.isBlank()){
            throw new  IllegalArgumentException("O email não pode ser vazio.");
        }
        this.email = email.trim();
    }
    public void setSenha(String senha){
        if (senha == null || senha.isBlank()){
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }
        this.senha = senha.trim();
    }
    
    public void setNome(String nome){
        if (nome == null || nome.isBlank()){
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public void setEndereco(Endereco endereco){
        this.endereco = endereco;
    }

    public void setTipoUsuario(TipoUsuario tipo){
        this.tipoUsuario = tipo;
    }
    private String fotoPerfil;

public String getFotoPerfil() {
    return fotoPerfil;
}

public void setFotoPerfil(String fotoPerfil) {
    this.fotoPerfil = fotoPerfil;
}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;

        // Se o ID for nulo, eles só são iguais se forem o MESMO objeto (já verificado acima)
        if (this.id == null || usuario.id == null) {
            return false;
        }

        return Objects.equals(getId(), usuario.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "email='" + email + '\'' +
                ", id=" + id +
                ", nome='" + nome + '\'' +
                ", tipoUsuario=" + tipoUsuario +
                '}';
    }
}