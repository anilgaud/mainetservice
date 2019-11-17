/**
 * 
 */
package com.abm.mainet.socialsecurity.ui.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import com.abm.mainet.common.ui.model.AbstractFormModel;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.socialsecurity.service.IPensionSchemeMasterService;
import com.abm.mainet.socialsecurity.ui.dto.PensionEligibilityCriteriaDto;
import com.abm.mainet.socialsecurity.ui.dto.PensionSchemeMasterDto;
import com.abm.mainet.socialsecurity.ui.dto.ViewDtoList;

/**
 * @author satish.rathore
 *
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class PensionSchemeMasterModel extends AbstractFormModel {

    @Autowired
    private IPensionSchemeMasterService pensionSchemeMasterService;

    private static final Logger LOGGER = Logger.getLogger(PensionSchemeMasterModel.class);
    /**
     * 
     */
    private static final long serialVersionUID = 4886278503425835102L;
    private PensionSchemeMasterDto pensionSchmDto = new PensionSchemeMasterDto();
    private List<LookUp> sourceLookUps = new ArrayList<>(30);
    private List<LookUp> secondLevellookUps = new ArrayList<>();
    private List<Object[]> serviceList = new ArrayList<>();
    private List<LookUp> sponcerByList = new ArrayList<>();
    private List<LookUp> paymentList = new ArrayList<>();
    private List<List<PensionEligibilityCriteriaDto>> saveDataList = new ArrayList<>();
    private List<ViewDtoList> viewList = new ArrayList<>();
    private String modeType;
    private Set<Long> deletedBatchIdSet = new HashSet<>();
    private Set<Long> updateBatchIdSet = new HashSet<>();

    private PensionEligibilityCriteriaDto pensionEligCriteriaDto = new PensionEligibilityCriteriaDto();

    public PensionSchemeMasterDto getPensionSchmDto() {
        return pensionSchmDto;
    }

    public void setPensionSchmDto(PensionSchemeMasterDto pensionSchmDto) {
        this.pensionSchmDto = pensionSchmDto;
    }

    public List<LookUp> getSourceLookUps() {

        return sourceLookUps;
    }

    public void setSourceLookUps(List<LookUp> sourceLookUps) {

        this.sourceLookUps = sourceLookUps;
    }

    public List<LookUp> getSecondLevellookUps() {
        return secondLevellookUps;
    }

    public void setSecondLevellookUps(List<LookUp> secondLevellookUps) {

        this.secondLevellookUps = secondLevellookUps;
    }

    public List<Object[]> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Object[]> serviceList) {
        this.serviceList = serviceList;
    }

    public List<LookUp> getSponcerByList() {
        return sponcerByList;
    }

    public void setSponcerByList(List<LookUp> sponcerByList) {
        this.sponcerByList = sponcerByList;
    }

    public List<LookUp> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<LookUp> paymentList) {
        this.paymentList = paymentList;
    }

    public List<List<PensionEligibilityCriteriaDto>> getSaveDataList() {
        return saveDataList;
    }

    public void setSaveDataList(List<List<PensionEligibilityCriteriaDto>> saveDataList) {
        this.saveDataList = saveDataList;
    }

    /**
     * @return the viewList
     */
    public List<ViewDtoList> getViewList() {
        return viewList;
    }

    /**
     * @param viewList the viewList to set
     */
    public void setViewList(List<ViewDtoList> viewList) {
        this.viewList = viewList;
    }

    public String getModeType() {
        return modeType;
    }

    public void setModeType(String modeType) {
        this.modeType = modeType;
    }

    public Set<Long> getDeletedBatchIdSet() {
        return deletedBatchIdSet;
    }

    public void setDeletedBatchIdSet(Set<Long> deletedBatchIdSet) {
        this.deletedBatchIdSet = deletedBatchIdSet;
    }

    public Set<Long> getUpdateBatchIdSet() {
        return updateBatchIdSet;
    }

    public void setUpdateBatchIdSet(Set<Long> updateBatchIdSet) {
        this.updateBatchIdSet = updateBatchIdSet;
    }

    public PensionEligibilityCriteriaDto getPensionEligCriteriaDto() {
        return pensionEligCriteriaDto;
    }

    public void setPensionEligCriteriaDto(PensionEligibilityCriteriaDto pensionEligCriteriaDto) {
        this.pensionEligCriteriaDto = pensionEligCriteriaDto;
    }

    public PensionSchemeMasterDto getOneDetails(Long id, Long orgId, String modeType) {
        PensionSchemeMasterDto dto = pensionSchemeMasterService.getOneDetails(id, orgId, modeType);
        List<PensionEligibilityCriteriaDto> eleCriteriaDtoList = dto.getPensioneligibilityList();

        Long distct = eleCriteriaDtoList.stream().distinct().count();
        int j = 0;
        for (int i = 0; i < distct; i++) {
            Long batchId = eleCriteriaDtoList.get(j).getBatchId();

            getSaveDataList().add(eleCriteriaDtoList.stream()
                    .filter(k -> k.getBatchId().equals(batchId)).collect(Collectors.toList()));
            j = j + (int) eleCriteriaDtoList.stream().filter(l -> l.getBatchId().equals(batchId)).count();

        }
        setPensionSchmDto(dto);
        return dto;

    }

}
