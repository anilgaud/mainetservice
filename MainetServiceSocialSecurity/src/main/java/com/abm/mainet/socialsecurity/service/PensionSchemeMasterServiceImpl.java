/**
 * 
 */
package com.abm.mainet.socialsecurity.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abm.mainet.common.audit.service.AuditService;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeDetailsHistory;
import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeEligibilty;
import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeEligibiltyHistory;
import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeMaster;
import com.abm.mainet.socialsecurity.domain.SocialSecuritySchemeMasterHistory;
import com.abm.mainet.socialsecurity.mapper.PensionDetailsMapper;
import com.abm.mainet.socialsecurity.repository.PensionDetailMasterRepository;
import com.abm.mainet.socialsecurity.ui.dto.PensionEligibilityCriteriaDto;
import com.abm.mainet.socialsecurity.ui.dto.PensionSchemeMasterDto;
import com.abm.mainet.socialsecurity.ui.dto.ViewDtoList;

/**
 * @author satish.rathore
 *
 */
@Service
public class PensionSchemeMasterServiceImpl implements IPensionSchemeMasterService {

    @Autowired
    private PensionDetailMasterRepository pensionDetailMasterRepository;
    @Autowired
    private ServiceMasterService serviceMasterService;
    @Autowired
    private AuditService auditService;

    private static final Logger LOGGER = Logger.getLogger(PensionSchemeMasterServiceImpl.class);

