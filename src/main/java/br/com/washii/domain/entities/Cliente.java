package br.com.washii.domain.entities;

import br.com.washii.domain.enums.TipoUsuario;

import java.util.List;

public class Cliente extends Usuario{
    private String telefone;
    private List<Veiculo> veiculos;

    public Cliente() {
    }

    public Cliente(String nome, String email, String senha, Endereco endereco, TipoUsuario tipoUsuario) {
        super(nome, email, senha, endereco, tipoUsuario);
    }

    public Cliente(Long id, String nome, String email, String senha, TipoUsuario tipoUsuario, List<Veiculo> veiculos, String telefone, Endereco endereco) {
        super(id, nome, email, senha, endereco, tipoUsuario);
        this.veiculos = veiculos;
        this.telefone = telefone;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    

    public void addVeiculo(Veiculo veiculo){
        veiculos.add(veiculo);
    }

    public boolean removeVeiculo(Veiculo veiculo){
        return veiculos.remove(veiculo);
    }

    // [CORREÇÃO OBJECT STORAGE]: Removido atributo fotoPerfil duplicado da subclasse,
    // pois já existe e é herdado diretamente da superclasse Usuario (evitando sombreamento de atributo)

    public List<Veiculo> getAllVeiculos(){
        return veiculos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Negocio)) return false;
        // Como o ID está em Usuario, usamos o equals da classe pai
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Baseado no ID definido na classe Usuario
        return super.hashCode();
    }
}
