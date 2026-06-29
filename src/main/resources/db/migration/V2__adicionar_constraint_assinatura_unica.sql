CREATE UNIQUE INDEX uk_assinatura_ativa_por_usuario
    ON assinaturas (usuario_id) WHERE status = 'ATIVA';