    @Transactional
    @Override
    public boolean savePensionDetails(Long orgId, Long empId, String macAddress,
            PensionSchemeMasterDto pensionSchemeMasterDto) {
        SocialSecuritySchemeMaster entiy;
        boolean statusFlag = false;
        try {
            pensionSchemeMasterDto.setOrgId(orgId);
            pensionSchemeMasterDto.setCreatedBy(empId);
            pensionSchemeMasterDto.setLgIpMac(macAddress);
            pensionSchemeMasterDto.setCreatedDate(new Date());
            
            
            entiy = PensionDetailsMapper.DtoToEntity(pensionSchemeMasterDto);
            pensionDetailMasterRepository.save(entiy);
            entityToHistoryDetails(entiy);
            statusFlag = true;
        } catch (Exception ex) {
            throw new FrameworkException(
                    "exception occured while saving the scheme master so please check all mandatory fields", ex);
        }
        return statusFlag;

    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewDtoList> getAllData(Long orgId, Long depId, Long status) {
        List<ViewDtoList> ViewDtoList = new ArrayList<>();
        try {
            List<SocialSecuritySchemeMaster> DtoList = (List<SocialSecuritySchemeMaster>) pensionDetailMasterRepository
                    .findAll();
            List<Object[]> serviceList = serviceMasterService.findAllActiveServicesByDepartment(orgId, depId, status);
            DtoList.stream().forEach(k -> {
                serviceList.stream().forEach(l -> {
                    if (k.getSchemeNameId().equals(l[0])) {
                        ViewDtoList dto = new ViewDtoList();
                        dto.setSchemeCode(l[2].toString());
                        dto.setSchemeName(l[1].toString());
                        dto.setId(k.getSchemeMstId());
                        dto.setOrgId(k.getOrgId());
                        dto.setServiceId((Long) l[0]);
                        ViewDtoList.add(dto);
                    }
                });
            });
        } catch (Exception ex) {
            throw new FrameworkException("data is not found or service is not define please check it again", ex);
        }

        return ViewDtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public PensionSchemeMasterDto getOneDetails(Long id, Long orgId, String modeType) {
        PensionSchemeMasterDto dto = new PensionSchemeMasterDto();
        try {
            SocialSecuritySchemeMaster entity = pensionDetailMasterRepository.findOne(id);
            Hibernate.initialize(entity.getSocialSecuritySchemeDetList());
            Hibernate.initialize(entity.getSocialSecuritySchemeEligibilty());
            dto = PensionDetailsMapper.entityToDto(entity);

        } catch (Exception ex) {
            throw new FrameworkException("data is not found or service is not define please check it again", ex);
        }

        return dto;
    }

    @Override
    public List<LookUp> filterCriteria(List<LookUp> lookupList, List<PensionEligibilityCriteriaDto> criteriaDto) {
        final List<LookUp> sourceLookUp = new ArrayList<>();
        criteriaDto.stream().forEach(k -> {
            lookupList.stream().forEach(l -> {
                if (k.getFactorApplicableId().equals(l.getLookUpId())) {
                    LookUp lookup = new LookUp();
                    lookup.setLookUpId(l.getLookUpId());
                    lookup.setLookUpDesc(l.getLookUpDesc());
                    lookup.setDescLangFirst(l.getDescLangFirst());
                    lookup.setDescLangSecond(l.getDescLangSecond());
                    lookup.setLookUpParentId(l.getLookUpParentId());
                    lookup.setOtherField(l.getOtherField());
                    lookup.setLookUpType(l.getLookUpType());
                    lookup.setLookUpCode(l.getLookUpCode());
                    sourceLookUp.add(lookup);
                    
                    
                }
            });
        });
        
        return sourceLookUp;
    }

    @Override
    @Transactional
    public void updatePensionDetails(Long orgId, Long empId, String macAddress,
            PensionSchemeMasterDto pensionSchemeMasterDto, Set<Long> deletedBatchIdSet, Set<Long> updateBatchIdSet) {
        List<SocialSecuritySchemeEligibilty> eligibiltyList = new ArrayList<>();
        try {

            if (deletedBatchIdSet != null && !deletedBatchIdSet.isEmpty()) {
                pensionDetailMasterRepository.deleteSchemeDetialsByBatchId(orgId, deletedBatchIdSet);
            }
            pensionSchemeMasterDto.setOrgId(orgId);
            pensionSchemeMasterDto.setUpdatedBy(empId);
            pensionSchemeMasterDto.setLgIpMac(macAddress);
            pensionSchemeMasterDto.setUpdatedDate(new Date());
            // After selecting checkbox for inactive scheme flag is set to N 
            if(pensionSchemeMasterDto.checkBox==true && pensionSchemeMasterDto.getIsSchmeActive().equals("Y")) {
                pensionSchemeMasterDto.setIsSchmeActive(MainetConstants.FlagN);
                pensionSchemeMasterDto.setCheckBox(true);
            }
            else
            {
                pensionSchemeMasterDto.setIsSchmeActive(MainetConstants.FlagY);
            }
            if(pensionSchemeMasterDto.getIsSchmeActive()==null) {
                
                pensionSchemeMasterDto.setIsSchmeActive(MainetConstants.FlagY);
            }
            SocialSecuritySchemeMaster entiy = PensionDetailsMapper.DtoToEntity(pensionSchemeMasterDto);
           
            if (updateBatchIdSet != null) {
                eligibiltyList = entiy.getSocialSecuritySchemeEligibilty()
                        .stream().filter(k -> updateBatchIdSet.contains(k.getGroupID()))
                        .collect(Collectors.toList());
                
                entiy.setSocialSecuritySchemeEligibilty(eligibiltyList);
            }
            pensionDetailMasterRepository.save(entiy);
        } catch (Exception ex) {
            throw new FrameworkException(
                    "while updating and deleting data problem occurs", ex);
        }

    }

    @Override
    @Transactional
    public void entityToHistoryDetails(SocialSecuritySchemeMaster entity) {
        // create history of pension Datils
        SocialSecuritySchemeMasterHistory entityhis = new SocialSecuritySchemeMasterHistory();
        try {
            auditService.createHistory(entity, entityhis);
        } catch (Exception ex) {
            LOGGER.error("could not make entry for" + entity, ex);
        }
        entity.getSocialSecuritySchemeEligibilty().forEach(eligibEntity -> {
            SocialSecuritySchemeEligibiltyHistory historyEntity = new SocialSecuritySchemeEligibiltyHistory();
            try {
                historyEntity.setSchemeMstId(entity.getSchemeMstId());
                auditService.createHistory(eligibEntity, historyEntity);
            } catch (Exception ex) {
                LOGGER.error("could not make entry for" + eligibEntity, ex);
            }
        });
        entity.getSocialSecuritySchemeDetList().forEach(schDetEntity -> {
            SocialSecuritySchemeDetailsHistory historyEntity = new SocialSecuritySchemeDetailsHistory();
            try {
                historyEntity.setSchemeMstId(entity.getSchemeMstId());
                auditService.createHistory(schDetEntity, historyEntity);
            } catch (Exception ex) {
                LOGGER.error("could not make entry for" + schDetEntity, ex);
            }
        });
    }

    @Override
    public int findServiceId(Long serviceId, Long orgId) {
        return pensionDetailMasterRepository.findServiceId(serviceId, orgId);
    }

   
    
    @Override
    @Transactional
    public int findfactorApplicable(Long factorApplicableId,Long criteriaId,String rangeFrom,String rangeTo,BigDecimal amt, Long orgId) {
       
        return pensionDetailMasterRepository.findfactorApplicable(factorApplicableId,criteriaId,rangeFrom,rangeTo,amt, orgId);
    }
    
    @Override
    @Transactional
    public int findfactorApplicablewithoutamt(Long factorApplicableId,Long criteriaId,String rangeFrom,String rangeTo, Long orgId) {
       
        return pensionDetailMasterRepository.findfactorApplicablewithoutamt(factorApplicableId,criteriaId,rangeFrom,rangeTo, orgId);
    }

    @Override
    @Transactional
    public void inactiveScheme(Long schmeMsId, Long orgId) {
       
         pensionDetailMasterRepository.inactiveScheme(schmeMsId, orgId);
    }

    

    

}
