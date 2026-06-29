package com.empresa.assinatura.infrastructure.adapter.out.persistence;

import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.domain.port.out.AssinaturaRepositoryPort;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementação do contrato de persistência de assinaturas utilizando R2DBC.
 */
@Component
@RequiredArgsConstructor
public class AssinaturaPersistenceAdapter implements AssinaturaRepositoryPort {

  private final AssinaturaR2dbcRepository r2dbcRepository;
  private final AssinaturaMapper mapper;

  @Override
  @Transactional
  public Mono<Assinatura> salvar(Assinatura assinatura) {
    AssinaturaEntity entidade = mapper.toEntity(assinatura);
    return r2dbcRepository.save(entidade).map(mapper::toDomain);
  }

  @Override
  public Mono<Assinatura> buscarPorId(UUID id) {
    return r2dbcRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public Mono<Assinatura> buscarAtivaDoUsuario(UUID usuarioId) {
    return r2dbcRepository.findAtivaByUsuarioId(usuarioId).map(mapper::toDomain);
  }

  @Override
  public Mono<Boolean> existeAssinaturaAtiva(UUID usuarioId) {
    return r2dbcRepository.existsByUsuarioIdAndStatus(usuarioId, StatusAssinatura.ATIVA);
  }

  @Override
  @Transactional
  public Flux<Assinatura> buscarParaRenovar(LocalDate data) {
    return r2dbcRepository.findParaRenovar(data).map(mapper::toDomain);
  }

  @Override
  public Flux<Assinatura> listar(String cursor, String status, String plano, int tamanho) {
    return r2dbcRepository.findWithCursor(cursor, status, plano, tamanho).map(mapper::toDomain);
  }
}
