package com.empresa.assinatura.infrastructure.adapter.out.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.infrastructure.mapper.AssinaturaMapper;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssinaturaPersistenceAdapter")
class AssinaturaPersistenceAdapterTest {

  @Mock
  private AssinaturaR2dbcRepository r2dbcRepository;
  @Mock
  private AssinaturaMapper mapper;
  @InjectMocks
  private AssinaturaPersistenceAdapter adapter;

  private AssinaturaEntity entidade() {
    return mock(AssinaturaEntity.class);
  }

  private Assinatura assinatura() {
    return Assinatura.nova(UUID.randomUUID(), Plano.PREMIUM);
  }

  @Test
  @DisplayName("salvar: persiste e retorna dominio")
  void deveSalvar() {
    Assinatura a = assinatura();
    AssinaturaEntity ent = entidade();
    when(mapper.toEntity(any())).thenReturn(ent);
    when(r2dbcRepository.save(ent)).thenReturn(Mono.just(ent));
    when(mapper.toDomain(ent)).thenReturn(a);
    StepVerifier.create(adapter.salvar(a)).expectNext(a).verifyComplete();
  }

  @Test
  @DisplayName("buscarPorId: retorna dominio quando encontrado")
  void deveBuscarPorId() {
    UUID id = UUID.randomUUID();
    Assinatura a = assinatura();
    AssinaturaEntity ent = entidade();
    when(r2dbcRepository.findById(id)).thenReturn(Mono.just(ent));
    when(mapper.toDomain(ent)).thenReturn(a);
    StepVerifier.create(adapter.buscarPorId(id)).expectNext(a).verifyComplete();
  }

  @Test
  @DisplayName("buscarAtivaDoUsuario: retorna dominio quando encontrado")
  void deveBuscarAtivaDoUsuario() {
    UUID uid = UUID.randomUUID();
    Assinatura a = assinatura();
    AssinaturaEntity ent = entidade();
    when(r2dbcRepository.findAtivaByUsuarioId(uid)).thenReturn(Mono.just(ent));
    when(mapper.toDomain(ent)).thenReturn(a);
    StepVerifier.create(adapter.buscarAtivaDoUsuario(uid)).expectNext(a).verifyComplete();
  }

  @Test
  @DisplayName("existeAssinaturaAtiva: delega para repository")
  void deveVerificarExistencia() {
    UUID uid = UUID.randomUUID();
    when(r2dbcRepository.existsByUsuarioIdAndStatus(uid, StatusAssinatura.ATIVA)).thenReturn(
        Mono.just(true));
    StepVerifier.create(adapter.existeAssinaturaAtiva(uid)).expectNext(true).verifyComplete();
  }

  @Test
  @DisplayName("buscarParaRenovar: retorna flux de dominios")
  void deveBuscarParaRenovar() {
    LocalDate hoje = LocalDate.now();
    Assinatura a = assinatura();
    AssinaturaEntity ent = entidade();
    when(r2dbcRepository.findParaRenovar(hoje)).thenReturn(Flux.just(ent));
    when(mapper.toDomain(ent)).thenReturn(a);
    StepVerifier.create(adapter.buscarParaRenovar(hoje)).expectNext(a).verifyComplete();
  }

  @Test
  @DisplayName("listar: retorna flux de dominios com cursor")
  void deveListar() {
    Assinatura a = assinatura();
    AssinaturaEntity ent = entidade();
    when(r2dbcRepository.findWithCursor(any(), any(), any(), anyInt())).thenReturn(Flux.just(ent));
    when(mapper.toDomain(ent)).thenReturn(a);
    StepVerifier.create(adapter.listar(null, null, null, 10)).expectNext(a).verifyComplete();
  }
}