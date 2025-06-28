
/* ***************************************************************
* Autor: Hugo Botelho Santana
* Matricula: 202210485
* Inicio: 19/04/2025
* Ultima alteracao: 23/04/2025
* Nome: Programa de Chat/WhatZap com múltiplos servidores (conexões UDP e TCP)
* Funcao: Aplicativo de chat para troca de mensagens com o modelo n clientes e n servidores
*************************************************************** */
import java.io.IOException;
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
  private String ipServidor;
  private GruposPeer gruposPeer; // Instância do cliente TCP
  // private EnviarMensagemGrupo enviarMensagemGrupo; // Instância do cliente UDP
  private static Set<String> peersConhecidos;
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

    peer = new Peer(this);

    Scene scene = new Scene(root, 390, 644);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Aplicativo de Instant Messaging");
    primaryStage.setResizable(false);
    primaryStage.show();

    // Configura o evento de encerramento do aplicativo
    primaryStage.setOnCloseRequest(t -> {
      // if (enviarMensagemGrupo != null) {
      // enviarMensagemGrupo.fechar(); // Fecha o cliente UDP
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

    // descobrirServidores = new DescobrirServidores(this);
    // descobrirServidores.iniciarSincronizacao(); // descobre os servidores da rede
    // e armazena em servidores conhecidos

  }

  /*
   * ***************************************************************
   * Metodo: criarClientes
   * Funcao: Cria as conexões TCP e UDP para o cliente com o IP do
   * servidor.
   * Parametros: String nomeUsuario - nome do usuário conectado.
   * Retorno: void
   */
  // public void criarClientes(String nomeUsuario) {
  // // this.ipServidor = ipServidor;
  // this.nomeUsuario = nomeUsuario;

  // if (ipServidor != null) {
  // System.out.println("Cliente criado!");
  // // Criando e conectando o cliente TCP
  // gruposPeer = new GruposPeer(ipServidor, 6789, this);

  // // Criando e conectando o cliente UDP
  // criarClienteUDP(ipServidor, 6789);
  // }
  // }

  /*
   * ***************************************************************
   * Metodo: criarClienteUDP
   * Funcao: Inicializa o cliente UDP e atualiza o IP do servidor se
   * necessário.
   * Parametros: String ipServidor - IP do servidor, int porta - porta de
   * conexão.
   * Retorno: void
   */
  // public void iniciarEscutaDeMensagens(String ipServidor, int porta) {
  // try {
  // if (mensagensGrupoPeer != null) {
  // mensagensGrupoPeer.setIpServidor(ipServidor); // atualiza o ip do servidor
  // caso o usuario mude na tela de
  // // configuracoes
  // } else {
  // mensagensGrupoPeer = new MensagensGruposPeer(ipServidor, porta); //
  // Inicializa o cliente UDP
  // }
  // System.out.println("Cliente UDP criado e conectado ao servidor " + ipServidor
  // + ":" + porta);
  // iniciarThreadRecebimentoUDP(); // Inicia a thread para receber mensagens via
  // UDP
  // } catch (Exception e) {
  // System.err.println("Erro ao criar ClienteUDP: " + e.getMessage());
  // }
  // }

  /*
   * ***************************************************************
   * Metodo: iniciarThreadRecebimentoUDP
   * Funcao: Cria e inicia uma thread para escutar mensagens recebidas
   * via UDP.
   * Parametros: sem parâmetros.
   * Retorno: void
   */
  // private void iniciarThreadRecebimentoUDP() {
  // new Thread(() -> {
  // try {
  // while (true) {
  // String mensagemRecebida = mensagensGrupoPeer.receberMensagem(); // Aguarda
  // mensagens do servidor
  // System.out.println("Mensagem recebida via UDP: " + mensagemRecebida);

  // // Criar uma thread para processar e renderizar a mensagem recebida
  // new Thread(() -> processarMensagemRecebida(mensagemRecebida)).start();
  // }
  // } catch (Exception e) {
  // System.err.println("Erro ao receber mensagem UDP: " + e.getMessage());
  // }
  // }).start();
  // }

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
      if (partes.length < 4 || !"SEND".equals(partes[0])) {
        System.err.println("Formato de mensagem inválido: " + mensagemRecebida);
        return;
      }

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
        Mensagem novaMensagem = new Mensagem(this, usuario, mensagem, horaAtual, "de outro usuario", timeStamp, grupo);
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
   * Funcao: Retorna a instância do cliente TCP.
   * Parametros: sem parâmetros.
   * Retorno: ClienteTCP
   */
  public GruposPeer getGruposPeer() {
    return gruposPeer;
  }

  /*
   * ***************************************************************
   * Metodo: getClienteUDP
   * Funcao: Retorna a instância do cliente UDP.
   * Parametros: sem parâmetros.
   * Retorno: ClienteUDP
   */
  // public EnviarMensagemGrupo getMensagensGruposPeer() {
  // return enviarMensagemGrupo;
  // }
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
   * Metodo: setIpServidor
   * Funcao: Define o IP do servidor e atualiza a conexão UDP.
   * Parametros: String ip - IP do servidor.
   * Retorno: void
   */
  // public void setIpServidor(String ip) {
  // this.ipServidor = ip;
  // criarClienteUDP(ip, 6789); // so atualiza o ip do servidor do cliente UDP
  // pois no tcp ja foi atualizado
  // // quando caiu no catch e elegeu um novo servidor
  // }
  /*
   * ***************************************************************
   * Metodo: getIpServidor
   * Funcao: Retorna o IP atual do servidor.
   * Parametros: sem parâmetros.
   * Retorno: String
   */
  public String getIpServidor() {
    return ipServidor;
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
   * Funcao: Retorna o conjunto de IPs dos servidores conhecidos.
   * Parametros: sem parâmetros.
   * Retorno: Set<String>
   */
  public Set<String> getPeersConhecidos() {
    return peersConhecidos;
  }

  /*
   * ***************************************************************
   * Metodo: setServidoresConhecidos
   * Funcao: Adiciona um novo IP ao conjunto de servidores conhecidos e inicializa
   * conexões se necessário.
   * Parametros: String novoServidor - IP do servidor a ser adicionado.
   * Retorno: void
   */
  public void setPeersConhecidos(String novoPeer) {
    peersConhecidos.add(novoPeer);
    // if (!peersConhecidos.contains(novoPeer)) {
    // // AtualizarPeers atualizarPeers = new AtualizarPeers(novoPeer, 6789, peer);
    // // peerTCP.add(atualizarPeers);
    // peersConhecidos.add(novoPeer);

    // // app.setPeersConhecidos(novoPeer);

    // System.out.println("Peer adicionado: " + novoPeer);

    // }
    System.out.println("Meus grupos: " + grupos);
    for (String nomeGrupo : grupos) { // envia join de todos os grupos que esta para todos os peers (rewolve o
                                      // problema de atualizar um peer caso ele caia, mas aumenta significativamente o
                                      // numero de mensagens trocadas), tentar melhor depois
      System.out
          .println("Enviando JOIN para grupo: " + nomeGrupo + " no peer: " + novoPeer + " usuario: " + nomeUsuario);
      GruposPeer gruposPeer = new GruposPeer(novoPeer, 6789, app);
      gruposPeer.enviarAPDUJoin(nomeUsuario, nomeGrupo);
    }
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
