import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.image.Image;

public class Mensagem {
  private String remetente;
  private String conteudo;
  private String hora;
  private String status;
  private String timeStamp;
  private String nomeGrupo;
  Image check = new Image("/img/check.png");
  Image checkDuplo = new Image("/img/checkDuplo.png");
  Image checkVisto = new Image("/img/checkVisto.png");
  Image iconeVisualizacaoUnica = new Image("/img/visualizacaoUnicaAmarelo.png");
  Boolean visualizacaoUnica;
  private int qtdRecebimentos = 0;
  // private int qtdVistos = 0;
  Principal app;
  private Set<String> membrosVistos = new HashSet<>();
  private Set<String> membrosRecebidos = new HashSet<>();

  private String nomeUsuarioASerRemovido;
  private Boolean remocao;

  public Mensagem(Principal app, String remetente, String conteudo, String hora, String status, String timeStamp,
      String nomeGrupo, Boolean visualizacaoUnica, Boolean remocao) {
    this.app = app;
    this.remetente = remetente;
    this.conteudo = conteudo;
    this.hora = hora;
    this.status = status;
    this.timeStamp = timeStamp;
    this.nomeGrupo = nomeGrupo;
    this.visualizacaoUnica = visualizacaoUnica;
    this.remocao = remocao;
  }

  public Mensagem(String nomeUsuarioASerRemovido, Boolean remocao) {
    this.nomeUsuarioASerRemovido = nomeUsuarioASerRemovido;
    this.remocao = remocao;
    this.visualizacaoUnica = false;
  }

  public String getNomeUsuarioASerRemovido() {
    return nomeUsuarioASerRemovido;
  }

  public Boolean isRemove() {
    return remocao;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getStatus() {
    return status;
  }

  public Boolean getVisualizacaoUnica() {
    return visualizacaoUnica;
  }

  public Image getStatusImage() {
    // if (status.equals("check")) {
    // return check;
    // }
    if (status.equals("checkDuplo")) {
      return checkDuplo;
    }
    if (status.equals("checkVisto")) {
      return checkVisto;
    }
    // if (status.equals("unique")) {
    // return iconeVisualizacaoUnica;
    // }
    return check;
  }

  public void incrementaRecebimento(String nomeRecebido, String nomeGrupo) {
    System.out.println("Vai incrementar recebimento, nome Grupo: " + nomeGrupo + " nome Remetente: " + remetente);
    // if (app.getGrupos().contains(nomeGrupo) &&
    // remetente.equals(app.getNomeUsuario())) {
    // qtdVistos++;
    // }
    for (Map.Entry<String, Set<Usuario>> entry : app.getPeer().getGrupoManager().getGrupos().entrySet()) {
      // System.out.println("Grupo: " + entry.getKey());
      if (entry.getKey().equals(nomeGrupo)) {
        for (Usuario usuario : entry.getValue()) {
          // System.out.println(" - " + usuario.getNome()); // ou .toString() se preferir
          if (usuario.getNome().equals(nomeRecebido)) {
            System.out.println("MENSAGEM RECEBIDA POR " + nomeRecebido);
            // qtdRecebimentos++;
            membrosRecebidos.add(nomeRecebido);
          }
        }
      }
    }

    // if (qtdRecebimentos ==
    // (app.getPeer().getGrupoManager().obterMembros(nomeGrupo).size())
    // && !status.equals("checkVisto")) {
    // setStatus("checkDuplo");
    // app.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).renderizarMensagens();
    // }

    boolean flag = true;
    for (Usuario user : app.getPeer().getGrupoManager().obterMembros(nomeGrupo)) {
      if (!membrosRecebidos.contains(user.getNome())) {
        flag = false;
        break;
      }
    }

    boolean temOutroMembro = app.getPeer().getGrupoManager().obterMembros(nomeGrupo).stream()
        .anyMatch(m -> !m.getNome().equals(app.getNomeUsuario()));

    if (flag && !status.equals("checkVisto") && temOutroMembro) {
      setStatus("checkDuplo");
      app.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).renderizarMensagens();
    }

  }

  public void incrementaVistos(String nomeRecebido, String nomeGrupo) {
    System.out.println("Vai incrementar recebimento, nome Grupo: " + nomeGrupo + " nome Remetente: " + remetente);
    // if (app.getGrupos().contains(nomeGrupo) &&
    // remetente.equals(app.getNomeUsuario())) {
    // qtdVistos++;
    // }
    for (Map.Entry<String, Set<Usuario>> entry : app.getPeer().getGrupoManager().getGrupos().entrySet()) {
      // System.out.println("Grupo: " + entry.getKey());
      if (entry.getKey().equals(nomeGrupo)) {
        for (Usuario usuario : entry.getValue()) {
          // System.out.println(" - " + usuario.getNome()); // ou .toString() se preferir
          if (usuario.getNome().equals(nomeRecebido)) {
            System.out.println("MENSAGEM RECEBIDA POR " + nomeRecebido);
            // qtdVistos++;
            membrosVistos.add(nomeRecebido);
          }
        }
      }
    }
    // if (membrosVistos.size() ==
    // (app.getPeer().getGrupoManager().obterMembros(nomeGrupo).size())) {
    // setStatus("checkVisto");
    // app.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).renderizarMensagens();
    // }
    boolean flag = true;
    for (Usuario user : app.getPeer().getGrupoManager().obterMembros(nomeGrupo)) {
      if (!membrosVistos.contains(user.getNome())) {
        flag = false;
        break;
      }
    }

    boolean temOutroMembro = app.getPeer().getGrupoManager().obterMembros(nomeGrupo).stream()
        .anyMatch(m -> !m.getNome().equals(app.getNomeUsuario()));

    if (flag && temOutroMembro) {
      setStatus("checkVisto");
      app.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).renderizarMensagens();
    }
  }

  public String getNomeGrupoMensagem() {
    return nomeGrupo;
  }

  public String getTimeStampMensagem() {
    return timeStamp;
  }

  /*
   * ***************************************************************
   * Metodo: getRemetente
   * Funcao: Retorna o nome do remetente da mensagem.
   * Parametros: nenhum
   * Retorno: String - nome do remetente
   */
  public String getRemetente() {
    return remetente;
  }
  /*
   * ***************************************************************
   * Metodo: getConteudo
   * Funcao: Retorna o conteúdo da mensagem.
   * Parametros: nenhum
   * Retorno: String - conteúdo da mensagem
   */

  public String getConteudo() {
    return conteudo;
  }

  /*
   * ***************************************************************
   * Metodo: getHora
   * Funcao: Retorna o horário em que a mensagem foi enviada.
   * Parametros: nenhum
   * Retorno: String - horário da mensagem
   */
  public String getHora() {
    return hora;
  }

  /*
   * ***************************************************************
   * Metodo: toString
   * Funcao: Representa a mensagem como uma string formatada.
   * Parametros: nenhum
   * Retorno: String - mensagem formatada com remetente, conteúdo e hora
   */
  @Override
  public String toString() {
    return remetente + ": " + conteudo + ": " + hora;
  }
}
