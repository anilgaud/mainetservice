package com.abm.mainet.socialsecurity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.abm.mainet.socialsecurity.domain.BeneficiaryPaymentDetailEntity;

@Repository
public interface IBeneficiaryPaymentOrderRepository extends PagingAndSortingRepository<BeneficiaryPaymentDetailEntity, Long> {

    
      
    
    
    
    
    /*
     * @Query(value = "SELECT a.SAPI_NAME," + "a.SAPI_ID," + "a.SAPI_BANKID," + "a.SAPI_ACCOUNTID," + "c.SDSCHE_AMT," +
     * "b.SDSCH_SER_ID," + "c.SDSCHE_PAYSCH,\r\n" + "a.APM_APPLICATION_ID,\r\n" + "a.BENEFICIARY_NUMBER \r \n" +
     * "FROM TB_SWD_SCHEME_APPLICATION a, \r\n" + "tb_swd_scheme_mast b, \r \n" + "TB_SWD_SCHEME_ElIGIBILITY c \r \n" +
     * "WHERE  a.SDSCH_SER_ID = b.SDSCH_SER_ID and\r\n" + "b.SDSCH_ID = c.SDSCH_ID and\r\n" + "a.SDSCH_SER_ID =:serviceId and\r\n"
     * + "c.SDSCHE_PAYSCH =:paymentscheId and\r\n" + "a.ORGID=:orgId and\r\n" + "a.SAPI_STATUS = 'A'\r\n" +
     * "and a.APM_APPLICATION_ID NOT IN \r\n" + " (SELECT j.APM_APPLICATION_ID from tb_swd_rtgs_payment j)",nativeQuery = true)
     * List<Object[]>filterSearchDatas(@Param("serviceId") Long serviceId, @Param("paymentscheId") Long
     * paymentscheId,@Param("orgId") Long orgId);
     */
     
     
            @Query(value ="SELECT a.SAPI_NAME,\r\n" + 
                    "\r\n" + 
                    "      a.SAPI_ID,\r\n" + 
                    "\r\n" + 
                    "      a.SAPI_BANKID,\r\n" + 
                    "\r\n" + 
                    "      a.SAPI_ACCOUNTID,\r\n" + 
                    "\r\n" + 
                    "      x.SDSCHE_AMT,\r\n" + 
                    "\r\n" + 
                    "      b.SDSCH_SER_ID,\r\n" + 
                    "\r\n" + 
                    "      x.SDSCHE_PAYSCH,\r\n" + 
                    "\r\n" + 
                    "      a.APM_APPLICATION_ID,\r\n" + 
                    "\r\n" + 
                    "      a.BENEFICIARY_NUMBER \r\n" + 
                    "\r\n" + 
                    "      FROM TB_SWD_SCHEME_APPLICATION a,\r\n" + 
                    "\r\n" + 
                    "      tb_swd_scheme_mast b, \r\n" + 
                    "\r\n" + 
                    "      (select c.SDSCHE_AMT,c.SDSCHE_PAYSCH,c.SDSCH_ID\r\n" + 
                    "\r\n" + 
                    "      from TB_SWD_SCHEME_ElIGIBILITY c \r\n" + 
                    "\r\n" + 
                    "      where  c.SDSCHE_PAYSCH is not null and c.SDSCHE_AMT is not null\r\n" + 
                    "\r\n" + 
                    "      and c.SDSCHE_PAYSCH =(case when COALESCE(:paymentscheId,0)=0 then COALESCE(c.SDSCHE_PAYSCH,0) else COALESCE(:paymentscheId,0) end)) x\r\n" + 
                    "\r\n" + 
                    "      WHERE  a.SDSCH_SER_ID = b.SDSCH_SER_ID and\r\n" + 
                    "\r\n" + 
                    "      x.SDSCH_ID =b.SDSCH_ID and\r\n" + 
                    "\r\n" + 
                    "      a.SDSCH_SER_ID =(case when COALESCE(:serviceId,0)=0 then COALESCE(a.SDSCH_SER_ID,0) else COALESCE(:serviceId,0) end)  and\r\n" + 
                    "\r\n" + 
                    "      a.ORGID=:orgId and a.SAPI_STATUS = 'A'\r\n" + 
                    "\r\n" + 
                    "      and COALESCE(a.APM_APPLICATION_ID,0) NOT IN\r\n" + 
                    "\r\n" + 
                    "       (SELECT j.APM_APPLICATION_ID from tb_swd_rtgs_payment j)",nativeQuery = true) 
            List<Object[]>filterSearchDatas(@Param("serviceId") Long serviceId, @Param("paymentscheId") Long paymentscheId,@Param("orgId") Long orgId);
            

    @Query("from BeneficiaryPaymentDetailEntity bpe where bpe.orgId=:orgId and bpe.workOrderNumber=:applicationId")
    List<BeneficiaryPaymentDetailEntity> getViewDataFromRtgsPayment(@Param("orgId") Long orgId,
            @Param("applicationId") Long applicationId);

    @Modifying
    @Query("update BeneficiaryPaymentDetailEntity bpe set bpe.rtgsStatus=:status where bpe.workOrderNumber=:applicationId and bpe.orgId=:orgId")
    void updateApprovalFlag(@Param("applicationId") Long applicationId, @Param("orgId") Long orgId,
            @Param("status") String status);

    @Modifying
    @Query("update BeneficiaryPaymentDetailEntity bpe set bpe.rtgsPostst=:accStatus,bpe.rtgsBillno=:billNo  where bpe.rtgsTransId=:rtgsTransId and bpe.orgId=:orgId")
    void updateAccountStatusAndBillNumber(@Param("billNo") String billNo, @Param("orgId") Long orgId,
            @Param("rtgsTransId") Long rtgsTransId, @Param("accStatus") String accStatus);

}
