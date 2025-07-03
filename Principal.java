
/* ***************************************************************
* Autor: Hugo Botelho Santana
* Matricula: 202210485
* Inicio: 27/06/2025
* Ultima alteracao: 03/07/2025
* Nome: Programa de Chat P2P
* Funcao: Aplicativo de chat para troca de mensagens no modelo Peer to Peer
*************************************************************** */
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Principal extends Application {
  private StackPane root = new StackPane(); // Usando StackPane para facilitar centralização
  private String nomeUsuario; // Nome do usuário conectado
  private GruposPeer gruposPeer; // Instância de cada Peer para enviar os JOINS e LEAVES TCP
  private static Set<String> peersConhecidos;
  private Set<GruposPeer> peersTCP;
  private Principal app;
  // private DescobrirServidores descobrirServidores;
  private Peer peer;

  private final static List<String> grupos = new ArrayList<>(); // Lista dinâmica de grupos
  private final static Map<String, HistoricoMensagens> historicosMensagens = new HashMap<>();

  private static TelaMeusGrupos telaMeusGrupos;

  @Override
  public void start(Stage primaryStage) {

    // app = new Principal();

    peersConhecidos = new HashSet<>();
    peersTCP = new HashSet<>();

    peer = new Peer(this);

    Scene scene = new Scene(root, 390, 644);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Aplicativo de Instant Messaging");
    primaryStage.setResizable(false);
    primaryStage.show();

    // Configura o evento de encerramento do aplicativo
    primaryStage.setOnCloseRequest(t -> {
      // if (enviarMensagemGrupo != null) {
      // enviarMensagemGrupo.fechar();
      // }
      Platform.exit();
      System.exit(0);
    });

    telaMeusGrupos = new TelaMeusGrupos(this);

    // Mostra a tela inicial ao iniciar o programa
    TelaInicio telaInicio = new TelaInicio(this);
    root.getChildren().setAll(telaInicio.getLayout());

    // Centralizando o layout da TelaInicio
    root.setAlignment(telaInicio.getLayout(), javafx.geometry.Pos.CENTER);

  }

  /*
   * ***************************************************************
   * Metodo: processarMensagemRecebida
   * Funcao: Processa uma mensagem recebida via UDP e atualiza a
   * interface gráfica.
   * Parametros: String mensagemRecebida - conteúdo da mensagem.
   * Retorno: void
   */
  public void processarMensagemRecebida(String mensagemRecebida) {
    try {
      // Separar os campos da mensagem
      String[] partes = mensagemRecebida.split("\\|");
      if (partes.length < 4 || !("SEND".equals(partes[0]) || "SENDUNIQUE".equals(partes[0]))) {
        System.err.println("Formato de mensagem inválido: " + mensagemRecebida);
        return;
      }
      String apdu = partes[0];
      String grupo = partes[1];
      String usuario = partes[2];
      String mensagem = partes[3];
      String timeStamp = partes[4];

      if (!usuario.equals(nomeUsuario)) {
        // Adicionar a mensagem ao histórico
        HistoricoMensagens historico = historicosMensagens.get(grupo);
        if (historico == null) {
          System.err.println("Grupo não encontrado: " + grupo);
          return;
        }
        String horaAtual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        Mensagem novaMensagem;
        if (apdu.equals("SENDUNIQUE")) {
          novaMensagem = new Mensagem(this, usuario, mensagem, horaAtual, "check", timeStamp,
              grupo, true, false);

        } else {
          novaMensagem = new Mensagem(this, usuario, mensagem, horaAtual, "de outro usuario", timeStamp,
              grupo, false, false);
        }
        historico.adicionarMensagem(novaMensagem);
        // telaMeusGrupos.getTelasChat().get(grupo).notificarVistoDasMensagens();

        // Atualizar a interface gráfica na thread da aplicação
        Platform.runLater(() -> {
          TelaMeusGrupos telaGrupos = getTelaMeusGrupos();
          Map<String, TelaChat> telasChat = telaGrupos.getTelasChat(); // Supondo que este método foi adicionado

          TelaChat telaChat = telasChat.get(grupo);
          if (telaChat != null) {
            telaChat.renderizarMensagens(); // Re-renderiza as mensagens
          }
        });
      }

    } catch (Exception e) {
      System.err.println("Erro ao processar mensagem recebida: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: getClienteTCP
   * Funcao: Retorna a instância do gruposPeer.
   * Parametros: sem parâmetros.
   * Retorno: ClienteTCP
   */
  public GruposPeer getGruposPeer() {
    return gruposPeer;
  }

  /*
   * ***************************************************************
   * Metodo: setNomeUsuario
   * Funcao: Define o nome do usuário atual.
   * Parametros: String nomeUsuario.
   * Retorno: void
   */
  public void setNomeUsuario(String nomeUsuario) {
    this.nomeUsuario = nomeUsuario;
  }

  /*
   * ***************************************************************
   * Metodo: getNomeUsuario
   * Funcao: Retorna o nome do usuário atual.
   * Parametros: sem parâmetros.
   * Retorno: String
   */
  public String getNomeUsuario() {
    return nomeUsuario;
  }

  /*
   * ***************************************************************
   * Metodo: getRoot
   * Funcao: Retorna o layout principal da aplicação.
   * Parametros: sem parâmetros.
   * Retorno: StackPane
   */
  public StackPane getRoot() {
    return root;
  }

  /*
   * ***************************************************************
   * Metodo: getGrupos
   * Funcao: Retorna a lista de grupos ativos.
   * Parametros: sem parâmetros.
   * Retorno: List<String>
   */
  public List<String> getGrupos() {
    return grupos;
  }

  /*
   * ***************************************************************
   * Metodo: getTelaMeusGrupos
   * Funcao: Retorna a tela de gerenciamento de grupos.
   * Parametros: sem parâmetros.
   * Retorno: TelaMeusGrupos
   */
  public TelaMeusGrupos getTelaMeusGrupos() {
    return telaMeusGrupos;
  }

  /*
   * ***************************************************************
   * Metodo: getHistoricosMensagens
   * Funcao: Retorna o mapa com o histórico de mensagens de cada grupo.
   * Parametros: sem parâmetros.
   * Retorno: Map<String, HistoricoMensagens>
   */
  public Map<String, HistoricoMensagens> getHistoricosMensagens() {
    return historicosMensagens;
  }

  /*
   * ***************************************************************
   * Metodo: getServidoresConhecidos
   * Funcao: Retorna o conjunto de IPs dos peers conhecidos.
   * Parametros: sem parâmetros.
   * Retorno: Set<String>
   */
  public Set<String> getPeersConhecidos() {
    return peersConhecidos;
  }

  public void removePeerConhecido(String ipPeer) {
    peersConhecidos.remove(ipPeer);

    List<GruposPeer> copia = new ArrayList<>(peersTCP);
    for (GruposPeer peer : copia) {
      if (peer.getHost().equals(ipPeer)) {
        peersTCP.remove(peer);
      }
    }

    // peersTCP.removeIf(p -> p.getHost().equals(ipPeer));
    // for (GruposPeer peer : peersTCP) {
    // if (peer.getHost().equals(ipPeer)) {
    // peersTCP.remove(peer);
    // }
    // }
  }

  public Set<GruposPeer> getGrupoPeer() {
    return peersTCP;
  }

  /*
   * ***************************************************************
   * Metodo: setServidoresConhecidos
   * Funcao: Adiciona um novo IP ao conjunto de peers conhecidos e inicializa
   * conexões se necessário.
   * Parametros: String novoServidor - IP do Peer a ser adicionado.
   * Retorno: void
   */
  public void setPeersConhecidos(String novoPeer) {
    if (!peersConhecidos.contains(novoPeer)) {
      GruposPeer peer = new GruposPeer(novoPeer, 6789, this);
      peersTCP.add(peer);
      System.out.println("Meus grupos: " + grupos);
      for (String nomeGrupo : grupos) { // envia join de todos os grupos que esta para todos os peers (rewolve o
                                        // problema de atualizar um peer caso ele caia, mas aumenta significativamente o
                                        // numero de mensagens trocadas), tentar melhor depois
        System.out
            .println("Enviando JOIN para grupo: " + nomeGrupo + " no peer: " + novoPeer + " usuario: " + nomeUsuario);
        // GruposPeer gruposPeer = new GruposPeer(novoPeer, 6789, app);
        peer.enviarAPDUJoin(nomeUsuario, nomeGrupo);
      }
    }
    peersConhecidos.add(novoPeer);
    // if (!peersConhecidos.contains(novoPeer)) {
    // // AtualizarPeers atualizarPeers = new AtualizarPeers(novoPeer, 6789, peer);
    // // peerTCP.add(atualizarPeers);
    // peersConhecidos.add(novoPeer);

    // // app.setPeersConhecidos(novoPeer);

    // System.out.println("Peer adicionado: " + novoPeer);

    // }

  }

  public Peer getPeer() {
    return peer;
  }

  /**
   * ***************************************************************
   * Metodo: main.
   * Funcao: metodo para iniciar a aplicacao.
   * Parametros: padrao java.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  public static void main(String[] args) {
    launch(args);
  }
}
