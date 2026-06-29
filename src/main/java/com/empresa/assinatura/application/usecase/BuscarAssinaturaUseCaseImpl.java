package com.empresa.assinatura.application.usecase;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CursorListaResponse;
import com.empresa.assinatura.domain.port.in.BuscarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoEncontradaException;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Implementação do caso de uso para buscar assinaturas.
 *
 * <p><b>Cache Reativo com Redis:</b> o método {@code buscarPorId} consulta o cache antes 
 * do banco via {@code switchIfEmpty}, armazenando o resultado no Redis após a primeira 
 * leitura para evitar acessos desnecessários.
 *
 * <p><b>Cursor-based Pagination:</b> o método
 * {@code listar} usa paginação por cursor em vez de offset. 
 * Otimizando consultas em grandes volumes de dados, evitando problemas de performance e 
 * inconsistência de resultados.
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BuscarAssinaturaUseCaseImpl implements BuscarAssinaturaUseCase {

  private final AssinaturaRepositoryPort repositorio;
  private final AssinaturaCachePort cache;
  private final AssinaturaMapper mapper;

  private static Function<List<AssinaturaResponse>, CursorListaResponse<AssinaturaResponse>>
              paginador(int tamanhoPaginaAtual) {
    return itens -> CursorListaResponse.fromListaItens(itens, tamanhoPaginaAtual,
        r -> String.valueOf(r.id()));
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<AssinaturaResponse> buscarPorId(UUID id) {
    log.info("Buscando assinatura: id={}", id);
    return cache.buscar(id)
        .switchIfEmpty(buscarNoBancoPorId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<AssinaturaResponse> buscarAtivaDoUsuario(UUID usuarioId) {
    log.info("Buscando assinatura ativa do usuario: {}", usuarioId);
    return repositorio.buscarAtivaDoUsuario(usuarioId)
        .map(mapper::toResponse)
        .switchIfEmpty(Mono.error(new AssinaturaNaoEncontradaException(usuarioId)));
  }

  @Override
  @Transactional(readOnly = true)
  public Mono<CursorListaResponse<AssinaturaResponse>> listar(String cursor, String status,
      String plano, int tamanhoPaginaAtual) {
    log.info("Listando assinaturas: cursor Id={}, status={}, plano={}, tamanhoPaginaAtual={}",
        cursor, status,
        plano, tamanhoPaginaAtual);
    return repositorio.listar(cursor, status, plano, tamanhoPaginaAtual + 1)
        .map(mapper::toResponse)
        .collectList()
        .map(paginador(tamanhoPaginaAtual));
  }

  private Mono<AssinaturaResponse> buscarNoBancoPorId(UUID id) {
    return repositorio.buscarPorId(id)
        .map(mapper::toResponse)
        .flatMap(armazenarNoCache(id))
        .switchIfEmpty(Mono.error(new AssinaturaNaoEncontradaException(id)));
  }

  private Function<AssinaturaResponse, Mono<AssinaturaResponse>> armazenarNoCache(UUID id) {
    return response -> cache.armazenar(id, response).thenReturn(response);
  }
}
