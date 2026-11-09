-- NexRain - apaga as tabelas para recriar o banco do zero
-- Rodar como script (DBeaver: Alt+X | SQL Developer: F5) e depois rodar o script.sql.
-- Se alguma tabela ainda nao existir vai aparecer ORA-00942: pode clicar em "Ignorar todos".

DROP TABLE T_FIAP_ROTA_NR CASCADE CONSTRAINTS;

DROP TABLE T_FIAP_ALERTA_NR CASCADE CONSTRAINTS;

DROP TABLE T_FIAP_OCORRENCIA_NR CASCADE CONSTRAINTS;

DROP TABLE T_FIAP_CAMERA_NR CASCADE CONSTRAINTS;

DROP TABLE T_FIAP_REGIAO_NR CASCADE CONSTRAINTS;
