CREATE TYPE papel_organizacao AS ENUM ('RECRUTADOR','ADMIN');
CREATE TYPE nivel_competencia AS ENUM ('BASICO','INTERMEDIARIO','AVANCADO');
CREATE TYPE status_vaga AS ENUM ('ABERTA','FECHADA','CANCELADA');
CREATE TYPE tipo_contrato AS ENUM ('ESTAGIO','CLT','PJ');
CREATE TYPE modalidade_trabalho AS ENUM ('PRESENCIAL','REMOTO','HIBRIDO');
CREATE TYPE status_candidatura AS ENUM ('PENDENTE','ACEITA','REJEITADA','DESISTENTE','EM_PROCESSO','FINALIZADA');

CREATE TABLE usuario
(
    id               UUID PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    cpf              VARCHAR(14) UNIQUE,
    senha_hash       TEXT         NOT NULL,
    email_verificado BOOLEAN      NOT NULL DEFAULT FALSE,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE organizacao
(
    id              UUID PRIMARY KEY,
    cnpj            VARCHAR(14)  NOT NULL UNIQUE,
    nome            VARCHAR(100) NOT NULL,
    end_logradouro  VARCHAR(80),
    end_numero      VARCHAR(20),
    end_complemento VARCHAR(50),
    end_cep         VARCHAR(10),
    end_cidade      VARCHAR(50),
    end_uf          VARCHAR(2),
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE membro_organizacao
(
    organizacao_id UUID              NOT NULL REFERENCES organizacao (id) ON DELETE CASCADE,
    usuario_id     UUID              NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    papel          papel_organizacao NOT NULL,
    ativo          BOOLEAN           NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ       NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ       NOT NULL DEFAULT now(),
    PRIMARY KEY (organizacao_id, usuario_id)
);

CREATE TABLE perfil_candidato
(
    usuario_id      UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    data_nascimento DATE        NOT NULL,
    end_logradouro  VARCHAR(80),
    end_numero      VARCHAR(20),
    end_complemento VARCHAR(50),
    end_cep         VARCHAR(10),
    end_cidade      VARCHAR(50),
    end_uf          VARCHAR(2),
    ativo           BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE portfolio
(
    id            UUID PRIMARY KEY,
    usuario_id    UUID         NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    titulo        VARCHAR(100) NOT NULL,
    link          VARCHAR(255) NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE historico_academico
(
    id            UUID PRIMARY KEY,
    usuario_id    UUID        NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    titulo        VARCHAR(80) NOT NULL,
    descricao     TEXT,
    instituicao   VARCHAR(80) NOT NULL,
    data_inicio   DATE        NOT NULL,
    data_fim      DATE,
    ativo         BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE TABLE competencia
(
    id            UUID PRIMARY KEY,
    usuario_id    UUID              NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    titulo        VARCHAR(50)       NOT NULL,
    descricao     TEXT              NOT NULL,
    nivel         nivel_competencia NOT NULL,
    ativo         BOOLEAN           NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ       NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ       NOT NULL DEFAULT now()
);

CREATE TABLE certificado
(
    id            UUID PRIMARY KEY,
    usuario_id    UUID         NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    titulo        VARCHAR(100) NOT NULL,
    instituicao   VARCHAR(100) NOT NULL,
    data_emissao  DATE         NOT NULL,
    data_validade DATE,
    descricao     TEXT,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (data_validade IS NULL OR data_validade >= data_emissao)
);

CREATE TABLE experiencia_profissional
(
    id            UUID PRIMARY KEY,
    usuario_id    UUID        NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    cargo         VARCHAR(80) NOT NULL,
    empresa       VARCHAR(80) NOT NULL,
    descricao     TEXT        NOT NULL,
    data_inicio   DATE        NOT NULL,
    data_fim      DATE,
    ativo         BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (data_fim IS NULL OR data_fim >= data_inicio)
);

CREATE TABLE projeto_experiencia
(
    id             UUID PRIMARY KEY,
    experiencia_id UUID        NOT NULL REFERENCES experiencia_profissional (id) ON DELETE CASCADE,
    titulo         VARCHAR(80) NOT NULL,
    descricao      TEXT        NOT NULL,
    ativo          BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE beneficio_org
(
    id             UUID PRIMARY KEY,
    organizacao_id UUID        NOT NULL REFERENCES organizacao (id) ON DELETE CASCADE,
    titulo         VARCHAR(50) NOT NULL,
    descricao      TEXT,
    ativo          BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organizacao_id, titulo)
);

CREATE TABLE nivel_experiencia
(
    id             UUID PRIMARY KEY,
    organizacao_id UUID        NOT NULL REFERENCES organizacao (id) ON DELETE CASCADE,
    descricao      VARCHAR(50) NOT NULL,
    ativo          BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (organizacao_id, descricao)
);

CREATE TABLE vaga
(
    id                    UUID PRIMARY KEY,
    organizacao_id        UUID                NOT NULL REFERENCES organizacao (id) ON DELETE CASCADE,
    recrutador_usuario_id UUID                NOT NULL REFERENCES usuario (id) ON DELETE RESTRICT,
    titulo                VARCHAR(80)         NOT NULL,
    descricao             TEXT                NOT NULL,
    requisitos            TEXT                NOT NULL,
    salario               NUMERIC(10, 2),
    data_publicacao       DATE                NOT NULL,
    status                status_vaga         NOT NULL,
    motivo_cancelamento   TEXT,
    tipo_contrato         tipo_contrato       NOT NULL,
    modalidade            modalidade_trabalho NOT NULL,
    horario_trabalho      VARCHAR(30)         NOT NULL,
    nivel_experiencia_id  UUID REFERENCES nivel_experiencia (id) ON DELETE RESTRICT,
    end_cidade            VARCHAR(50),
    end_uf                VARCHAR(2),

    ativo                 BOOLEAN             NOT NULL DEFAULT TRUE,
    criado_em             TIMESTAMPTZ         NOT NULL DEFAULT now(),
    atualizado_em         TIMESTAMPTZ         NOT NULL DEFAULT now()
);

ALTER TABLE vaga
    ADD CONSTRAINT chk_vaga_endereco_por_modalidade
        CHECK (
            (modalidade <> 'PRESENCIAL' AND modalidade <> 'HIBRIDO')
                OR (
                end_cidade IS NOT NULL
                    AND end_uf IS NOT NULL
                )
            );

CREATE TABLE vaga_beneficio
(
    vaga_id      UUID NOT NULL REFERENCES vaga (id) ON DELETE CASCADE,
    beneficio_id UUID NOT NULL REFERENCES beneficio_org (id) ON DELETE RESTRICT,
    PRIMARY KEY (vaga_id, beneficio_id)
);

CREATE TABLE candidatura
(
    id                   UUID PRIMARY KEY,
    vaga_id              UUID               NOT NULL REFERENCES vaga (id) ON DELETE CASCADE,
    candidato_usuario_id UUID               NOT NULL REFERENCES usuario (id) ON DELETE RESTRICT,
    status               status_candidatura NOT NULL,
    data_candidatura     DATE               NOT NULL,
    arquivo_curriculo    VARCHAR(255),
    compatibilidade      NUMERIC(5, 2),
    criado_em            TIMESTAMPTZ        NOT NULL DEFAULT now(),
    atualizado_em        TIMESTAMPTZ        NOT NULL DEFAULT now(),
    UNIQUE (vaga_id, candidato_usuario_id)
);

CREATE TABLE etapas_processo
(
    id           UUID PRIMARY KEY,
    vaga_id      UUID         NOT NULL,
    nome         VARCHAR(100) NOT NULL,
    descricao    TEXT,
    tipo         VARCHAR(50)  NOT NULL,
    ordem        INTEGER      NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    data_inicio  TIMESTAMP,
    data_fim     TIMESTAMP,
    data_criacao TIMESTAMP    NOT NULL,

    CONSTRAINT fk_etapa_vaga FOREIGN KEY (vaga_id) REFERENCES vaga (id) ON DELETE CASCADE
);

CREATE TABLE processo_seletivo
(
    id                      UUID PRIMARY KEY,
    candidatura_id          UUID        NOT NULL UNIQUE REFERENCES candidatura (id) ON DELETE CASCADE,
    etapa_processo_atual_id UUID        NOT NULL REFERENCES etapas_processo (id) ON DELETE RESTRICT,
    data_inicio             TIMESTAMPTZ NOT NULL,
    data_fim                TIMESTAMPTZ,
    data_ultima_mudanca     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE vaga_externa
(
    id                 UUID PRIMARY KEY,
    titulo             VARCHAR(50) NOT NULL,
    descricao          TEXT        NOT NULL,
    requisitos         TEXT        NOT NULL,
    arquivo_curriculo  TEXT,
    conteudo_curriculo TEXT,
    modelo_curriculo   VARCHAR(20),
    usuario_id         UUID        NOT NULL REFERENCES perfil_candidato (usuario_id) ON DELETE CASCADE,
    ativo              BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_modelo_curriculo
        CHECK (modelo_curriculo IS NULL OR modelo_curriculo IN ('PROFISSIONAL', 'CRIATIVO', 'EXECUTIVO', 'ACADEMICO'))
);

CREATE TABLE refresh_token
(
    id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    token      TEXT      NOT NULL UNIQUE,
    usuario_id UUID      NOT NULL,
    expira_em  TIMESTAMP NOT NULL,
    criado_em  TIMESTAMP NOT NULL,
    revogado   BOOLEAN   NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE historico_etapa_processo
(
    id                UUID PRIMARY KEY,
    processo_id       UUID        NOT NULL REFERENCES processo_seletivo (id) ON DELETE CASCADE,
    etapa_anterior_id UUID REFERENCES etapas_processo (id) ON DELETE RESTRICT,
    etapa_nova_id     UUID        NOT NULL REFERENCES etapas_processo (id) ON DELETE RESTRICT,
    usuario_id        UUID        NOT NULL REFERENCES usuario (id) ON DELETE RESTRICT,
    feedback          TEXT        NOT NULL,
    data_mudanca      TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE avaliacao_organizacao
(
    id                   UUID PRIMARY KEY,
    processo_id          UUID        NOT NULL UNIQUE REFERENCES processo_seletivo (id) ON DELETE CASCADE,
    candidato_usuario_id UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    organizacao_id       UUID        NOT NULL REFERENCES organizacao (id) ON DELETE CASCADE,
    nota                 INT         NOT NULL CHECK (nota >= 1 AND nota <= 5),
    comentario           TEXT        NOT NULL,
    criado_em            TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em        TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (processo_id, candidato_usuario_id)
);

CREATE TABLE vaga_salva
(
    id         UUID PRIMARY KEY,
    vaga_id    UUID        NOT NULL REFERENCES vaga (id) ON DELETE CASCADE,
    usuario_id UUID        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    salva_em   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (vaga_id, usuario_id)
);

CREATE TABLE convites_recrutador
(
    id             UUID PRIMARY KEY,
    organizacao_id UUID         NOT NULL,
    email          VARCHAR(255) NOT NULL,
    token          VARCHAR(255) NOT NULL UNIQUE,
    status         VARCHAR(20)  NOT NULL,
    data_envio     TIMESTAMP    NOT NULL,
    data_expiracao TIMESTAMP    NOT NULL,
    data_aceite    TIMESTAMP,

    CONSTRAINT fk_convite_organizacao
        FOREIGN KEY (organizacao_id)
            REFERENCES organizacao (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_convite_status
        CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'EXPIRADO'))
);

CREATE TABLE convites_processo_seletivo
(
    id                    UUID PRIMARY KEY,
    vaga_id               UUID        NOT NULL,
    recrutador_usuario_id UUID        NOT NULL,
    candidato_usuario_id  UUID        NOT NULL,
    mensagem              TEXT,
    status                VARCHAR(20) NOT NULL,
    data_envio            TIMESTAMP   NOT NULL,
    data_expiracao        TIMESTAMP   NOT NULL,
    data_resposta         TIMESTAMP,

    CONSTRAINT fk_convite_vaga FOREIGN KEY (vaga_id) REFERENCES vaga (id) ON DELETE CASCADE,
    CONSTRAINT fk_convite_recrutador FOREIGN KEY (recrutador_usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_convite_candidato FOREIGN KEY (candidato_usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT chk_convite_status CHECK (status IN ('PENDENTE', 'ACEITO', 'RECUSADO', 'EXPIRADO'))
);

CREATE TABLE IF NOT EXISTS compatibilidade_cache
(
    id                         UUID PRIMARY KEY,
    candidato_usuario_id       UUID          NOT NULL,
    vaga_id                    UUID          NOT NULL,
    percentual_compatibilidade DECIMAL(5, 2) NOT NULL CHECK (percentual_compatibilidade >= 0 AND percentual_compatibilidade <= 100),
    justificativa              TEXT,
    data_calculo               TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao           TIMESTAMP,

    CONSTRAINT fk_cache_candidato FOREIGN KEY (candidato_usuario_id)
        REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_cache_vaga FOREIGN KEY (vaga_id)
        REFERENCES vaga (id) ON DELETE CASCADE,

    CONSTRAINT uk_cache_candidato_vaga UNIQUE (candidato_usuario_id, vaga_id)
);

CREATE TABLE reset_senha
(
    id               UUID PRIMARY KEY,
    usuario_id       UUID         NOT NULL,
    token            VARCHAR(255) NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL,
    data_solicitacao TIMESTAMP    NOT NULL,
    data_expiracao   TIMESTAMP    NOT NULL,
    data_uso         TIMESTAMP,

    CONSTRAINT fk_reset_usuario
        FOREIGN KEY (usuario_id)
            REFERENCES usuario (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_reset_status
        CHECK (status IN ('PENDENTE', 'USADO', 'EXPIRADO'))
);

CREATE INDEX idx_membro_usuario ON membro_organizacao (usuario_id);
CREATE INDEX idx_vaga_org ON vaga (organizacao_id);
CREATE INDEX idx_vaga_recrutador ON vaga (recrutador_usuario_id);
CREATE INDEX idx_cand_por_vaga ON candidatura (vaga_id);
CREATE INDEX idx_cand_por_usuario ON candidatura (candidato_usuario_id);
CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
CREATE INDEX idx_refresh_token_token ON refresh_token (token);
CREATE INDEX idx_candidatura_status ON candidatura (status);
CREATE INDEX idx_candidatura_data ON candidatura (data_candidatura);
CREATE INDEX idx_processo_etapa_atual ON processo_seletivo (etapa_processo_atual_id);
CREATE INDEX idx_vaga_status ON vaga (status);
CREATE INDEX idx_historico_processo ON historico_etapa_processo (processo_id);
CREATE INDEX idx_historico_data ON historico_etapa_processo (data_mudanca);
CREATE INDEX idx_avaliacao_org ON avaliacao_organizacao (organizacao_id);
CREATE INDEX idx_avaliacao_candidato ON avaliacao_organizacao (candidato_usuario_id);
CREATE INDEX idx_avaliacao_nota ON avaliacao_organizacao (nota);
CREATE INDEX idx_avaliacao_org_nota ON avaliacao_organizacao (organizacao_id, nota);
CREATE INDEX idx_vaga_salva_usuario ON vaga_salva (usuario_id);
CREATE INDEX idx_vaga_salva_vaga ON vaga_salva (vaga_id);
CREATE INDEX idx_vaga_salva_data ON vaga_salva (salva_em);
CREATE INDEX idx_convite_token ON convites_recrutador (token);
CREATE INDEX idx_convite_email ON convites_recrutador (email);
CREATE INDEX idx_convite_organizacao ON convites_recrutador (organizacao_id);
CREATE INDEX idx_convite_candidato ON convites_processo_seletivo (candidato_usuario_id);
CREATE INDEX idx_convite_vaga ON convites_processo_seletivo (vaga_id);
CREATE INDEX idx_convite_status ON convites_processo_seletivo (status);
CREATE INDEX idx_convite_candidato_status ON convites_processo_seletivo (candidato_usuario_id, status);
CREATE INDEX idx_etapa_vaga ON etapas_processo (vaga_id, ordem);
CREATE INDEX idx_etapa_status ON etapas_processo (status);
CREATE INDEX idx_convite_recrutador ON convites_processo_seletivo (recrutador_usuario_id);
CREATE UNIQUE INDEX idx_convite_unico_pendente
    ON convites_processo_seletivo (vaga_id, candidato_usuario_id)
    WHERE status = 'PENDENTE';

CREATE INDEX idx_cache_candidato ON compatibilidade_cache (candidato_usuario_id);
CREATE INDEX idx_cache_vaga ON compatibilidade_cache (vaga_id);
CREATE INDEX idx_cache_data_calculo ON compatibilidade_cache (data_calculo);
CREATE INDEX idx_reset_senha_token ON reset_senha (token);
CREATE INDEX idx_reset_senha_usuario_status ON reset_senha (usuario_id, status);
