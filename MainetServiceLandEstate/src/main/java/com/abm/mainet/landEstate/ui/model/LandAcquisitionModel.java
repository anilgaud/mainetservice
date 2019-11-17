package com.abm.mainet.landEstate.ui.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.abm.mainet.cfc.checklist.service.IChecklistVerificationService;
import com.abm.mainet.cfc.scrutiny.dto.ScrutinyLableValueDTO;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.MainetConstants.AccountConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.constant.ServiceEndpoints;
import com.abm.mainet.common.domain.BankMasterEntity;
import com.abm.mainet.common.domain.Employee;
import com.abm.mainet.common.domain.ServiceMaster;
import com.abm.mainet.common.domain.TbTaxMasEntity;
import com.abm.mainet.common.dto.ApplicantDetailDTO;
import com.abm.mainet.common.dto.TbAcVendormaster;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.integration.acccount.dto.VendorBillApprovalDTO;
import com.abm.mainet.common.integration.acccount.dto.VendorBillExpDetailDTO;
import com.abm.mainet.common.integration.asset.dto.AssetClassificationDTO;
import com.abm.mainet.common.integration.asset.dto.AssetDetailsDTO;
import com.abm.mainet.common.integration.asset.dto.AssetInformationDTO;
import com.abm.mainet.common.integration.asset.dto.AssetPurchaseInformationDTO;
import com.abm.mainet.common.integration.dms.domain.AttachDocs;
import com.abm.mainet.common.integration.dms.service.IAttachDocsService;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.integration.dto.DocumentDetailsVO;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.master.dto.TbFinancialyear;
import com.abm.mainet.common.master.dto.TbLocationMas;
import com.abm.mainet.common.master.service.DepartmentService;
import com.abm.mainet.common.master.service.TbAcVendormasterService;
import com.abm.mainet.common.master.service.TbTaxMasService;
import com.abm.mainet.common.service.ILocationMasService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.ui.model.AbstractFormModel;
import com.abm.mainet.common.utility.ApplicationContextProvider;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.RestClient;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.common.workflow.dto.ApplicationMetadata;
import com.abm.mainet.landEstate.dto.LandAcquisitionDto;
import com.abm.mainet.landEstate.service.ILandAcquisitionService;
import com.abm.mainet.landEstate.ui.validator.LandAcquisitionFormValidator;

@Component
@Scope("session")
public class LandAcquisitionModel extends AbstractFormModel {

    private static final long serialVersionUID = -4831181353774295892L;
    private LandAcquisitionDto acquisitionDto = null;
    private List<LandAcquisitionDto> landAcquisitionList = null;
    private List<TbLocationMas> locationList = null;
    List<TbAcVendormaster> vendorList = null;
    private List<DocumentDetailsVO> checkList = null;
    private boolean isDocumentSubmitted;
    private List<DocumentDetailsVO> checkListForPreview = new ArrayList<>();
    private List<AttachDocs> attachDocsList = new ArrayList<>();
    private ServiceMaster serviceMaster = new ServiceMaster();
    private ScrutinyLableValueDTO lableValueDTO = new ScrutinyLableValueDTO();
    private List<BankMasterEntity> bankList = null;
    private List<TbFinancialyear> financialYearList = new ArrayList<>();
    private String hasError = MainetConstants.BLANK;
    private Long serviceId;
    private Long orgId;
    private Long userId;
    private String saveMode;
    private Long deptId;
    private Long langId;
    private Boolean isChecklistApplicable = false;
    private Boolean accountIntegrateBT = false; // this is for dynamically hide submit button from UI

    private List<String> proposalNoList = new ArrayList<String>();

    public List<String> getProposalNoList() {
        return proposalNoList;
    }

    public void setProposalNoList(List<String> proposalNoList) {
        this.proposalNoList = proposalNoList;
    }

    public List<BankMasterEntity> getBankList() {
        return bankList;
    }

    public void setBankList(List<BankMasterEntity> bankList) {
        this.bankList = bankList;
    }

    public List<TbFinancialyear> getFinancialYearList() {
        return financialYearList;
    }

    public void setFinancialYearList(List<TbFinancialyear> financialYearList) {
        this.financialYearList = financialYearList;
    }

