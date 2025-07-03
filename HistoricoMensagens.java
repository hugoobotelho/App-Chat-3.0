
import java.util.ArrayList;
import java.util.List;

public class HistoricoMensagens {
  private List<Mensagem> mensagens;

  public HistoricoMensagens() {
    mensagens = new ArrayList<>();
  }

  /*
   * ***************************************************************
   * Metodo: adicionarMensagem
   * Funcao: Adiciona uma nova mensagem ao histórico de mensagens.
   * Parametros: Mensagem mensagem - objeto contendo a mensagem a ser armazenada.
   * Retorno: void
   */
  public void adicionarMensagem(Mensagem mensagem) {
    mensagens.add(mensagem);
  }

  /*
   * ***************************************************************
   * Metodo: getMensagens
   * Funcao: Retorna a lista de mensagens armazenadas no histórico.
   * Parametros: nenhum
   * Retorno: List<Mensagem> - lista contendo as mensagens.
   */
  public List<Mensagem> getMensagens() {
    return mensagens;
  }
}
