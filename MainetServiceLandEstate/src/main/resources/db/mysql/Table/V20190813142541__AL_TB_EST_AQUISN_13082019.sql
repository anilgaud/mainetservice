--liquibase formatted sql
--changeset Anil:V20190813142541__AL_TB_EST_AQUISN_13082019.sql
rename table TB_LN_AQUISN to TB_EST_AQUISN;
--liquibase formatted sql
--changeset Anil:V20190813142541__AL_TB_EST_AQUISN_130820191.sql
ALTER TABLE tb_est_aquisn 
CHANGE COLUMN DEPT_APPL_ID APM_APPLICATION_ID BIGINT(16) NOT NULL COMMENT 'Service ID',
ADD COLUMN TRANSFER_STATUS CHAR(1) NULL AFTER LG_IP_MAC_UPD;