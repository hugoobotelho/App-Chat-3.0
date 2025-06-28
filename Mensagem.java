import java.time.LocalTime;

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
  private int qtdVistos = 0;
  Principal app;

  public Mensagem(Principal app, String remetente, String conteudo, String hora, String status, String timeStamp,
      String nomeGrupo) {
    this.app = app;
    this.remetente = remetente;
    this.conteudo = conteudo;
    this.hora = hora;
    this.status = status;
    this.timeStamp = timeStamp;
    this.nomeGrupo = nomeGrupo;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Image getStatus() {
    // if (status.equals("check")) {
    // return check;
    // }
    if (status.equals("checkDuplo")) {
      return checkDuplo;
    }
    if (status.equals("checkVisto")) {
      return checkVisto;
    }
    return check;
  }

  public void incrementaRecebimento(String remetente, String nomeGrupo) {
    System.out.println("Vai incrementar recebimento, nome Grupo: " + nomeGrupo + " nome Remetente: " + remetente);
    // if (app.getGrupos().contains(nomeGrupo) && remetente.equals(app.getNomeUsuario())) {
    //   qtdVistos++;
    // }
    if (app.getPeer().getGrupoManager().obterMembros(nomeGrupo).contains(remetente)) {
      System.out.println("MENSAGEM RECEBIDA POR " + remetente);
      qtdVistos++;
    }
    if (qtdVistos == (app.getPeer().getGrupoManager().obterMembros(nomeGrupo).size())) {
      setStatus("checkDuplo");
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
