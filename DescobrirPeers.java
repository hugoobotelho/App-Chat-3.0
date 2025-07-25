import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;

public class DescobrirPeers {
  private final static int porta = 2025;
  private static final String broadCast = "255.255.255.255";
  private static final int timeOutMs = 2000;
  private static final int intervaloDeSincronizacao = 5000; // Tempo entre sincronizações (5s)
  private Principal app;
  private InetAddress ipLocal;

  DescobrirPeers(Principal app) {
    this.app = app;
    try {
      this.ipLocal = InetAddress.getLocalHost();
    } catch (Exception e) {
      System.err.println("Erro ao obter IP local: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: iniciarSincronizacao.
   * Funcao: cria duas threads, uma para enviar mensagens para descobrir os
   * peer disponiveis e outra para receber essas mensagens e responder
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  public void iniciarDescobrimento() {

    new Thread(this::enviarSinc).start();
    new Thread(this::receberSinc).start();
  }

  /*
   * ***************************************************************
   * Metodo: enviarSinc.
   * Funcao: envia uma APDU SINC para os Peers via broadcast e espera uma
   * resposta que contem o horario da maquina do Peer que respondeu
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  private void enviarSinc() {
    try {
      DatagramSocket socket = new DatagramSocket();
      while (true) {
        byte[] mensagem = "AREYOUALIVE".getBytes();
        DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length, InetAddress.getByName(broadCast), porta);
        socket.send(pacote);
        System.out.println("APDU AREYOUALIVE enviada via broadcast");

        // espera respostas
        long inicio = System.currentTimeMillis();

        while (System.currentTimeMillis() - inicio < timeOutMs) {
          byte[] buffer = new byte[1024];
          DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
          try {

            socket.setSoTimeout(timeOutMs);
            socket.receive(resposta);
            String msg1 = new String(resposta.getData(), 0, resposta.getLength());

            System.out.println("Recebida de " + resposta.getAddress() + " a mensagem eh: " + msg1);

            if (!resposta.getAddress().getHostAddress().equals(ipLocal.getHostAddress())) { // ignora se foi ele mesmo
                                                                                            // que se respondeu
              String msg = new String(resposta.getData(), 0, resposta.getLength());
              if (msg.equals("IMALIVE")) {
                app.setPeersConhecidos(resposta.getAddress().getHostAddress()); // adiciona o ip do Peer descoberto
                System.out.println("Resposta recebida de " + resposta.getAddress());
              }
            }

          } catch (SocketTimeoutException e) {
            // TODO: handle exception
          }
        }

        Thread.sleep(intervaloDeSincronizacao);
      }

    } catch (Exception e) {
      System.err.println("Erro ao enviar APDU SINC: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: receberSinc.
   * Funcao: fica apto a receber mensagens de outros Peers,
   * caso a APDU que chegar for SINC, entao ele responde com o seu horario, se for
   * AREYOUALIVE, ele responde ao Peers que esta disponivel
   * Parametros: sem paramentros.
   * Retorno: sem retorno.
   * ***************************************************************
   */
  private void receberSinc() {
    try {
      DatagramSocket socket = new DatagramSocket(porta);
      while (true) {
        byte[] buffer = new byte[1024];
        DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacoteRecebido);

        String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());

        System.out.println("Recebi mensagem de " + pacoteRecebido.getAddress().getHostAddress() + " : " + mensagem);

        // if (mensagem.equals("SINC")) {
        // String horaAtual =
        // app.getHorarioMaquina().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        // byte[] resposta = ("HORA|" + horaAtual).getBytes();

        // DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length,
        // pacoteRecebido.getAddress(),
        // pacoteRecebido.getPort());
        // socket.send(pacoteResposta);
        // System.out.println("Respondi com meu horário: " + horaAtual);

        // } else
        if (mensagem.equals("AREYOUALIVE")
            && !pacoteRecebido.getAddress().getHostAddress().equals(ipLocal.getHostAddress())) {
          byte[] resposta = ("IMALIVE").getBytes();

          DatagramPacket pacoteResposta = new DatagramPacket(resposta, resposta.length, pacoteRecebido.getAddress(),
              pacoteRecebido.getPort());
          socket.send(pacoteResposta);
          System.out.println("Respondi que estou ativo");
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao receber APDU SINC: " + e.getMessage());
    }
  }

  /*
   * ***************************************************************
   * Metodo: obterBroadCast
   * Funcao: retorna o ip de broadcast da rede
   * Parametros: sem parametro.
   * Retorno: retorna um string
   * ***************************************************************
   */
  public static String obterBroadcast() {
    try {
      InetAddress ipLocal = InetAddress.getLocalHost();
      NetworkInterface netInterface = NetworkInterface.getByInetAddress(ipLocal);

      for (InterfaceAddress interfaceAddress : netInterface.getInterfaceAddresses()) {
        InetAddress broadcast = interfaceAddress.getBroadcast();
        if (broadcast != null) {
          return broadcast.getHostAddress();
        }
      }
    } catch (Exception e) {
      System.err.println("Erro ao obter o endereço de broadcast: " + e.getMessage());
    }
    return "255.255.255.255"; // Padrão genérico caso não seja possível detectar
  }

}
