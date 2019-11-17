/**
 * 
 */
package com.abm.mainet.socialsecurity.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeMaster;
import com.abm.mainet.socialsecurity.ui.dto.PensionEligibilityCriteriaDto;

/**
 * @author satish.rathore
 *
 */
@Repository
public interface PensionDetailMasterRepository extends PagingAndSortingRepository<SocialSecuritySchemeMaster, Long> {
    @Modifying
    @Query("delete from SocialSecuritySchemeEligibilty sss where sss.orgId=:orgId and sss.groupID in :deletedBatchIdSet")
    void deleteSchemeDetialsByBatchId(@Param("orgId") Long orgId, @Param("deletedBatchIdSet") Set<Long> deletedBatchIdSet);

    @Query("select count(ssm) from SocialSecuritySchemeMaster ssm where ssm.schemeNameId=:serviceId and ssm.orgId=:orgId and ssm.isSchmeActive='Y'")
    int findServiceId(@Param("serviceId") Long serviceId, @Param("orgId") Long orgId);
    
    @Query("select count(ssa) from SocialSecuritySchemeEligibilty ssa where ssa.criteriaId=:criteriaId and ssa.orgId=:orgId")
    List<PensionEligibilityCriteriaDto> findEligibilityData(@Param("criteriaId") Long criteriaId,@Param("orgId") Long orgId);
    
    
    @Query("select count(ssm) from SocialSecuritySchemeEligibilty ssm where ssm.factorApplicableId=:factorApplicableId and ssm.criteriaId=:criteriaId and ssm.rangeFrom=:rangeFrom and ssm.rangeTo=:rangeTo and ssm.amount=:amt and ssm.orgId=:orgId")
    int findfactorApplicable(@Param("factorApplicableId") Long factorApplicableId,@Param("criteriaId") Long criteriaId,@Param("rangeFrom") String rangeFrom, @Param("rangeTo") String rangeTo,
                @Param("amt") BigDecimal amt,@Param("orgId") Long orgId);
    
    @Query("select count(ssm) from SocialSecuritySchemeEligibilty ssm where ssm.factorApplicableId=:factorApplicableId and ssm.criteriaId=:criteriaId and ssm.rangeFrom=:rangeFrom and ssm.rangeTo=:rangeTo and ssm.orgId=:orgId")
    int findfactorApplicablewithoutamt(@Param("factorApplicableId") Long factorApplicableId,@Param("criteriaId") Long criteriaId,@Param("rangeFrom") String rangeFrom, @Param("rangeTo") String rangeTo,
                @Param("orgId") Long orgId);
     
    @Modifying
    @Query("update SocialSecuritySchemeMaster ssa set ssa.isSchmeActive='N' where ssa.schemeMstId=:schmeMsId and ssa.orgId=:orgId")
    void inactiveScheme(@Param("schmeMsId")Long schmeMsId,@Param("orgId") Long orgId);
}
