
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

  // Construtor
  public GruposPeer(String host, int porta, Principal app) {
    this.host = host;
    this.porta = porta;
    this.app = app;
    try {
      socket = new Socket(host, porta);
    } catch (UnknownHostException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * ***************************************************************
   * Metodo: conectarESalvarAPDU
   * Funcao: Conecta ao servidor TCP, envia uma APDU com dados formatados e
   * aguarda resposta.
   * Parametros: String tipoMensagem - tipo da requisição (JOIN ou LEAVE)
   * String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo
   * Retorno: void
   */
  private void conectarESalvarAPDU(String tipoMensagem, String nomeUsuario, String nomeGrupo) {
    try {
      System.out.println("Conectado ao servidor " + host + ":" + porta);
      // app.setIpServidor(host); // atualiza o servidor ativo

      // Envia a mensagem
      ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
      String mensagem = tipoMensagem + "|" + nomeUsuario + "|" + nomeGrupo;
      saida.writeObject(mensagem);
      saida.flush();
      System.out.println("Mensagem enviada: " + mensagem);

      // Aguarda a resposta do servidor
      ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
      String resposta = (String) entrada.readObject(); // Lê a resposta
      System.out.println("Resposta do servidor: " + resposta);
    } catch (IOException | ClassNotFoundException e) {
      System.err.println("Erro ao conectar ou enviar mensagem: " + e.getMessage());
      try {
        Thread.sleep(1000); // espera 1 segundo para escolher o novo servidor
      } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      conectarESalvarAPDU(tipoMensagem, nomeUsuario, nomeGrupo); //tenta novamente
      // escolherNovoServidor(tipoMensagem, nomeUsuario, nomeGrupo); // Se falhar, tenta outro
    }

  }

  /*
   * ***************************************************************
   * Metodo: enviarAPDUJoin
   * Funcao: Inicia uma thread para envio da APDU JOIN ao servidor.
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
   * Funcao: Inicia uma thread para envio da APDU LEAVE ao servidor.
   * Parametros: String nomeUsuario - nome do usuário
   * String nomeGrupo - nome do grupo a ser deixado
   * Retorno: void
   */
  public void enviarAPDULeave(String nomeUsuario, String nomeGrupo) {
    Thread threadLeave = new Thread(() -> conectarESalvarAPDU("LEAVE", nomeUsuario, nomeGrupo));
    threadLeave.start(); // Inicia a thread de envio LEAVE
  }

}
