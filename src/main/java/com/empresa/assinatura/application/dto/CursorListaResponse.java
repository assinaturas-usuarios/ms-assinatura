package com.empresa.assinatura.application.dto;

import java.util.List;
import java.util.function.Function;

/**
 * Resposta paginada por cursor para listagens.
 * 
 * <p><b>Cursor-based Pagination:</b> o método {@link #fromListaItens} encapsula a lógica 
 * de divisão da página e extração do cursor, evitando que use cases conheçam detalhes de paginação. 
 * A estratégia busca {@code tamanho + 1} registros: se retornar mais do que o solicitado, 
 * há próxima página, evitando a realização de querys que verificam a quantidade total de registros.
 *
 * @param <T> tipo dos itens da lista
 * @param itens itens da página atual
 * @param proximoCursor cursor para a próxima página, {@code null} se não houver
 * @param hasNext indica se existem mais páginas disponíveis
 */
public record CursorListaResponse<T>(
    List<T> itens,
    String proximoCursor,
    boolean hasNext
) {

  /**
   * Cria uma instância de CursorListaResponse a partir de uma lista de itens, tamanho da página
   * atual e função para extrair o cursor.
   *
   * @param listaItens         lista completa de itens
   * @param tamanhoPaginaAtual tamanho da página atual
   * @param cursorExtractor    função para extrair o cursor do último item da página
   * @return instância de CursorListaResponse
   */
  public static <T> CursorListaResponse<T> fromListaItens(
      List<T> listaItens, int tamanhoPaginaAtual, Function<T, String> cursorExtractor) {
    boolean hasNext = listaItens.size() > tamanhoPaginaAtual;
    List<T> paginaAtual = paginar(listaItens, tamanhoPaginaAtual, hasNext);
    String proximoCursor = extrairCursor(paginaAtual, hasNext, cursorExtractor);
    return new CursorListaResponse<>(paginaAtual, proximoCursor, hasNext);
  }

  private static <T> List<T> paginar(List<T> itens, int tamanhoPaginaAtual,
      boolean hasNext) {
    return hasNext ? itens.subList(0, tamanhoPaginaAtual) : itens;
  }

  private static <T> String extrairCursor(List<T> paginaAtual, boolean hasNext,
      Function<T, String> extractor) {
    return hasNext ? extractor.apply(paginaAtual.getLast()) : null;
  }
}
