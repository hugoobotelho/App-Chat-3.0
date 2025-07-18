import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;

public class TelaChat {
  private VBox layout = new VBox(10); // Layout principal da tela de chat
  private HistoricoMensagens historicoMensagens;
  private Principal app; // Instância principal do aplicativo
  private VBox listaMensagens; // Container para exibição das mensagens
  private int mensagensJaVistas = 0;
  private boolean threadRodando = true;
  public boolean isOpen = false;
  private boolean iconeFlag = true;
  private ScrollPane scrollMensagens;
  private Label nomeIntegrantes;
  private String nomeGurpo;

  public TelaChat(Principal app, String nomeGrupo, HistoricoMensagens historicoMensagens) {
    this.app = app;
    this.historicoMensagens = historicoMensagens;
    this.nomeGurpo = nomeGrupo;

    layout.setStyle("-fx-alignment: top-center;");

    // Criando o Header
    HBox header = new HBox();
    header.setStyle("-fx-background-color: #333333; -fx-padding: 24px; -fx-alignment: center;");

    // Crie o ImageView como antes
    ImageView iconeVisualizacaoUnica = new ImageView(new Image("/img/visualizacaoUnicaCinza.png"));
    iconeVisualizacaoUnica.setFitHeight(24);
    iconeVisualizacaoUnica.setFitWidth(24);

    // Coloque dentro de um Button
    Button botaoVisualizacaoUnica = new Button();
    botaoVisualizacaoUnica.setGraphic(iconeVisualizacaoUnica);
    botaoVisualizacaoUnica.setStyle(
        "-fx-background-color: transparent; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 10px;" // Aumenta a área clicável sem fundo visível
    );

    // icone de Voltar
    ImageView voltarIcon = new ImageView(new Image("/img/voltarIcon.png"));
    voltarIcon.setFitHeight(40);
    voltarIcon.setFitWidth(40);
    Button botaoVoltar = new Button();
    botaoVoltar.setGraphic(voltarIcon);
    botaoVoltar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
    botaoVoltar.setOnAction(e -> {
      isOpen = false;
      iconeFlag = true;
      iconeVisualizacaoUnica.setImage(new Image("/img/visualizacaoUnicaCinza.png"));
      botaoVisualizacaoUnica.setGraphic(iconeVisualizacaoUnica);
      historicoMensagens.getMensagens().removeIf(m -> m.getVisualizacaoUnica()); // remove as mensagens de
                                                                                 // visualizacao unica
      renderizarMensagens();

      TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
      app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
    });

    // Nome do Grupo
    Label nomeGrupoTexto = new Label(nomeGrupo);
    nomeGrupoTexto.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E5AF18;");
    nomeGrupoTexto.setMaxWidth(400);
    nomeGrupoTexto.setWrapText(true);

    nomeIntegrantes = new Label();
    nomeIntegrantes.setStyle("-fx-text-fill: #B4B4B4; -fx-font-size: 16px;");
    nomeIntegrantes.setWrapText(true);
    nomeIntegrantes.setMaxWidth(400);

    VBox dadosGrupo = new VBox(5);
    dadosGrupo.getChildren().addAll(nomeGrupoTexto, nomeIntegrantes);
    dadosGrupo.setAlignment(Pos.CENTER_LEFT);

    dadosGrupo.setMaxWidth(400);

    HBox.setHgrow(dadosGrupo, Priority.ALWAYS);

    // icone de Sair do Grupo
    ImageView sairIcon = new ImageView(new Image("/img/sairIcon.png"));
    sairIcon.setFitHeight(40);
    sairIcon.setFitWidth(40);
    Button botaoSair = new Button();
    botaoSair.setGraphic(sairIcon);
    botaoSair.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
    botaoSair.setOnAction(e -> {
      enviarAPDULeave(nomeGrupo);
      TelaMeusGrupos.removerGrupo(nomeGrupo);
      TelaMeusGrupos telaMeusGrupos = new TelaMeusGrupos(app);
      app.getRoot().getChildren().setAll(telaMeusGrupos.getLayout());
    });

    header.getChildren().addAll(botaoVoltar, dadosGrupo, botaoSair);

    // Lista de Mensagens
    listaMensagens = new VBox(10);
    listaMensagens.setStyle("-fx-padding: 10px; -fx-background-color: transparent;");

    listaMensagens.heightProperty().addListener((obs, oldVal, newVal) -> {
      scrollMensagens.setVvalue(1.0); // rola para o final sempre que a altura muda
    });

    renderizarMensagens();

    scrollMensagens = new ScrollPane(listaMensagens);
    scrollMensagens.setStyle("-fx-background: #F5F5F5; -fx-border-color: #F5F5F5;");
    scrollMensagens.setFitToWidth(true);
    scrollMensagens.setPrefHeight(500);
    scrollMensagens.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollMensagens.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    // Campo para Envio de Mensagens
    HBox enviarMensagemLayout = new HBox(10);
    enviarMensagemLayout.setStyle("-fx-background-color: #E1E0E0; -fx-background-radius: 10px;");
    enviarMensagemLayout.setPadding(new Insets(10, 20, 10, 20));
    VBox.setMargin(enviarMensagemLayout, new Insets(10, 10, 10, 10));
    enviarMensagemLayout.setAlignment(Pos.CENTER_LEFT);

    // botaoVisualizacaoUnica.setMinSize(24, 24); // area de clique maior

    // Alternância de imagem ao clicar
    botaoVisualizacaoUnica.setOnAction(event -> {
      iconeFlag = !iconeFlag;
      String imagemPath = iconeFlag
          ? "/img/visualizacaoUnicaCinza.png"
          : "/img/visualizacaoUnicaPreto.png";
      iconeVisualizacaoUnica.setImage(new Image(imagemPath, false));
    });

    TextField campoMensagem = criarCampoComPlaceholder("Mensagem");
    campoMensagem.setMaxWidth(Double.MAX_VALUE);
    campoMensagem.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px; -fx-padding: 8px;");
    HBox.setHgrow(campoMensagem, Priority.ALWAYS);

    campoMensagem.setOnKeyPressed(event -> {
      if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
        String mensagem = campoMensagem.getText();
        if (!mensagem.isEmpty()) {
          try {
            String timeStamp = Long.toString(System.currentTimeMillis());

            String horaAtual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            // Adiciona mensagem ao histórico

            // notificarVistoDasMensagens();
            String mensagemFormatada;
            if (iconeFlag) {
              Mensagem novaMensagem = new Mensagem(app, app.getNomeUsuario(), mensagem, horaAtual, "check", timeStamp,
                  nomeGrupo, false, false);
              historicoMensagens.adicionarMensagem(novaMensagem);
              mensagemFormatada = "SEND|" + nomeGrupo + "|" + app.getNomeUsuario() + "|" + mensagem + "|"
                  + timeStamp;
            } else {
              Mensagem novaMensagem = new Mensagem(app, app.getNomeUsuario(), mensagem, horaAtual, "check", timeStamp,
                  nomeGrupo, true, false);
              historicoMensagens.adicionarMensagem(novaMensagem);
              mensagemFormatada = "SENDUNIQUE|" + nomeGrupo + "|" + app.getNomeUsuario() + "|" + mensagem + "|"
                  + timeStamp;
            }

            // app.getMensagensGruposPeer().enviarMensagem(mensagemFormatada);
            for (Usuario membro : app.getPeer().getGrupoManager().obterMembros(nomeGrupo)) { // enviar para cada peer do
                                                                                             // grupo, a mensagem.
              // if (!membro.getNome().equals(app.getNomeUsuario())) {
              EnviarMensagemGrupo enviarMensagemGrupo = new EnviarMensagemGrupo(app,
                  membro.getEndereco().getHostAddress(),
                  1234);
              enviarMensagemGrupo.enviarMensagem(mensagemFormatada);
              // }
            }

            // Atualiza as mensagens exibidas
            renderizarMensagens();

            // Limpa o campo de mensagem
            campoMensagem.clear();
          } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
          }
        }
      }
    });

    enviarMensagemLayout.getChildren().addAll(botaoVisualizacaoUnica, campoMensagem);
    layout.getChildren().addAll(header, scrollMensagens, enviarMensagemLayout);

  }

  public void iniciarThreadVisualizacoes() {
    new Thread(() -> {
      while (threadRodando) {
        if (isOpen) {
          long totalNaoUnicas = historicoMensagens.getMensagens().stream()
              .filter(m -> !m.isRemove())
              // .filter(m -> !m.getVisualizacaoUnica())
              .count();

          if (totalNaoUnicas > mensagensJaVistas) {
            mensagensJaVistas = (int) totalNaoUnicas;
            notificarVistoDasMensagens();
          }
        }

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  /*
   * ***************************************************************
   * Metodo: renderizarMensagens
   * Funcao: Atualiza a exibição das mensagens no layout, renderizando as
   * mensagens
   * armazenadas no histórico.
   * Parametros: nenhum
   * Retorno: void
   */
  public void renderizarMensagens() {

    Platform.runLater(() -> {
      StringBuilder dadosMembros = new StringBuilder();
      for (Usuario membro : app.getPeer().getGrupoManager().obterMembros(nomeGurpo)) {
        if (dadosMembros.length() > 0) {
          dadosMembros.append(", ");
        }
        dadosMembros.append(membro.getNome());
      }
      nomeIntegrantes.setText(dadosMembros.toString());

      listaMensagens.getChildren().clear();
      List<Mensagem> mensagensCopia = new ArrayList<>(historicoMensagens.getMensagens());
      for (Mensagem mensagem : mensagensCopia) {
        listaMensagens.getChildren().add(criarComponenteMensagem(mensagem));
      }

      // for (Mensagem mensagem : historicoMensagens.getMensagens()) {
      //   listaMensagens.getChildren().add(criarComponenteMensagem(mensagem));
      // }
      // Garante que o scroll só aconteça depois do layout estar renderizado
      // Platform.runLater(() -> {
      // scrollMensagens.setVvalue(1.0); // Scrolla para o fim
      // });
    });
  }

  public void notificarVistoDasMensagens() {
    for (Mensagem mensagem : historicoMensagens.getMensagens()) {
      // if (!mensagem.getStatus().equals("unique")) {
      for (Map.Entry<String, Set<Usuario>> entry : app.getPeer().getGrupoManager().getGrupos().entrySet()) {
        if (entry.getKey().equals(mensagem.getNomeGrupoMensagem())) {
          for (Usuario usuario : entry.getValue()) {
            if (usuario.getNome().equals(mensagem.getRemetente())) {
              String ipRemetente = usuario.getEndereco().getHostAddress();
              try {
                EnviarMensagemGrupo enviarMensagemGrupo = new EnviarMensagemGrupo(app, ipRemetente, 2345);
                String respostaVisto = "VISTO|" + mensagem.getNomeGrupoMensagem() + "|" + app.getNomeUsuario() + "|"
                    + mensagem.getConteudo() + "|"
                    + mensagem.getTimeStampMensagem();
                enviarMensagemGrupo.enviarMensagem(respostaVisto);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      // }
    }
  }

  /*
   * ***************************************************************
   * Metodo: criarComponenteMensagem
   * Funcao: Cria um componente visual para cada mensagem, incluindo remetente,
   * conteúdo e horário.
   * Parametros: Mensagem mensagem - objeto contendo os dados da mensagem
   * Retorno: VBox - layout que contém a estrutura da mensagem
   */
  private VBox criarComponenteMensagem(Mensagem mensagem) {
    if (!mensagem.isRemove()) {
      VBox componenteMensagem = new VBox(5);

      Label remetenteLabel = new Label(mensagem.getRemetente());
      remetenteLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: #B4B4B4;");

      Label conteudoMensagem = new Label(mensagem.getConteudo());
      // conteudoMensagem.setStyle(
      // "-fx-background-color: #333333; " +
      // "-fx-text-fill: #E5AF18; " +
      // "-fx-font-size: 16px; " +
      // "-fx-padding: 10px; " +
      // "-fx-background-radius: 10px; " +
      // "-fx-max-width: 250px; " +
      // "-fx-wrap-text: true;");

      ImageView iconImageView = new ImageView(mensagem.getStatusImage());
      HBox conteudoCheckHBox = new HBox(5);

      Label horarioLabel = new Label(mensagem.getHora());
      horarioLabel.setStyle("-fx-font-weight: 500; -fx-text-fill: #B4B4B4;");

      if (mensagem.getRemetente().equals(app.getNomeUsuario())) {
        componenteMensagem.setStyle("-fx-alignment: top-right;");
        conteudoCheckHBox.setStyle(
            "-fx-background-color: #E5AF18; " +
                "-fx-text-fill: #333333; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-max-width: 250px; " +
                "-fx-wrap-text: true;");
        conteudoMensagem.setStyle(
            "-fx-max-width: 240px;" +
                "-fx-wrap-text: true;");

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        conteudoCheckHBox.getChildren().addAll(conteudoMensagem, region, iconImageView);

        conteudoCheckHBox.setAlignment(Pos.CENTER);

        componenteMensagem.getChildren().addAll(remetenteLabel, conteudoCheckHBox, horarioLabel);
      } else {
        conteudoMensagem.setStyle(
            "-fx-background-color: #333333; " +
                "-fx-text-fill: #E5AF18; " +
                "-fx-font-size: 16px; " +
                "-fx-padding: 10px; " +
                "-fx-background-radius: 10px; " +
                "-fx-max-width: 250px; " +
                "-fx-wrap-text: true;");
        componenteMensagem.setStyle("-fx-alignment: top-left;");
        componenteMensagem.getChildren().addAll(remetenteLabel, conteudoMensagem, horarioLabel);
      }
      return componenteMensagem;
    } else {
      VBox componenteMensagem = new VBox(5);
      Label mensagemRemocao = new Label(mensagem.getNomeUsuarioASerRemovido() + " saiu do grupo");
      mensagemRemocao.setStyle("-fx-text-fill: #B4B4B4; -fx-font-size: 16px; -fx-font-style: italic;");
      mensagemRemocao.setAlignment(Pos.CENTER);
      componenteMensagem.getChildren().addAll(mensagemRemocao);
      return componenteMensagem;
    }

  }

  public void mostrarMensagemDeRemovido(String nomeUsuario) {
    Mensagem mensagemDeRemocao = new Mensagem(nomeUsuario, true);
    historicoMensagens.adicionarMensagem(mensagemDeRemocao);
    renderizarMensagens();
  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDULeave
   * Funcao: Envia uma APDU de "LEAVE" para o Peer para que o usuário saia do
   * grupo.
   * Parametros: String nomeGrupo - nome do grupo a ser deixado
   * Retorno: void
   */
  private void enviarAPDULeave(String nomeGrupo) {
    try {
      // app.getGruposPeer().enviarAPDULeave(app.getNomeUsuario(), nomeGrupo);
      for (GruposPeer peer : app.getGrupoPeer()) { // envia o leave para todos os peers
        // GruposPeer gruposPeer = new GruposPeer(grupoPeerIP, 6789, app);
        peer.enviarAPDULeave(app.getNomeUsuario(), nomeGrupo);
      }
    } catch (Exception e) {
      System.err.println("Erro ao enviar APDU Leave: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: criarCampoComPlaceholder
   * Funcao: Cria um campo de texto (TextField) com um placeholder.
   * Parametros: String placeholder - texto a ser exibido como dica no campo
   * Retorno: TextField - o campo de texto configurado
   */
  private TextField criarCampoComPlaceholder(String placeholder) {
    TextField campo = new TextField();
    campo.setPromptText(placeholder);
    campo.setMaxWidth(300);
    campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");

    campo.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");
      } else {
        campo.setStyle("-fx-background-color: #E1E0E0; -fx-font-size: 16px");
      }
    });

    return campo;
  }

  /*
   * ***************************************************************
   * Metodo: getLayout
   * Funcao: Retorna o layout principal da tela de chat.
   * Parametros: nenhum
   * Retorno: VBox - o layout principal da tela de chat
   */
  public VBox getLayout() {
    return layout;
  }
}
