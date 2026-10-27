package br.com.washii.presentation.screens.perfil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import br.com.washii.domain.entities.Endereco;
import br.com.washii.domain.entities.Negocio;
import br.com.washii.domain.entities.Usuario;
import br.com.washii.domain.exceptions.NegocioException;
import br.com.washii.infra.session.Sessao;
import br.com.washii.presentation.utils.AvisoUtils;
import br.com.washii.service.SupabaseStorageService;
import br.com.washii.service.UsuarioService;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PerfilNegocioController {

    private UsuarioService usuarioService;

    @FXML private Button btnEditar, btnCancelar;
    @FXML private Circle circleFoto;
    @FXML private FontIcon iconCamera;
    @FXML private StackPane paneFoto;
    @FXML private TextFlow containerAviso;
    
    @FXML private TextField txtBairro, txtCapacidade, txtCep, txtCidade, 
                            txtCnpj, txtEmail, txtEstado, txtHoraFim, txtHoraInicio, 
                            txtNomeNegocio, txtNumero, txtRazaoSocial, 
                            txtRua, txtDuracaoServico;

    private List<TextField> todosCampos;
    private boolean modoEdicao = false;
    private Usuario usuarioLogado;

    // [CORREÇÃO OBJECT STORAGE]: Guarda o arquivo de imagem selecionado para envio ao Supabase ao salvar
    private File arquivoFotoSelecionada;

    public PerfilNegocioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;

    }

    @FXML
    public void initialize() {
        todosCampos = List.of(
            txtBairro, txtCapacidade, txtCep, txtCidade, txtCnpj, 
            txtEmail, txtEstado, txtHoraFim, txtHoraInicio, txtNomeNegocio, 
            txtNumero, txtRazaoSocial, txtRua, txtDuracaoServico
        );

        
        configurarModoEdicao(false);
        carregarDados(buscarUsuarioLogado());
    }

    /**
     * A logica pra mudar entre editavel ou nçao ta aquii
     */
    @FXML
    void handleAlternarEdicao(ActionEvent event) {
        if (modoEdicao) {
            boolean sucesso = salvarAlteracoes();
            
            configurarModoEdicao(!sucesso);
            modoEdicao = !sucesso;
            
        } else {
            configurarModoEdicao(true);
            modoEdicao = true;
        }
    }

    @FXML
    void handleCancelarEdicao(ActionEvent event) {
        // [CORREÇÃO OBJECT STORAGE]: Limpa a foto selecionada se o usuário cancelar a edição
        this.arquivoFotoSelecionada = null;
        carregarDados(buscarUsuarioLogado());
        configurarModoEdicao(false);
        modoEdicao = false;
    }

    /**
     * pra gerenciar a troca de foto de perfil usando o FileChooser.
     */
    @FXML
    void handleSelecionarFoto(MouseEvent event) {
        if (!modoEdicao) return; 

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Logo do Negócio");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        File arquivo = fileChooser.showOpenDialog(circleFoto.getScene().getWindow());

        if (arquivo != null) {
            Image imagem = new Image(arquivo.toURI().toString());
            circleFoto.setFill(new ImagePattern(imagem));
            // [CORREÇÃO OBJECT STORAGE]: Armazena o arquivo selecionado para realizar o upload ao salvar
            this.arquivoFotoSelecionada = arquivo;
        }
    }

    // configuração do modo edicao
    private void configurarModoEdicao(boolean editavel) {
        todosCampos.forEach(tf -> tf.setEditable(editavel));

        
        if (editavel) {
            btnEditar.setText("Salvar");
            btnEditar.getStyleClass().add("btn-salvar");
            iconCamera.setVisible(true);
            paneFoto.setCursor(Cursor.HAND);
            circleFoto.setOpacity(0.7);
        } else {
            btnEditar.setText("Editar");
            btnEditar.getStyleClass().removeAll("btn-salvar", "button-primary");
            btnEditar.getStyleClass().add("button-primary");
            iconCamera.setVisible(false);
            paneFoto.setCursor(Cursor.DEFAULT);
            circleFoto.setOpacity(1.0);
        }

        btnCancelar.setVisible(editavel);
        btnCancelar.setManaged(editavel);

    }

    private void carregarDados(Usuario logado) {
        
        if (logado instanceof Negocio negocio) {
            txtNomeNegocio.setText(negocio.getNome());
            txtEmail.setText(negocio.getEmail());
            
            // Se negocio não tiver nenhum endereço (null) então é criado um novo
            if (negocio.getEndereco() == null) { negocio.setEndereco(new Endereco()); }

            txtCep.setText(negocio.getEndereco().getCep());
            txtRua.setText(negocio.getEndereco().getRua());
            txtNumero.setText(negocio.getEndereco().getNumero());
            txtBairro.setText(negocio.getEndereco().getBairro());
            txtCidade.setText(negocio.getEndereco().getCidade());
            txtEstado.setText(negocio.getEndereco().getEstado());

            // carrega hora com formatacao
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            txtHoraInicio.setText(negocio.getInicioExpediente().format(formatter));
            txtHoraFim.setText(negocio.getFimExpediente().format(formatter));
            txtDuracaoServico.setText(negocio.getDuracaoMediaServico().format(formatter));

            txtCapacidade.setText(String.valueOf(negocio.getCapacidadeAtendimentoSimultaneo()));
            txtCnpj.setText(negocio.getCnpj());
            txtRazaoSocial.setText(negocio.getRazaoSocial());

            // [CORREÇÃO OBJECT STORAGE]: Carrega a foto do Supabase se existir URL salva;
            // caso contrário, exibe a imagem padrão (profile-placeholder.png)
            carregarFotoPerfil(negocio.getFotoPerfil());
        }
    }

    // [CORREÇÃO OBJECT STORAGE]: Método auxiliar para carregar a imagem do Supabase ou o placeholder padrão
    private void carregarFotoPerfil(String urlFoto) {
        try {
            if (urlFoto != null && !urlFoto.isBlank()) {
                Image imgRemota = new Image(urlFoto, false);
                if (!imgRemota.isError()) {
                    circleFoto.setFill(new ImagePattern(imgRemota));
                    return;
                }
            }
            Image imgPlaceholder = new Image(getClass().getResourceAsStream("/br/com/washii/assets/images/perfil/profile-placeholder.png"));
            circleFoto.setFill(new ImagePattern(imgPlaceholder));
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
        }
    }

    private boolean salvarAlteracoes() {
        try {
            validarCampos();

            Negocio negocio = (Negocio) buscarUsuarioLogado();

            negocio.setNome(txtNomeNegocio.getText());
            negocio.setEmail(txtEmail.getText());
            negocio.setCnpj(txtCnpj.getText());
            negocio.setRazaoSocial(txtRazaoSocial.getText());
            negocio.setCapacidadeAtendimentoSimultaneo(Integer.parseInt(txtCapacidade.getText()));


            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            negocio.setInicioExpediente(LocalTime.parse(txtHoraInicio.getText(), formatter));
            negocio.setFimExpediente(LocalTime.parse(txtHoraFim.getText(), formatter));
            negocio.setDuracaoMediaServico(LocalTime.parse(txtDuracaoServico.getText(), formatter));

            
            // Atualiza o Endereço, se houver objeto de endereço
            if (negocio.getEndereco() != null) {
                negocio.getEndereco().setCep(txtCep.getText());
                negocio.getEndereco().setRua(txtRua.getText());
                negocio.getEndereco().setNumero(txtNumero.getText());
                negocio.getEndereco().setBairro(txtBairro.getText());
                negocio.getEndereco().setCidade(txtCidade.getText());
                negocio.getEndereco().setEstado(txtEstado.getText());
            }

            // [CORREÇÃO OBJECT STORAGE]: Envia a foto selecionada para o Supabase Storage
            // e atualiza a URL no objeto Negocio antes de persistir no banco
            if (arquivoFotoSelecionada != null) {
                String idUsuario = String.valueOf(negocio.getId());
                String urlFoto = SupabaseStorageService.fazerUpload(arquivoFotoSelecionada.toPath(), idUsuario);
                negocio.setFotoPerfil(urlFoto);
                arquivoFotoSelecionada = null;
            }

            usuarioService.atualizarUsuario(negocio);

            this.usuarioLogado = negocio;
            // [CORREÇÃO OBJECT STORAGE]: Mantém o usuário da Sessão sincronizado
            Sessao.getInstance().iniciarSessao(negocio);
            
            AvisoUtils.exibirAvisoSucesso(containerAviso, "Usuario atualizado com sucesso!");
            return true;

        } catch (IllegalArgumentException e) {
            AvisoUtils.exibirAvisoAlerta(containerAviso, e.getMessage());
          
        } catch (NegocioException e) {
            AvisoUtils.exibirAvisoErro(containerAviso, e.getMessage());
            
        } catch (Exception e) {
            AvisoUtils.exibirAvisoErro(containerAviso, "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            limparAvisos(10);
        }

        return false;
    }
        
    private void validarCampos() throws IllegalArgumentException {
        if (txtNomeNegocio.getText().isBlank()) throw new IllegalArgumentException("O nome do negócio é obrigatório.");

        if (!txtCnpj.getText().isBlank()) {
            String apenasNumerosCnpj = txtCnpj.getText().replaceAll("[.\\-/]", "");

            if (!apenasNumerosCnpj.matches("\\d+")) { 
                throw new IllegalArgumentException("O CNPJ deve conter apenas números (letras não são permitidas).");
            }
        
            if (apenasNumerosCnpj.length() != 14) {
                throw new IllegalArgumentException("O CNPJ deve ter exatamente 14 números.");
            }
        }
        
        
        if (txtEmail.getText().isBlank() || !txtEmail.getText().contains("@")) {
            throw new IllegalArgumentException("E-mail inválido ou vazio.");
        }

        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            LocalTime horaInicio = LocalTime.parse(txtHoraInicio.getText(), formatter);
            LocalTime horaFim = LocalTime.parse(txtHoraFim.getText(), formatter);

            // Valida se o fim do expediente é uma hora além do inicio
            if (!horaFim.isAfter(horaInicio)) {
                throw new IllegalArgumentException("O horário de fechamento deve ser após o horário de abertura.");
            }

            LocalTime.parse(txtDuracaoServico.getText(), formatter);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horário inválido! Use o formato 00:00 (ex: 08:30)");
        }

        String capText = txtCapacidade.getText();
        if (capText == null || capText.isBlank()) {
            throw new IllegalArgumentException("A capacidade de atendimento deve ser informada.");
        }
        
        try {
            Integer n = Integer.parseInt(txtCapacidade.getText());

            if (n <= 0) {
                throw new IllegalArgumentException("A capacidade deve ser um número maior que 0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("A capacidade deve ser um número inteiro.");
        }
    }

    private void limparAvisos(int tempo){
        AvisoUtils.limparCampoAviso(containerAviso, tempo);
    }

    private Usuario buscarUsuarioLogado(){
        if (usuarioLogado == null){
            long id = Sessao.getInstance().getUsuarioLogado().getId();

            this.usuarioLogado = usuarioService.buscarUsuarioPorId(id);
        }

        return usuarioLogado;
    }
}
