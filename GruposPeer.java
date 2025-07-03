
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GruposPeer {

  private String host;
  private int porta;
  private int indiceAtual = 0;
  private Principal app;
  private Socket socket;
  private ObjectOutputStream saida;
  private ObjectInputStream entrada;

  // Construtor
  public GruposPeer(String host, int porta, Principal app) {
    this.host = host;
    this.porta = porta;
    this.app = app;
    try {
      socket = new Socket(host, porta);
      saida = new ObjectOutputStream(socket.getOutputStream());
      // entrada = new ObjectInputStream(socket.getInputStream());

    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String getHost() {
    return host;
  }

  /*
   * ***************************************************************
   * Metodo: conectarESalvarAPDU
   * Funcao: Conecta ao Peer TCP, envia uma APDU com dados formatados e
   * aguarda resposta.
   * Parametros: String tipoMensagem - tipo da requisição (JOIN ou LEAVE)
   * String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo
   * Retorno: void
   */
  private void conectarESalvarAPDU(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
    try {
      System.out.println("Conectado ao Peer " + host + ":" + porta);

      // Envia a mensagem
      // ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
      String mensagem = tipoMensagem + "|" + nomeUsuario + "|" + nomeGrupo;
      saida.writeObject(mensagem);
      saida.flush();
      System.out.println("Mensagem enviada: " + mensagem);


    } catch (IOException e) {
      System.out.println("O peer que teria que receber o LEAVE nao esta rodando.");
      if (!tipoMensagem.equals("LEAVE")) { 
        System.err.println("Erro ao conectar ou enviar mensagem: " + e.getMessage());
        try {
          Thread.sleep(1000); // espera 1 segundo para tentar novamente
        } catch (InterruptedException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo); // tenta novamente
        // escolherNovoServidor(tipoMensagem, nomeUsuario, nomeGrupo); // Se falhar,
        // tenta outro
      }
    }

  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDUJoin
   * Funcao: Inicia uma thread para envio da APDU JOIN ao Peer.
   * Parametros: String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo a ser ingressado
   * Retorno: void
   */
  public void enviarAPDUJoin(String nomeUsuario, String nomeGrupo) {
    Thread threadJoin = new Thread(() -> conectarESalvarAPDU("JOIN", nomeUsuario, nomeGrupo));
    threadJoin.start(); // Inicia a thread de envio JOIN
  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDULeave
   * Funcao: Inicia uma thread para envio da APDU LEAVE ao Peer.
   * Parametros: String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo a ser deixado
   * Retorno: void
   */
  public void enviarAPDULeave(String nomeUsuario, String nomeGrupo) {
    Thread threadLeave = new Thread(() -> conectarESalvarAPDU("LEAVE", nomeUsuario, nomeGrupo));
    threadLeave.start(); // Inicia a thread de envio LEAVE
  }

}
