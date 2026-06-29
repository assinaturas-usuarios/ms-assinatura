CREATE TABLE assinaturas
(
    id                     UUID        NOT NULL DEFAULT gen_random_uuid(),
    usuario_id             UUID        NOT NULL,
    plano                  VARCHAR(20) NOT NULL,
    data_inicio            DATE        NOT NULL,
    data_expiracao         DATE        NOT NULL,
    status                 VARCHAR(30) NOT NULL,
    tentativas_renovacao   INTEGER     NOT NULL DEFAULT 0,
    renovacao_em_andamento BOOLEAN     NOT NULL DEFAULT FALSE,
    data_inicio_renovacao  TIMESTAMP,
    versao                 BIGINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_assinaturas PRIMARY KEY (id)
);

CREATE INDEX idx_assinatura_usuario_id ON assinaturas (usuario_id);
CREATE INDEX idx_assinatura_status ON assinaturas (status);
CREATE INDEX idx_assinatura_data_expiracao ON assinaturas (data_expiracao);
CREATE INDEX idx_assinatura_usuario_status ON assinaturas (usuario_id, status);
