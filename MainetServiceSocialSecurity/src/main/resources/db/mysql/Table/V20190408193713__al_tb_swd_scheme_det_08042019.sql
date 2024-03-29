--liquibase formatted sql
--changeset nilima:V20190408193713__al_tb_swd_scheme_det_08042019.sql
CREATE TABLE tb_swd_scheme_det (
  SDSCHD_ID BIGINT(12) NOT NULL COMMENT 'Primary Key',
  SDSCH_ID BIGINT(12) NOT NULL COMMENT 'Foregin Key',
  SDSCH_SPONSORED_BY VARCHAR(250) NOT NULL COMMENT 'Sponser by',
  SDSCH_SHARING_PER DECIMAL(6,2) NOT NULL COMMENT 'Sharing Percentage',
  SDSCH_ACTIVE CHAR(1) NOT NULL COMMENT '(Active->Y,Inactive->N)',
  ORGID BIGINT(12) NOT NULL,
  CREATED_BY BIGINT(12) NOT NULL,
  CREATED_DATE DATETIME NOT NULL,
  UPDATED_BY BIGINT(12) NULL,
  UPDATED_DATE DATETIME NULL,
  LG_IP_MAC VARCHAR(100) NOT NULL,
  LG_IP_MAC_UPD VARCHAR(100) NULL,
  PRIMARY KEY (SDSCHD_ID))
COMMENT = 'scheme Detail';

--liquibase formatted sql
--changeset nilima:V20190408193713__al_tb_swd_scheme_det_080420191.sql
CREATE TABLE tb_swd_scheme_det_hist (
  SDSCHD_HIST_ID BIGINT(12) NOT NULL COMMENT 'Primary Key',
  SDSCHD_ID BIGINT(12) NOT NULL COMMENT 'Primary Key',
  SDSCH_ID BIGINT(12) NOT NULL COMMENT 'Foregin Key',
  SDSCH_SPONSORED_BY VARCHAR(250) NOT NULL COMMENT 'Sponser by',
  SDSCH_SHARING_PER DECIMAL(6,2) NOT NULL COMMENT 'Sharing Percentage',
  SDSCH_ACTIVE CHAR(1) NOT NULL COMMENT '(Active->Y,Inactive->N)',
  H_STATUS CHAR(1) COMMENT 'History status',
  ORGID BIGINT(12) NOT NULL,
  CREATED_BY BIGINT(12) NOT NULL,
  CREATED_DATE DATETIME NOT NULL,
  UPDATED_BY BIGINT(12) NULL,
  UPDATED_DATE DATETIME NULL,
  LG_IP_MAC VARCHAR(100) NOT NULL,
  LG_IP_MAC_UPD VARCHAR(100) NULL,
  PRIMARY KEY (SDSCHD_ID))
COMMENT = 'scheme Detail';
