
import java.util.*;

public class GrupoManager {
  // Estrutura para armazenar grupos e seus membros
  private final Map<String, Set<Usuario>> grupos;
  private Peer app;
  private Principal appPrincipal;

  public GrupoManager(Peer app, Principal appPrincipal) {
    this.grupos = new HashMap<>();
    this.app = app;
    this.appPrincipal = appPrincipal;
  }

  /*
   * ***************************************************************
   * Metodo: adicionarUsuario
   * Funcao: Adiciona um usuário a um grupo. Cria o grupo se ele ainda não
   * existir.
   * Também envia APDU de "JOIN" para os Peers TCP conhecidos.
   * Parametros:
   * String nomeGrupo - nome do grupo
   * Usuario usuario - usuário que será adicionado
   * Retorno: void
   */
  public synchronized void adicionarUsuario(String nomeGrupo, Usuario usuario) {
    grupos.computeIfAbsent(nomeGrupo, k -> new HashSet<>()).add(usuario);
    if (appPrincipal.getTelaMeusGrupos().getTelasChat().get(nomeGrupo) != null) {
      appPrincipal.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).renderizarMensagens();
    }

    imprimirGrupos();

  }

  /*
   * ***************************************************************
   * Metodo: removerUsuario
   * Funcao: Remove um usuário de um grupo. Remove o grupo completamente se ele
   * ficar vazio.
   * Também envia APDU de "LEAVE" para os Peers TCP conhecidos.
   * Parametros:
   * String nomeGrupo - nome do grupo
   * Usuario usuario - usuário que será removido
   * Retorno: void
   */
  public synchronized void removerUsuario(String nomeGrupo, Usuario usuario) {
    if (grupos.containsKey(nomeGrupo)) {
      grupos.get(nomeGrupo).remove(usuario);
      if (appPrincipal.getTelaMeusGrupos().getTelasChat().containsKey(nomeGrupo)) {
        appPrincipal.getTelaMeusGrupos().getTelasChat().get(nomeGrupo).mostrarMensagemDeRemovido(usuario.getNome());
      }
      // Remove o grupo se ele estiver vazio
      if (grupos.get(nomeGrupo).isEmpty()) {
        grupos.remove(nomeGrupo);
      }
    }

    imprimirGrupos();

  }

  public void removerUsuarioTodosGrupos(String nomeUsuario) {
    System.out.println("USUARIO: " + nomeUsuario + " caiu. Removendo ele de todos os grupos... ");
    ArrayList<String> gruposQueFazParte = new ArrayList<>();
    Usuario user = null;
    for (String nomeGrupo : grupos.keySet()) {
      for (Usuario u : grupos.get(nomeGrupo)) {
        if (u.getNome().equals(nomeUsuario)) {
          user = u;
          // grupos.get(nomeGrupo).remove(user);
          gruposQueFazParte.add(nomeGrupo);
        }
      }
    }
    if (user != null) {
      for (String nomeGrupo : gruposQueFazParte) {
        removerUsuario(nomeGrupo, user);
      }
    }
  }

  public Map<String, Set<Usuario>> getGrupos() {
    return grupos;
  }

  /*
   * ***************************************************************
   * Metodo: obterMembros
   * Funcao: Retorna o conjunto de membros de um grupo específico.
   * Parametros: String nomeGrupo - nome do grupo
   * Retorno: Set<Usuario> - conjunto de usuários que pertencem ao grupo, ou
   * conjunto vazio se não existir
   */
  public synchronized Set<Usuario> obterMembros(String nomeGrupo) {
    return grupos.getOrDefault(nomeGrupo, Collections.emptySet());
  }

  /*
   * ***************************************************************
   * Metodo: grupoExiste
   * Funcao: Verifica se um grupo já existe no gerenciador.
   * Parametros: String nomeGrupo - nome do grupo a ser verificado
   * Retorno: boolean - true se o grupo existe, false caso contrário
   */
  public synchronized boolean grupoExiste(String nomeGrupo) {
    return grupos.containsKey(nomeGrupo);
  }

  /*
   * ***************************************************************
   * Metodo: imprimirGrupos
   * Funcao: Imprime todos os grupos e seus respectivos membros no console.
   * Parametros: nenhum
   * Retorno: void
   */
  public synchronized void imprimirGrupos() {
    if (grupos.isEmpty()) {
      System.out.println("Nenhum grupo cadastrado.");
      return;
    }

    System.out.println("==== GRUPOS ATUAIS ====");
    for (Map.Entry<String, Set<Usuario>> entry : grupos.entrySet()) {
      System.out.println("Grupo: " + entry.getKey());
      for (Usuario usuario : entry.getValue()) {
        System.out.println("  - " + usuario.getNome()); // ou .toString() se preferir
      }
    }
    System.out.println("========================");
  }
}
