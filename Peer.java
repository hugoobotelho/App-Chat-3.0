import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Peer {

  private static final Map<String, Usuario> usuarios = new HashMap<>(); // Gerencia usuários
  private Set<String> peersConhecidos;
  private static DescobrirPeers descobrirPeers;
  private static Peer peer;
  private Principal app;
  private static GrupoManager grupoManager; // Gerencia grupos
  // private Set<AtualizarPeers> peerTCP = new HashSet<>();
  // private ArrayList<String> menssagensLog = new ArrayList<>();

  // public static void main(String[] args) {
  public Peer(Principal app) {

    this.app = app;

    System.out.println(app.getPeersConhecidos());

    peersConhecidos = app.getPeersConhecidos();

    // peer = new Peer(app);

    // peersConhecidos = new HashSet<>();

    descobrirPeers = new DescobrirPeers(app);
    descobrirPeers.iniciarDescobrimento();

    grupoManager = new GrupoManager(this, app);

    // Inicia o peer UDP em uma thread separada
    Thread peerUDPThread = new Thread(() -> {
      PeerUDP peerUDP;
      try {
        peerUDP = new PeerUDP(grupoManager, usuarios, app);
        peerUDP.iniciar();
      } catch (SocketException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });

    // Inicia o Peer TCP em uma thread separada
    Thread peerTCPThread = new Thread(() -> {
      PeerTCP peerTCP = new PeerTCP(app, grupoManager, usuarios, peer);
      peerTCP.iniciar();
    });

    // Inicia as threads
    peerUDPThread.start();
    peerTCPThread.start();

    System.out.println("Servidores UDP e TCP iniciados...");
  }

  /*
   * ***************************************************************
   * Metodo: getGrupoManager
   * Funcao: Retorna a instância do gerenciador de grupos (GrupoManager).
   * Parametros: nenhum
   * Retorno: GrupoManager - instância do gerenciador de grupos
   */
  public static GrupoManager getGrupoManager() {
    return grupoManager;
  }

  /*
   * ***************************************************************
   * Metodo: getUsuarios
   * Funcao: Retorna o mapa de usuários cadastrados na aplicação.
   * Parametros: nenhum
   * Retorno: Map<String, Usuario> - mapa contendo os usuários registrados
   */
  public static Map<String, Usuario> getUsuarios() {
    return usuarios;
  }

  /*
   * ***************************************************************
   * Metodo: getServidoresConhecidos
   * Funcao: Retorna o conjunto de IPs dos Peers conhecidos detectados na
   * rede.
   * Parametros: nenhum
   * Retorno: Set<String> - conjunto de IPs de Peers conhecidos
   */
  public Set<String> getPeersConhecidos() {
    return peersConhecidos;
  }

  /*
   * ***************************************************************
   * Metodo: setServidoresConhecidos
   * Funcao: Adiciona um novo Peer à lista de Peers conhecidos, evitando
   * duplicatas,
   * e inicializa uma thread AtualizarServidores para ele.
   * Parametros: String novoServidor - IP do novo Peer a ser adicionado
   * Retorno: void
   */
  public void setPeersConhecidos(String novoPeer) {
    if (!peersConhecidos.contains(novoPeer)) {
      // AtualizarPeers atualizarPeers = new AtualizarPeers(novoPeer, 6789, peer);
      // peerTCP.add(atualizarPeers);
      peersConhecidos.add(novoPeer);

      app.setPeersConhecidos(novoPeer);

      System.out.println("Peer adicionado: " + novoPeer);

    }

    for (String nomeGrupo : app.getGrupos()) { // envia join de todos os grupos que esta para todos os peers (rewolve o
                                               // problema de atualizar um peer caso ele caia, mas aumenta
                                               // significativamente o numero de mensagens trocadas), tentar melhor
                                               // depois
      GruposPeer gruposPeer = new GruposPeer(novoPeer, 6789, app);
      gruposPeer.enviarAPDUJoin(app.getNomeUsuario(), nomeGrupo);
    }

  }

}
