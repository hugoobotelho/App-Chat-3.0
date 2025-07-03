
import java.net.*;
import java.io.*;
import java.util.Map;

public class PeerTCP {

  private final GrupoManager grupoManager;
  private final Map<String, Usuario> usuarios;
  private Peer peer;
  private Principal app;

  public PeerTCP(Principal app, GrupoManager grupoManager, Map<String, Usuario> usuarios, Peer peer) {
    this.app = app;
    this.grupoManager = grupoManager;
    this.usuarios = usuarios;
    this.peer = peer;
  }

  /*
   * ***************************************************************
   * Metodo: iniciar
   * Funcao: Inicia o Peer TCP na porta 6789, aguardando conexões.
   * Cada conexão é tratada por uma nova thread.
   * Parametros: nenhum
   * Retorno: void
   */
  public void iniciar() {
    try {
      int portaLocal = 6789;
      ServerSocket peerSocket = new ServerSocket(portaLocal); // Socket do peer
      System.out.println("Servidor TCP iniciado na porta " + portaLocal + "...");

      while (true) {
        Socket conexao = peerSocket.accept(); // Aceita conexões de Peers
        new Thread(new ProcessaCliente(conexao)).start();
      }
    } catch (Exception e) {
      System.err.println("Erro no peer TCP: " + e.getMessage());
    }
  }

  private class ProcessaCliente extends Thread {
    private final Socket conexao;

    public ProcessaCliente(Socket conexao) {
      this.conexao = conexao;
    }

    /*
     * ***************************************************************
     * Metodo: run
     * Funcao: Lê a mensagem recebida, processa a operação (JOIN/LEAVE),
     * envia resposta ao Peer e fecha a conexão.
     * Parametros: nenhum
     * Retorno: void
     */
    @Override
    public void run() {
      ObjectInputStream entrada = null;
      // ObjectOutputStream saida = null;
      try {
        entrada = new ObjectInputStream(conexao.getInputStream());
        while (true) {
          String mensagemRecebida = (String) entrada.readObject(); // Lê a mensagem do Peer
          System.out.println("Mensagem recebida via TCP: " + mensagemRecebida);

          String resposta = processarMensagem(mensagemRecebida, conexao);

          // saida = new ObjectOutputStream(conexao.getOutputStream());
          // saida.writeObject(resposta); // Envia a resposta
          // saida.flush(); // Garante que a resposta será enviada ao Peer
        }
      } catch (Exception e) {
        System.err.println("Erro de I/O ao processar Peer: " + e.getMessage());
        app.removePeerConhecido(conexao.getInetAddress().getHostAddress());

        System.out.println("Vai remover o usuario de endereco: " + conexao.getInetAddress().getHostAddress());
        System.out.println("Os peers conhecidos sao: " + app.getPeersConhecidos());
        if (app.getPeersConhecidos().contains(conexao.getInetAddress().getHostAddress())) {
          System.out.println("Vai verificar qual eh o nome do usuario");
          for (String nomeUsuario : app.getPeer().getUsuarios().keySet()) {
            // System.out.println(app.getPeer().getUsuarios().get(nomeUsuario).getEndereco().getHostAddress());
            if (app.getPeer().getUsuarios().containsKey(nomeUsuario)) {
              if (app.getPeer().getUsuarios().get(nomeUsuario).getEndereco().getHostAddress()
                  .equals(conexao.getInetAddress().getHostAddress())) {
                System.out.println("Vai chamar a funcao remover usuario de todos os grupos: " + nomeUsuario);
                // app.removePeerConhecido(app.getPeer().getUsuarios().get(nomeUsuario).getEndereco().getHostAddress());
                grupoManager.removerUsuarioTodosGrupos(nomeUsuario);
              }
            }
          }
        }

        try {
          if (entrada != null)
            entrada.close();
          // if (saida != null)
          //   saida.close();
          conexao.close(); // Fechar conexão ao final
        } catch (IOException ex) {
          System.err.println("Erro ao fechar a conexão: " + ex.getMessage());
        }
      }
    }

    /*
     * ***************************************************************
     * Metodo: processarMensagem
     * Funcao: Interpreta e executa o comando recebido (JOIN ou LEAVE).
     * Parametros:
     * String mensagem - mensagem no formato TIPO|USUARIO|GRUPO
     * Socket conexao - conexão com o Peer, usada para obter IP e porta
     * Retorno: String - resposta de sucesso ou erro da operação
     */
    private String processarMensagem(String mensagem, Socket conexao) {
      String[] partes = mensagem.split("\\|");
      if (partes.length < 3) {
        return "Erro: Mensagem mal formatada. Esperado TIPO|USUARIO|GRUPO.";
      }

      String tipo = partes[0].trim();
      String nomeUsuario = partes[1].trim();
      String nomeGrupo = partes[2].trim();

      Usuario usuario;
      synchronized (usuarios) {
        usuario = usuarios.computeIfAbsent(nomeUsuario,
            k -> new Usuario(nomeUsuario, conexao.getInetAddress(), conexao.getPort()));
      }

      synchronized (grupoManager) {
        // String newMessage;
        String mensagemComTimestamp;
        switch (tipo.toUpperCase()) {
          case "JOIN":
            // mensagemComTimestamp = mensagem + "|" + System.currentTimeMillis();
            // peer.setMessageLog(mensagemComTimestamp);

            // app.setMessageLog(mensagem); // adiciona a mensagem ao log de mensagens
            grupoManager.adicionarUsuario(nomeGrupo, usuario);
            return "Usuário " + nomeUsuario + " adicionado ao grupo " + nomeGrupo;

          case "LEAVE":
            // mensagemComTimestamp = mensagem + "|" + System.currentTimeMillis();
            // peer.setMessageLog(mensagemComTimestamp);

            // app.setMessageLog(mensagem); // adiciona a mensagem ao log de mensagens
            if (grupoManager.grupoExiste(nomeGrupo)) {
              grupoManager.removerUsuario(nomeGrupo, usuario);
              return "Usuário " + nomeUsuario + " removido do grupo " + nomeGrupo;
            } else {
              return "Erro: Grupo " + nomeGrupo + " não existe.";
            }
            // case "ATUALIZAR_JOIN":
            // // newMessage = "JOIN|" + nomeUsuario + "|" + nomeGrupo;
            // peer.setMessageLog(mensagem);
            // grupoManager.adicionarUsuario(nomeGrupo, usuario, true);
            // return "Fui atualizado com Usuário " + nomeUsuario + " adicionado ao grupo "
            // + nomeGrupo;

            // case "ATUALIZAR_LEAVE":
            // // newMessage = "LEAVE|" + nomeUsuario + "|" + nomeGrupo;
            // peer.setMessageLog(mensagem);
            // if (grupoManager.grupoExiste(nomeGrupo)) {
            // grupoManager.removerUsuario(nomeGrupo, usuario, true);
            // return "Fui atualizado com Usuário " + nomeUsuario + " removido do grupo " +
            // nomeGrupo;
            // } else {
            // return "Erro: Grupo " + nomeGrupo + " não existe.";
            // }

          default:
            return "Erro: Tipo de mensagem desconhecido. Use JOIN ou LEAVE.";
        }
      }
    }
  }
}