
import java.io.IOException;
import java.net.*;
import java.util.Map;

public class PeerUDP {
  private final GrupoManager grupoManager;
  private final Map<String, Usuario> usuarios;
  Principal app;
  private final DatagramSocket peerSocket1; // escuta

  public PeerUDP(GrupoManager grupoManager, Map<String, Usuario> usuarios, Principal app) throws SocketException {
    this.grupoManager = grupoManager;
    this.usuarios = usuarios;
    this.app = app;
    this.peerSocket1 = new DatagramSocket(2345); // Para receber "RECEBIDO"

  }

  /*
   * ***************************************************************
   * Metodo: iniciar
   * Funcao: Inicia o peer UDP na porta 6789 e aguarda mensagens dos peers.
   * A cada mensagem recebida, cria uma nova thread para processá-la.
   * Parametros: nenhum
   * Retorno: void
   */
  public void iniciar() {
    try {
      DatagramSocket peerSocketS = new DatagramSocket(1234); // Porta do peer
      System.out.println("Servidor UDP iniciado na porta 6789...");
      new Thread(() -> {
        while (true) {
          // Buffer para receber dados
          byte[] dadosRecebidos = new byte[1024];
          DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);

          // Aguarda uma mensagem de um peer
          try {
            peerSocketS.receive(pacoteRecebido);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          // Inicia uma nova thread para processar a mensagem recebida
          new Thread(new ProcessaMensagem(pacoteRecebido, peerSocketS)).start();
        }
      }).start();
      new Thread(() -> {
        while (true) {
          // Espera confirmação
          String resposta = receberConfirmacao();
          System.out.println("Recebi a resposta da minha mensagem: " + resposta);
          if (resposta.startsWith("RECEBIDO|") || resposta.startsWith("VISTO|")) {
            String[] partes = resposta.split("\\|");

            String apdu = partes[0];
            String grupo = partes[1];
            String remetente = partes[2];
            String timestamp = partes[4];

            // app.getPeer().getGrupoManager().historicoMensagens.atualizarStatusMensagem(timestamp,
            // grupo);
            System.out.print("Vendo se existe historico de mensagens do grupo");
            if (app.getTelaMeusGrupos().getHistoricoMensagensGrupo(grupo) != null) {
              System.out.println("Existe!!!");
              for (Mensagem m : app.getTelaMeusGrupos().getHistoricoMensagensGrupo(grupo).getMensagens()) {
                if (m.getTimeStampMensagem() != null) {
                  if (m.getTimeStampMensagem().equals(timestamp) && m.getNomeGrupoMensagem().equals(grupo)) {
                    if (apdu.equals("RECEBIDO")) {
                      m.incrementaRecebimento(remetente, grupo);
                    } else if (apdu.equals("VISTO")) {
                      m.incrementaVistos(remetente, grupo);
                    }
                  }
                }
              }
            } else {
              System.out.println("NAO Existe!!!");
            }

          } else {
            System.out.println("Falha no envio: " + resposta);
          }
        }
      }).start();

    } catch (Exception e) {
      System.err.println("Erro no peer UDP: " + e.getMessage());
    }
  }

  public String receberConfirmacao() {
    try {
      byte[] dadosRecebidos = new byte[1024];
      DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
      peerSocket1.receive(pacoteRecebido); // Bloqueia até receber um pacote
      return new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
    } catch (Exception e) {
      e.printStackTrace();
      return "Erro";
    }
  }

  private class ProcessaMensagem extends Thread {
    private final DatagramPacket pacoteRecebido;
    private final DatagramSocket peerSocketS;

    public ProcessaMensagem(DatagramPacket pacoteRecebido, DatagramSocket peerSocketS) {
      this.pacoteRecebido = pacoteRecebido;
      this.peerSocketS = peerSocketS;
    }

    /*
     * ***************************************************************
     * Metodo: run
     * Funcao: Executa o processamento da mensagem recebida no formato:
     * "SEND|Grupo|Usuario|Mensagem", e reencaminha a mensagem
     * para os demais membros do grupo, exceto o remetente.
     * Parametros: nenhum
     * Retorno: void
     */
    @Override
    public void run() {
      try {
        // Converte os dados recebidos em String
        String mensagemRecebida = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
        System.out.println(
            "Mensagem recebida de " + pacoteRecebido.getAddress() + ":" + pacoteRecebido.getPort());
        System.out.println("Conteudo: " + mensagemRecebida);

        // Divide a mensagem pela estrutura definida:
        // "SEND|NomeGrupo|NomeUsuario|Mensagem"
        String[] partes = mensagemRecebida.split("\\|", 5);
        if (partes.length != 5) {
          System.err.println("Formato inválido ou tipo de mensagem desconhecido.");
          return;
        }

        // Extração dos campos
        String tipoMensagem = partes[0];
        String nomeGrupo = partes[1];
        String nomeUsuario = partes[2];
        String conteudoMensagem = partes[3];
        String timeStamp = partes[4];

        // Verifica se a mensagem é do tipo SEND
        if (tipoMensagem.equals("SEND") || tipoMensagem.equals("SENDUNIQUE")) {
          // Sincroniza para garantir consistência na manipulação de usuários
          Usuario remetente;
          synchronized (usuarios) {
            remetente = usuarios.computeIfAbsent(nomeUsuario,
                k -> new Usuario(nomeUsuario, pacoteRecebido.getAddress(), pacoteRecebido.getPort()));
          }

          // Envia confirmação de recebimento ao remetente
          String resposta = "RECEBIDO|" + nomeGrupo + "|" + app.getNomeUsuario() + "|" + conteudoMensagem + "|"
              + timeStamp;
          byte[] dadosResposta = resposta.getBytes();
          DatagramPacket pacoteResposta = new DatagramPacket(
              dadosResposta,
              dadosResposta.length,
              pacoteRecebido.getAddress(),
              2345);
          peerSocketS.send(pacoteResposta);

          app.processarMensagemRecebida(mensagemRecebida);
        } 
        // else if (tipoMensagem.equals("SENDUNIQUE")) {
        //   app.processarMensagemRecebida(mensagemRecebida);
        // }

        else {
          System.err.println("Tipo de mensagem desconhecido: " + tipoMensagem);
        }
      } catch (Exception e) {
        System.err.println("Erro ao processar mensagem: " + e.getMessage());
      }
    }
  }
}
