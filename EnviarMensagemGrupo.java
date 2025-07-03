import java.net.*;
import java.security.MessageDigest;

public class EnviarMensagemGrupo {
  private final DatagramSocket peerSocket;
  private InetAddress enderecoServidor;
  private final int portaServidor;
  private Principal app;

  public EnviarMensagemGrupo(Principal app, String ipServidor, int portaServidor) throws Exception {
    this.app = app;
    this.peerSocket = new DatagramSocket(); // Socket para comunicação UDP


    this.enderecoServidor = InetAddress.getByName(ipServidor); // Endereço do Peer
    this.portaServidor = portaServidor;
  }

  /*
   * ***************************************************************
   * Metodo: enviarMensagem
   * Funcao: Envia uma mensagem UDP para o Peer configurado.
   * Parametros: String mensagem - conteúdo da mensagem a ser enviada.
   * Retorno: void
   */
  public void enviarMensagem(String mensagem) {
    try {
      byte[] dadosEnvio = mensagem.getBytes();
      DatagramPacket pacoteEnvio = new DatagramPacket(
          dadosEnvio,
          dadosEnvio.length,
          enderecoServidor,
          portaServidor);
      peerSocket.send(pacoteEnvio);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // /* ***************************************************************
  // * Metodo: fechar
  // * Funcao: Fecha o socket UDP utilizado para envio de mensagens.
  // * Parametros: sem parâmetros.
  // * Retorno: void
  // *************************************************************** */
  public void fechar() {
    // escutando = false; // Para a thread de escuta
    peerSocket.close();
  }
}
