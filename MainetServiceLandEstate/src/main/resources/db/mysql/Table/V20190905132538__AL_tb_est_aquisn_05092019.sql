--liquibase formatted sql
--changeset Anil:V20190905132538__AL_tb_est_aquisn_05092019.sql
ALTER TABLE tb_est_aquisn 
CHANGE COLUMN BM_ID LN_BILLNO VARCHAR(40) NULL DEFAULT NULL ,
ADD COLUMN LN_MOBNO BIGINT(10) NULL AFTER ASSET_ID,
ADD COLUMN LN_ADDRESS VARCHAR(500) NOT NULL AFTER LN_MOBNO;
