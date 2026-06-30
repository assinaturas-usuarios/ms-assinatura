package com.empresa.assinatura.infrastructure.mapper;

import com.empresa.assinatura.application.dto.AssinaturaResponse;
import com.empresa.assinatura.domain.model.Assinatura;
import com.empresa.assinatura.domain.model.Plano;
import com.empresa.assinatura.domain.model.StatusAssinatura;
import com.empresa.assinatura.infrastructure.adapter.out.persistence.AssinaturaEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T22:33:32-0300",
    comments = "version: 1.6.2, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class AssinaturaMapperImpl implements AssinaturaMapper {

    @Override
    public AssinaturaEntity toEntity(Assinatura assinatura) {
        if ( assinatura == null ) {
            return null;
        }

        AssinaturaEntity.AssinaturaEntityBuilder assinaturaEntity = AssinaturaEntity.builder();

        assinaturaEntity.id( assinatura.getId() );
        assinaturaEntity.usuarioId( assinatura.getUsuarioId() );
        assinaturaEntity.plano( assinatura.getPlano() );
        assinaturaEntity.dataInicio( assinatura.getDataInicio() );
        assinaturaEntity.dataExpiracao( assinatura.getDataExpiracao() );
        assinaturaEntity.status( assinatura.getStatus() );
        assinaturaEntity.tentativasRenovacao( assinatura.getTentativasRenovacao() );
        assinaturaEntity.renovacaoEmAndamento( assinatura.isRenovacaoEmAndamento() );
        assinaturaEntity.dataInicioRenovacao( assinatura.getDataInicioRenovacao() );
        assinaturaEntity.versao( assinatura.getVersao() );

        return assinaturaEntity.build();
    }

    @Override
    public Assinatura toDomain(AssinaturaEntity entidade) {
        if ( entidade == null ) {
            return null;
        }

        UUID id = null;
        UUID usuarioId = null;
        Plano plano = null;
        LocalDate dataInicio = null;
        LocalDate dataExpiracao = null;
        StatusAssinatura status = null;
        int tentativasRenovacao = 0;
        boolean renovacaoEmAndamento = false;
        LocalDateTime dataInicioRenovacao = null;
        Long versao = null;

        id = entidade.getId();
        usuarioId = entidade.getUsuarioId();
        plano = entidade.getPlano();
        dataInicio = entidade.getDataInicio();
        dataExpiracao = entidade.getDataExpiracao();
        status = entidade.getStatus();
        tentativasRenovacao = entidade.getTentativasRenovacao();
        renovacaoEmAndamento = entidade.isRenovacaoEmAndamento();
        dataInicioRenovacao = entidade.getDataInicioRenovacao();
        versao = entidade.getVersao();

        Assinatura assinatura = new Assinatura( id, usuarioId, plano, dataInicio, dataExpiracao, status, tentativasRenovacao, renovacaoEmAndamento, dataInicioRenovacao, versao );

        return assinatura;
    }

    @Override
    public AssinaturaResponse toResponse(Assinatura assinatura) {
        if ( assinatura == null ) {
            return null;
        }

        UUID id = null;
        UUID usuarioId = null;
        Plano plano = null;
        LocalDate dataInicio = null;
        LocalDate dataExpiracao = null;
        StatusAssinatura status = null;
        int tentativasRenovacao = 0;

        id = assinatura.getId();
        usuarioId = assinatura.getUsuarioId();
        plano = assinatura.getPlano();
        dataInicio = assinatura.getDataInicio();
        dataExpiracao = assinatura.getDataExpiracao();
        status = assinatura.getStatus();
        tentativasRenovacao = assinatura.getTentativasRenovacao();

        AssinaturaResponse assinaturaResponse = new AssinaturaResponse( id, usuarioId, plano, dataInicio, dataExpiracao, status, tentativasRenovacao );

        return assinaturaResponse;
    }
}
