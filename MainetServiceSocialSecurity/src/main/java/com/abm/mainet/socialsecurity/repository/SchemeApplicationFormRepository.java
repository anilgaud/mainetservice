/**
 * 
 */
package com.abm.mainet.socialsecurity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.abm.mainet.socialsecurity.domain.SocialSecurityApplicationForm;

/**
 * @author satish.rathore
 *
 */

@Repository
public interface SchemeApplicationFormRepository extends PagingAndSortingRepository<SocialSecurityApplicationForm, Long> {

    @Query("from SocialSecurityApplicationForm ssa where ssa.apmApplicationId=:applicationId and ssa.orgId=:orgId")
    SocialSecurityApplicationForm findApplicationdetails(@Param("applicationId") String applicationId,
            @Param("orgId") Long orgId);

    @Modifying
    @Query("update SocialSecurityApplicationForm ssa set ssa.sapiStatus=:status where ssa.apmApplicationId=:applicationId and ssa.orgId=:parentOrgId")
    void updateApprovalFlag(@Param("applicationId") String applicationId, @Param("parentOrgId") Long parentOrgId,
            @Param("status") String status);

    @Modifying
    @Transactional
    @Query("update SocialSecurityApplicationForm ssa set ssa.lastDateofLifeCerti=:lastDateofLifeCerti where ssa.beneficiarynumber=:beneficiarynumber")
    void updateLastDateofLifeCerti(@Param("beneficiarynumber") String beneficiarynumber,
            @Param("lastDateofLifeCerti") Date lastDateofLifeCerti);

    @Query("from SocialSecurityApplicationForm ssa where ssa.apmApplicationId=:applicationId and ssa.beneficiarynumber=:beneficiarynumber and ssa.orgId=:orgId")
    SocialSecurityApplicationForm findDatabyBenef(@Param("applicationId") String applicationId,
            @Param("beneficiarynumber") String beneficiarynumber, @Param("orgId") Long orgId);

    @Query("from SocialSecurityApplicationForm ssa where ssa.beneficiarynumber=:beneficiarynumber and ssa.orgId=:orgId")
    SocialSecurityApplicationForm fetchDataOnBenef(@Param("beneficiarynumber") String beneficiarynumber,
            @Param("orgId") Long orgId);

    @Modifying
    @Transactional
    @Query("update SocialSecurityApplicationForm ssa set ssa.validtoDate=:validtoDate where ssa.beneficiarynumber=:beneficiarynumber")
    void updateValidtoDate(@Param("beneficiarynumber") String beneficiarynumber, @Param("validtoDate") Date validtoDate);

    @Query("from SocialSecurityApplicationForm ssa where ssa.apmApplicationId=:applicationId and ssa.orgId=:orgId")
    SocialSecurityApplicationForm fetchAllData(@Param("applicationId") String applicationId, @Param("orgId") Long orgId);

    @Query("from CFCAttachment ssa where ssa.applicationId=:applicationId and ssa.orgid=:orgid")
    SocialSecurityApplicationForm getUploadedfile(@Param("applicationId") Long applicationId, @Param("orgid") Long orgid);

    @Query("from SocialSecurityApplicationForm ssa where ssa.orgId=:orgId ")
    List<SocialSecurityApplicationForm> fetchAlldata(@Param("orgId") Long orgId);
    
    @Query("from SocialSecurityApplicationForm ssa where ssa.beneficiarynumber=:beneficiarynumber and ssa.orgId=:orgId")
    SocialSecurityApplicationForm findPensionDetails(@Param("beneficiarynumber") String beneficiarynumber, @Param("orgId") Long orgId);
    
    @Modifying
    @Query("update SocialSecurityApplicationForm ssa set ssa.sapiStatus='C', ssa.pensionCancelReason=:pensionCancelReason, ssa.pensionCancelDate=:pensionCancelDate where ssa.beneficiarynumber=:beneficiarynumber")
    void updateClosePension( @Param("pensionCancelReason") String pensionCancelReason,
            @Param("pensionCancelDate") Date pensionCancelDate, @Param("beneficiarynumber") String beneficiarynumber);
    
    @Modifying
    @Query("update SocialSecurityApplicationForm ssa set ssa.sapiStatus='R' where ssa.apmApplicationId=:applicationId and ssa.orgId=:orgId")
    void rejectPension(@Param("applicationId") String applicationId,@Param("orgId") Long orgId);
}
