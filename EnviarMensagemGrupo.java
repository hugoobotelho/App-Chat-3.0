import java.net.*;
import java.security.MessageDigest;

public class EnviarMensagemGrupo {
  private final DatagramSocket clienteSocket;
  // private final DatagramSocket clienteSocket1;
  private InetAddress enderecoServidor;
  private final int portaServidor;
  private final DatagramSocket clienteSocket1; // escuta
  private Principal app;

  public EnviarMensagemGrupo(Principal app, String ipServidor, int portaServidor) throws Exception {
    this.app = app;
    this.clienteSocket = new DatagramSocket(); // Socket para comunicação UDP

    this.clienteSocket1 = new DatagramSocket(1234); // Para receber "RECEBIDO"

    this.enderecoServidor = InetAddress.getByName(ipServidor); // Endereço do servidor
    this.portaServidor = portaServidor;
    // this.clienteSocket1 = new DatagramSocket(1234); // Escolha uma porta fixada
  }

  /*
   * ***************************************************************
   * Metodo: enviarMensagem
   * Funcao: Envia uma mensagem UDP para o servidor configurado.
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
      clienteSocket.send(pacoteEnvio);

      // Espera confirmação
      String resposta = receberConfirmacao();
      if (resposta.startsWith("RECEBIDO|")) {
        String[] partes = resposta.split("\\|");
        String remetente = partes[1];
        String grupo = partes[2];
        String timestamp = partes[3];

        // app.getPeer().getGrupoManager().historicoMensagens.atualizarStatusMensagem(timestamp, grupo);
        for (Mensagem m : app.getTelaMeusGrupos().getHistoricoMensagensGrupo(grupo).getMensagens()) {
          if (m.getTimeStampMensagem().equals(timestamp) && m.getNomeGrupoMensagem().equals(grupo)){
            m.incrementaRecebimento(remetente, grupo);
          }
        }
        
      } else {
        System.out.println("Falha no envio: " + resposta);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String receberConfirmacao() {
    try {
      byte[] dadosRecebidos = new byte[1024];
      DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
      clienteSocket1.receive(pacoteRecebido); // Bloqueia até receber um pacote
      return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
    } catch (Exception e) {
      e.printStackTrace();
      return "Erro";
    }
  }

  /*
   * ***************************************************************
   * Metodo: receberMensagem
   * Funcao: Aguarda e retorna uma mensagem recebida do servidor.
   * Parametros: sem parâmetros.
   * Retorno: String - conteúdo da mensagem recebida.
   */
  // public String receberMensagem() {
  // try {
  // byte[] dadosRecebidos = new byte[1024];
  // DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos,
  // dadosRecebidos.length);
  // clienteSocket1.receive(pacoteRecebido); // Bloqueia até receber um pacote
  // return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
  // } catch (Exception e) {
  // e.printStackTrace();
  // return "Erro";
  // }
  // }

  // /* ***************************************************************
  // * Metodo: setIpServidor
  // * Funcao: Atualiza o IP do servidor utilizado pelo cliente UDP.
  // * Parametros: String novoIP - novo endereço IP do servidor.
  // * Retorno: void
  // *************************************************************** */
  // public void setIpServidor(String novoIP) {
  // try {
  // enderecoServidor = InetAddress.getByName(novoIP);
  // } catch (Exception e) {
  // // TODO: handle exception
  // }
  // }

  // /* ***************************************************************
  // * Metodo: fechar
  // * Funcao: Fecha o socket UDP utilizado para envio de mensagens.
  // * Parametros: sem parâmetros.
  // * Retorno: void
  // *************************************************************** */
  public void fechar() {
    // escutando = false; // Para a thread de escuta
    clienteSocket.close();
  }
}
