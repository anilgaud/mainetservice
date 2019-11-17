package com.abm.mainet.socialsecurity.ui.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PensionSchemeMasterDto implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3337806617014632572L;
    /* factor applicable */
    private Long schmeMsId;
    private Long serviceId;
    private String objOfschme;
    private Long orgId;
    private Date createdDate;
    private Long createdBy;
    private Long updatedBy;
    private Date updatedDate;
    private String lgIpMac;
    private String lgIpMacUpd;
    private String isSchmeActive;
    public boolean checkBox;
    private List<PensionEligibilityCriteriaDto> pensioneligibilityList = new ArrayList<>();
    private List<PensionSourceOfFundDto> pensionSourceFundList = new ArrayList<>();
    private List<List<PensionEligibilityCriteriaDto>> saveDataList = new ArrayList<>();

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getObjOfschme() {
        return objOfschme;
    }

    public void setObjOfschme(String objOfschme) {
        this.objOfschme = objOfschme;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getLgIpMac() {
        return lgIpMac;
    }

    public void setLgIpMac(String lgIpMac) {
        this.lgIpMac = lgIpMac;
    }

    public String getLgIpMacUpd() {
        return lgIpMacUpd;
    }

    public void setLgIpMacUpd(String lgIpMacUpd) {
        this.lgIpMacUpd = lgIpMacUpd;
    }

    public List<List<PensionEligibilityCriteriaDto>> getSaveDataList() {
        return saveDataList;
    }

    public void setSaveDataList(List<List<PensionEligibilityCriteriaDto>> saveDataList) {
        this.saveDataList = saveDataList;
    }

    public String getIsSchmeActive() {
        return isSchmeActive;
    }

    public void setIsSchmeActive(String isSchmeActive) {
        this.isSchmeActive = isSchmeActive;
    }

    public List<PensionEligibilityCriteriaDto> getPensioneligibilityList() {
        return pensioneligibilityList;
    }

    public void setPensioneligibilityList(List<PensionEligibilityCriteriaDto> pensioneligibilityList) {
        this.pensioneligibilityList = pensioneligibilityList;
    }

    public List<PensionSourceOfFundDto> getPensionSourceFundList() {
        return pensionSourceFundList;
    }

    public void setPensionSourceFundList(List<PensionSourceOfFundDto> pensionSourceFundList) {
        this.pensionSourceFundList = pensionSourceFundList;
    }

    public Long getSchmeMsId() {
        return schmeMsId;
    }

    public void setSchmeMsId(Long schmeMsId) {
        this.schmeMsId = schmeMsId;
    }

    public boolean isCheckBox() {
        return checkBox;
    }

    public void setCheckBox(boolean checkBox) {
        this.checkBox = checkBox;
    }

}
