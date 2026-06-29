package com.empresa.assinatura.application.usecase;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.application.dto.CriarAssinaturaRequest;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.port.in.CriarAssinaturaUseCase;
import com.empresa.assinatura.domain.port.out.AssinaturaCachePort;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.domain.port.out.UsuarioClientPort;
import com.empresa.assinatura.infrastructure.exception.AssinaturaJaAtivaException;
import com.empresa.assinatura.infrastructure.exception.PlanoInvalidoException;
import com.empresa.assinatura.infrastructure.exception.UsuarioNaoEncontradoException;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Implementação do caso de uso para criar uma nova assinatura.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CriarAssinaturaUseCaseImpl implements CriarAssinaturaUseCase {

  private final AssinaturaRepositoryPort repository;
  private final UsuarioClientPort usuarioClient;
  private final AssinaturaCachePort cache;
  private final AssinaturaMapper mapper;

  @Override
  @Transactional
  public Mono<AssinaturaResponse> criar(CriarAssinaturaRequest request) {
    log.info("Criando assinatura para usuário: {}, plano: {}", request.usuarioId(),
        request.plano());
    return criarComValidacoes(request)
        .flatMap(this::armazenarNoCache)
        .doOnSuccess(this::logCriacao);
  }

  private Mono<AssinaturaResponse> criarComValidacoes(CriarAssinaturaRequest request) {
    Plano plano = Plano.fromNome(request.plano())
        .orElseThrow(() -> new PlanoInvalidoException(request.plano()));
    return validarCriacaoAssinatura(request)
        .flatMap(_ -> repository.salvar(Assinatura.nova(request.usuarioId(), plano)))
        .map(mapper::toResponse);
  }

  private Mono<Boolean> validarCriacaoAssinatura(CriarAssinaturaRequest request) {
    return buscarValidandoSeUsuarioExiste(request)
        .flatMap(_ -> repository.existeAssinaturaAtiva(request.usuarioId()))
        .flatMap(possuiAtiva -> validarAssinaturaUnica(possuiAtiva, request));
  }

  private Mono<Boolean> buscarValidandoSeUsuarioExiste(CriarAssinaturaRequest request) {
    return usuarioClient.existePorId(request.usuarioId())
        .flatMap(existeUsuario -> validarExistenciaUsuario(existeUsuario, request));
  }

  private Mono<AssinaturaResponse> armazenarNoCache(AssinaturaResponse response) {
    return cache.armazenar(response.id(), response).thenReturn(response);
  }

  private void logCriacao(AssinaturaResponse response) {
    log.info("Assinatura criada com sucesso: id={}", response.id());
  }

  private Mono<Boolean> validarExistenciaUsuario(Boolean existe, CriarAssinaturaRequest request) {
    if (Boolean.FALSE.equals(existe)) {
      return Mono.error(new UsuarioNaoEncontradoException(request.usuarioId()));
    }
    return Mono.just(true);
  }

  private Mono<Boolean> validarAssinaturaUnica(Boolean possuiAtiva,
      CriarAssinaturaRequest request) {
    if (Boolean.TRUE.equals(possuiAtiva)) {
      return Mono.error(new AssinaturaJaAtivaException(request.usuarioId()));
    }
    return Mono.just(false);
  }
}
