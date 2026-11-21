CREATE DATABASE IF NOT EXISTS estoque CHARACTER SET utf8mb4;
USE estoque;

CREATE TABLE IF NOT EXISTS usuario (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    login        VARCHAR(50)  NOT NULL UNIQUE,
    senha        CHAR(64)     NOT NULL,
    data_criacao DATETIME     NOT NULL,
    tipo         CHAR(1)      NOT NULL DEFAULT 'U',
    ativo        CHAR(1)      NOT NULL DEFAULT 'T',
    email        VARCHAR(100) NOT NULL DEFAULT ''
);

CREATE TABLE IF NOT EXISTS produto (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    nome           VARCHAR(100)  NOT NULL UNIQUE,
    marca          VARCHAR(100),
    preco          DECIMAL(10,2) NOT NULL,
    tipo           CHAR(1)       NOT NULL DEFAULT 'N',
    quantidade     INT           NOT NULL DEFAULT 0,
    estoque_minimo INT           NOT NULL DEFAULT 0,
    CONSTRAINT chk_produto_tipo CHECK (tipo IN ('P', 'N')),
    CONSTRAINT chk_produto_qtd  CHECK (quantidade >= 0 AND estoque_minimo >= 0)
);

CREATE TABLE IF NOT EXISTS lote (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    produto_id INT         NOT NULL,
    numero     VARCHAR(30) NOT NULL,
    validade   DATE        NOT NULL,
    quantidade INT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_lote_produto FOREIGN KEY (produto_id)
        REFERENCES produto (id) ON DELETE CASCADE,
    CONSTRAINT uq_lote UNIQUE (produto_id, numero),
    CONSTRAINT chk_lote_qtd CHECK (quantidade >= 0)
);

INSERT INTO usuario (login, senha, data_criacao, tipo, ativo, email)
VALUES ('admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        NOW(), 'A', 'T', 'admin@estoque.com');

INSERT INTO produto (nome, marca, preco, tipo, quantidade, estoque_minimo) VALUES
    ('Leite Integral 1L', 'Italac',  5.49, 'P', 10, 5),
    ('Arroz 5kg',         'Tio JoÃ£o', 28.90, 'N', 40, 10);

INSERT INTO lote (produto_id, numero, validade, quantidade)
SELECT id, 'L1', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 5 FROM produto WHERE nome = 'Leite Integral 1L';
INSERT INTO lote (produto_id, numero, validade, quantidade)
SELECT id, 'L2', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 5 FROM produto WHERE nome = 'Leite Integral 1L';
