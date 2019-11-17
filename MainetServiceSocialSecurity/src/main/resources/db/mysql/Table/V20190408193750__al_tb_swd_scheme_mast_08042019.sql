--liquibase formatted sql
--changeset nilima:V20190408193750__al_tb_swd_scheme_mast_08042019.sql
CREATE TABLE tb_swd_scheme_mast (
  SDSCH_ID BIGINT(12) NOT NULL COMMENT 'Primary key',
  SDSCH_SER_ID BIGINT(12) NOT NULL COMMENT 'Scheme Name (from service master)',
  SDSCH_OBJ VARCHAR(250) NOT NULL COMMENT 'Objective of Scheme',
  SDSCH_ACTIVE CHAR(1) NOT NULL,
  ORGID BIGINT(12) NOT NULL,
  CREATED_BY BIGINT(12) NOT NULL,
  CREATED_DATE DATE NOT NULL,
  UPDATED_BY BIGINT(12) NULL,
  UPDATED_DATE DATETIME NULL,
  LG_IP_MAC VARCHAR(100) NOT NULL,
  LG_IP_MAC_UPD VARCHAR(100) NULL,
  PRIMARY KEY (SDSCH_ID))
COMMENT = 'Scheme Master';

--liquibase formatted sql
--changeset nilima:V20190408193750__al_tb_swd_scheme_mast_080420191.sql
CREATE TABLE tb_swd_scheme_mast_hist (
  SDSCH_HIST_ID BIGINT(12) NOT NULL COMMENT 'Primary key',
  SDSCH_ID BIGINT(12)  COMMENT 'Primary key',
  SDSCH_SER_ID BIGINT(12)  COMMENT 'Scheme Name (from service master)',
  SDSCH_OBJ VARCHAR(250)  COMMENT 'Objective of Scheme',
  SDSCH_ACTIVE CHAR(1) ,
  ORGID BIGINT(12) ,
  CREATED_BY BIGINT(12) ,
  CREATED_DATE DATE ,
  UPDATED_BY BIGINT(12) NULL,
  UPDATED_DATE DATETIME NULL,
  LG_IP_MAC VARCHAR(100) ,
  LG_IP_MAC_UPD VARCHAR(100) NULL,
  PRIMARY KEY (SDSCH_ID))
COMMENT = 'Scheme Master Hist';
