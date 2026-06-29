package com.empresa.assinatura.infrastructure.mapper;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.infrastructure.adapter.out.persistence.AssinaturaEntity;
import org.mapstruct.Mapper;

/**
 * Mapper para converter entre entidades de domínio, entidades de persistência e DTOs.
 */
@Mapper(componentModel = "spring")
public interface AssinaturaMapper {

  /**
   * Converte entidade de domínio para entidade R2DBC.
   *
   * @param assinatura entidade de domínio
   * @return entidade R2DBC
   */
  AssinaturaEntity toEntity(Assinatura assinatura);

  /**
   * Converte entidade R2DBC para entidade de domínio.
   *
   * @param entidade entidade R2DBC
   * @return entidade de domínio
   */
  Assinatura toDomain(AssinaturaEntity entidade);

  /**
   * Converte entidade de domínio para DTO de resposta.
   *
   * @param assinatura entidade de domínio
   * @return DTO de resposta
   */
  AssinaturaResponse toResponse(Assinatura assinatura);
}