    public LandAcquisitionDto getAcquisitionDto() {
        return acquisitionDto;
    }

    public void setAcquisitionDto(LandAcquisitionDto acquisitionDto) {
        this.acquisitionDto = acquisitionDto;
    }

    public List<LandAcquisitionDto> getLandAcquisitionList() {
        return landAcquisitionList;
    }

    public void setLandAcquisitionList(List<LandAcquisitionDto> landAcquisitionList) {
        this.landAcquisitionList = landAcquisitionList;
    }

    public List<TbLocationMas> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<TbLocationMas> locationList) {
        this.locationList = locationList;
    }

    public List<TbAcVendormaster> getVendorList() {
        return vendorList;
    }

    public void setVendorList(List<TbAcVendormaster> vendorList) {
        this.vendorList = vendorList;
    }

    public List<DocumentDetailsVO> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<DocumentDetailsVO> checkList) {
        this.checkList = checkList;
    }

    public boolean isDocumentSubmitted() {
        return isDocumentSubmitted;
    }

    public void setDocumentSubmitted(boolean isDocumentSubmitted) {
        this.isDocumentSubmitted = isDocumentSubmitted;
    }

    public List<DocumentDetailsVO> getCheckListForPreview() {
        return checkListForPreview;
    }

    public void setCheckListForPreview(List<DocumentDetailsVO> checkListForPreview) {
        this.checkListForPreview = checkListForPreview;
    }

    public ServiceMaster getServiceMaster() {
        return serviceMaster;
    }

    public void setServiceMaster(ServiceMaster serviceMaster) {
        this.serviceMaster = serviceMaster;
    }

    public ScrutinyLableValueDTO getLableValueDTO() {
        return lableValueDTO;
    }

    public void setLableValueDTO(ScrutinyLableValueDTO lableValueDTO) {
        this.lableValueDTO = lableValueDTO;
    }

    public String getHasError() {
        return hasError;
    }

    public void setHasError(String hasError) {
        this.hasError = hasError;
    }

    public List<AttachDocs> getAttachDocsList() {
        return attachDocsList;
    }

    public void setAttachDocsList(List<AttachDocs> attachDocsList) {
        this.attachDocsList = attachDocsList;
    }

    public IChecklistVerificationService getiChecklistVerificationService() {
        return iChecklistVerificationService;
    }

    public void setiChecklistVerificationService(IChecklistVerificationService iChecklistVerificationService) {
        this.iChecklistVerificationService = iChecklistVerificationService;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSaveMode() {
        return saveMode;
    }

    public void setSaveMode(String saveMode) {
        this.saveMode = saveMode;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getLangId() {
        return langId;
    }

    public void setLangId(Long langId) {
        this.langId = langId;
    }

    public Boolean getIsChecklistApplicable() {
        return isChecklistApplicable;
    }

    public void setIsChecklistApplicable(Boolean isChecklistApplicable) {
        this.isChecklistApplicable = isChecklistApplicable;
    }

    public Boolean getAccountIntegrateBT() {
        return accountIntegrateBT;
    }

    public void setAccountIntegrateBT(Boolean accountIntegrateBT) {
        this.accountIntegrateBT = accountIntegrateBT;
    }

    @Autowired
    private IChecklistVerificationService iChecklistVerificationService;

    @Autowired
    ILandAcquisitionService acquisitionService;

    @Override
    public boolean saveForm() {
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        try {
            // Server Side validation
            validateBean(acquisitionDto, LandAcquisitionFormValidator.class);
            if (hasValidationErrors()) {
                return false;
            }
            setCheckList(ApplicationContextProvider.getApplicationContext().getBean(IFileUploadService.class)
                    .setFileUploadMethod(new ArrayList<DocumentDetailsVO>()));

            RequestDTO requestDTO = new RequestDTO();
            requestDTO.setOrgId(orgId);
            requestDTO.setStatus(MainetConstants.FlagA);
            requestDTO.setDepartmentName(MainetConstants.LandEstate.LandEstateCode);
            requestDTO.setServiceId(getServiceId());
            requestDTO.setDeptId(getDeptId());
            requestDTO.setLangId(getLangId());
            requestDTO.setUserId(getUserId());

            // setting applicant info
            requestDTO.setfName(acquisitionDto.getPayTo());
            requestDTO.setMobileNo(acquisitionDto.getLnMobNo() != null ? String.valueOf(acquisitionDto.getLnMobNo()) : null);
            requestDTO.setAreaName(acquisitionDto.getLnAddress());

            // getAcquisitionDto().setApmApplicationId(appNo);
            getAcquisitionDto().setCreatedBy(UserSession.getCurrent().getEmployee().getEmpId());
            getAcquisitionDto().setLgIpMac(UserSession.getCurrent().getEmployee().getEmppiservername());
            getAcquisitionDto().setOrgId(orgId);
            /* getAcquisitionDto().setLangId(getLangId()); */
            getAcquisitionDto().setCreatedDate(new Date());
            getAcquisitionDto().setAcqStatus("T");
            getAcquisitionDto().setTransferStatus("N");

            // check for duplicate
            int duplicateData = acquisitionService.checkDuplicateLand(acquisitionDto.getLnServno(),
                    acquisitionDto.getLnArea(), acquisitionDto.getLocId(), acquisitionDto.getPayTo());
            if (duplicateData > 0) {
                addValidationError(ApplicationSession.getInstance().getMessage("Details Already Exists"));
                return false;
            } else {

                // save in TB_LN_AQUISN table
                acquisitionDto = acquisitionService.saveLandAcquisition(acquisitionDto, getServiceMaster(), getCheckList(),
                        requestDTO);

                // code for workflow process

                // check charge applicable at application level
                Long chargeApplicableAt = CommonMasterUtility
                        .getValueFromPrefixLookUp(MainetConstants.NewWaterServiceConstants.APL,
                                MainetConstants.NewWaterServiceConstants.CAA, UserSession.getCurrent().getOrganisation())
                        .getLookUpId();

                LookUp lookUp = CommonMasterUtility.getNonHierarchicalLookUpObject(
                        chargeApplicableAt, UserSession.getCurrent().getOrganisation());
                if (serviceMaster.getSmFeesSchedule().equals(1l)
                        && ((serviceMaster.getSmAppliChargeFlag().equals(MainetConstants.Common_Constant.YES))
                                && lookUp.getLookUpCode().equalsIgnoreCase(MainetConstants.ChargeApplicableAt.APPLICATION))
                        || ((serviceMaster.getSmScrutinyChargeFlag().equals(MainetConstants.Common_Constant.YES))
                                && lookUp.getLookUpCode().equalsIgnoreCase(MainetConstants.ChargeApplicableAt.SCRUTINY))) {
                    // code write for Taxes based on rate if required

                    // till time not initiate workflow here because charges not know
                    // and if service master change like charges at application time than write code here

                } else {
                    // Application Time is Free service so call below method
                    ApplicationMetadata applicationData = new ApplicationMetadata();
                    applicationData.setApplicationId(acquisitionDto.getApmApplicationId());
                    applicationData.setIsCheckListApplicable(this.getIsChecklistApplicable());
                    applicationData.setOrgId(requestDTO.getOrgId());
                    if (serviceMaster.getSmFeesSchedule().longValue() == 0) {
                        applicationData.setIsLoiApplicable(false);
                    } else {
                        applicationData.setIsLoiApplicable(true);
                    }

                    ApplicantDetailDTO applicantDto = new ApplicantDetailDTO();
                    applicantDto.setServiceId(requestDTO.getServiceId());
                    applicantDto.setOrgId(requestDTO.getOrgId());
                    applicantDto.setDepartmentId(requestDTO.getDeptId());
                    applicantDto.setUserId(requestDTO.getUserId());

                    // pass ApplicationMetadata OBJ and applicant dto
                    acquisitionService.initiateWorkflowForFreeService(applicationData, applicantDto);

                }
                setSuccessMessage(getAppSession().getMessage("land.acq.success",
                        new Object[] { acquisitionDto.getApmApplicationId().toString() }));
                return true;
            }
        } catch (Exception e) {
            throw new FrameworkException("error occured while saving land proposal ", e);
        }
    }

    @Override
    public void populateApplicationData(long applicationId) {

        this.setAcquisitionDto(acquisitionService.getLandAcqDataByApplicationId(applicationId,
                UserSession.getCurrent().getOrganisation().getOrgid()));
        List<TbLocationMas> locationList = ApplicationContextProvider.getApplicationContext().getBean(ILocationMasService.class)
                .fillAllLocationMasterDetails(UserSession.getCurrent().getOrganisation());
        this.setLocationList(locationList);

        final List<AttachDocs> attachDocs = ApplicationContextProvider.getApplicationContext()
                .getBean(IAttachDocsService.class).findByCode(UserSession.getCurrent().getOrganisation().getOrgid(),
                        "LAQ" + MainetConstants.WINDOWS_SLASH + getAcquisitionDto().getLnaqId());
        this.setAttachDocsList(attachDocs);
        // fetch vendor list from vendor master
        final Long vendorStatus = CommonMasterUtility
                .getLookUpFromPrefixLookUpValue(AccountConstants.AC.getValue(), PrefixConstants.VSS,
                        UserSession.getCurrent().getLanguageId(), UserSession.getCurrent().getOrganisation())
                .getLookUpId();
        List<TbAcVendormaster> vendorList = ApplicationContextProvider.getApplicationContext()
                .getBean(TbAcVendormasterService.class)
                .getAllActiveVendors(UserSession.getCurrent().getOrganisation().getOrgid(), vendorStatus);
        this.setVendorList(vendorList);

    }

    public void accountIntegrate(String loiNo) {
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long empId = UserSession.getCurrent().getEmployee().getEmpId();
        VendorBillApprovalDTO billDTO = new VendorBillApprovalDTO();
        List<VendorBillExpDetailDTO> billExpDetListDto = new ArrayList<>();
        VendorBillExpDetailDTO billExpDetailDTO = new VendorBillExpDetailDTO();

        billDTO.setBillEntryDate(Utility.dateToString(new Date()));
        billDTO.setBillTypeId(CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix("MI",
                MainetConstants.ABT, orgId));
        // get LOI no for set in NARRATION field
        billDTO.setOrgId(orgId);
        billDTO.setNarration(loiNo + "-" + acquisitionDto.getAcqPurpose());
        billDTO.setCreatedBy(empId);
        billDTO.setCreatedDate(Utility.dateToString(new Date()));
        billDTO.setLgIpMacAddress(UserSession.getCurrent().getEmployee().getEmppiservername());
        billDTO.setVendorId(acquisitionDto.getVendorId());
        billDTO.setInvoiceAmount(acquisitionDto.getAcqCost());
        billDTO.setFieldId(acquisitionDto.getLocId());
        billDTO.setDepartmentId(ApplicationContextProvider.getApplicationContext().getBean(DepartmentService.class)
                .getDepartment(MainetConstants.LandEstate.LandEstateCode,
                        MainetConstants.CommonConstants.ACTIVE)
                .getDpDeptid());

        final LookUp chargeApplicableAt = CommonMasterUtility.getValueFromPrefixLookUp(
                MainetConstants.ChargeApplicableAt.SCRUTINY,
                PrefixConstants.LookUp.CHARGE_MASTER_CAA, UserSession.getCurrent().getOrganisation());

        ServiceMaster sm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
                .getServiceMasterByShortCode(
                        MainetConstants.LandEstate.LandAcquisition.SERVICE_SHOT_CODE,
                        orgId);

        // ask to sir below code is correct or not
        final List<TbTaxMasEntity> taxesMasters = ApplicationContextProvider.getApplicationContext()
                .getBean(TbTaxMasService.class)
                .fetchAllApplicableServiceCharge(serviceId, orgId, chargeApplicableAt.getLookUpId());
        Long sacHeadId = null;
        for (TbTaxMasEntity tax : taxesMasters) {
            Long taxId = tax.getTaxId();
            sacHeadId = ApplicationContextProvider.getApplicationContext().getBean(TbTaxMasService.class)
                    .fetchSacHeadIdForReceiptDet(orgId, taxId, "A"); // here A means Active
            if (sacHeadId != null) {
                break;
            }
        }
        billExpDetailDTO.setBudgetCodeId(sacHeadId);
        billExpDetailDTO.setAmount(acquisitionDto.getAcqCost());
        billExpDetailDTO.setSanctionedAmount(acquisitionDto.getAcqCost());
        billExpDetListDto.add(billExpDetailDTO);

        billDTO.setExpDetListDto(billExpDetListDto);

        try {
            ResponseEntity<?> responseEntity = RestClient.callRestTemplateClient(billDTO, ServiceEndpoints.SALARY_POSTING);
            if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                // update billNo in TB_EST_AQUISN
                String billNo = responseEntity.getBody().toString();
                acquisitionService.updateLandValuationData(acquisitionDto.getApmApplicationId(), acquisitionDto.getAcqCost(),
                        null, billNo, null, null, orgId);

            }
        } catch (Exception exception) {
            throw new FrameworkException("error occured while bill posting to account module ", exception);
        }

    }

    public String assetIntegrate(Long apmApplicationId, Long lnaqId) {
        String assetintegrateMSG = MainetConstants.SUCCESS_MSG;
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Employee emp = UserSession.getCurrent().getEmployee();
        LandAcquisitionDto acquisitionDto = acquisitionService.getLandAcqDataByApplicationId(apmApplicationId, orgId);
        // check here if AMT and vendorId is null than show error MSG
        if (acquisitionDto.getAcqCost() == null || acquisitionDto.getVendorId() == null) {
            assetintegrateMSG = "1st complete scrutiny process for integrate Asset";
        }
        AssetDetailsDTO assetDetailsDTO = new AssetDetailsDTO();
        assetDetailsDTO.setOrgId(orgId);
        AssetInformationDTO assetInformationDTO = new AssetInformationDTO();
        assetInformationDTO.setAssetName(acquisitionDto.getAcqPurpose());
        assetInformationDTO.setDetails(acquisitionDto.getLnDesc());
        assetInformationDTO.setAcquisitionMethod(acquisitionDto.getAcqModeId());
        // PREFIX IMO and description for code L is Land
        assetInformationDTO.setAssetClass1((Long) CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix("L", "IMO", orgId));
        // PREFIX ASC and description for code IMO is immovable
        assetInformationDTO.setAssetClass2((Long) CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix("IMO", "ASC", orgId));
        assetInformationDTO.setPurpose(acquisitionDto.getAcqPurpose());
        // PREFIX ACN and description for code A is Active
        assetInformationDTO.setAssetStatus((Long) CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix("A", "ACN", orgId));
        assetInformationDTO.setAcquisitionMethod(acquisitionDto.getAcqModeId());
        assetInformationDTO.setOrgId(orgId);
        assetInformationDTO.setCreatedBy(emp.getEmpId());
        assetInformationDTO.setCreationDate(new Date());

        AssetPurchaseInformationDTO purchaseInformationDTO = new AssetPurchaseInformationDTO();
        purchaseInformationDTO.setDateOfAcquisition(acquisitionDto.getAcqDate());

        purchaseInformationDTO.setCostOfAcquisition(acquisitionDto.getAcqCost());
        purchaseInformationDTO.setFromWhomAcquired(acquisitionDto.getVendorId());

        AssetClassificationDTO classificationDTO = new AssetClassificationDTO();
        classificationDTO.setDepartment(ApplicationContextProvider.getApplicationContext().getBean(DepartmentService.class)
                .getDepartment(MainetConstants.LandEstate.LandEstateCode,
                        MainetConstants.CommonConstants.ACTIVE)
                .getDpDeptid());

        assetDetailsDTO.setAssetInformationDTO(assetInformationDTO);
        assetDetailsDTO.setAssetPurchaseInformationDTO(purchaseInformationDTO);
        assetDetailsDTO.setAssetClassificationDTO(classificationDTO);
        // push in asset
        Long assetId = acquisitionService.pushAssetDetails(assetDetailsDTO);
        if (assetId != null) {
            // update assetId AND transferStatus = Y in TB_EST_AQUISN
            acquisitionService.updateLandValuationData(acquisitionDto.getApmApplicationId(), null,
                    null, null, assetId, "Y", orgId);
        } else {
            return "something went wrong";
        }
        return assetintegrateMSG;
    }

}
