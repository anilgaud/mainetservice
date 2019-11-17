package com.abm.mainet.socialsecurity.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.MainetConstants.AccountConstants;
import com.abm.mainet.common.constant.MainetConstants.BUG_HEAD_OPENING_BALANCE_MASTER;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.constant.ServiceEndpoints;
import com.abm.mainet.common.domain.BankMasterEntity;
import com.abm.mainet.common.domain.Department;
import com.abm.mainet.common.domain.Employee;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.domain.ServiceMaster;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.integration.acccount.dto.VendorBillApprovalDTO;
import com.abm.mainet.common.integration.acccount.dto.VendorBillExpDetailDTO;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.master.dto.TbDepartment;
import com.abm.mainet.common.master.dto.TbLocationMas;
import com.abm.mainet.common.master.dto.TbTaxMas;
import com.abm.mainet.common.master.service.BankMasterService;
import com.abm.mainet.common.master.service.DepartmentService;
import com.abm.mainet.common.master.service.TbAcVendormasterService;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.master.service.TbTaxMasService;
import com.abm.mainet.common.service.CommonService;
import com.abm.mainet.common.service.ILocationMasService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.RestClient;
import com.abm.mainet.common.utility.SeqGenFunctionUtility;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.common.workflow.dto.ApplicationMetadata;
import com.abm.mainet.common.workflow.dto.WorkflowTaskAction;
import com.abm.mainet.common.workflow.service.IWorkFlowTypeService;
import com.abm.mainet.common.workflow.service.IWorkflowActionService;
import com.abm.mainet.socialsecurity.domain.BeneficiaryPaymentDetailEntity;
import com.abm.mainet.socialsecurity.mapper.BeneficiaryPaymentDetMapper;
import com.abm.mainet.socialsecurity.repository.IBeneficiaryPaymentOrderRepository;
import com.abm.mainet.socialsecurity.ui.dto.BeneficiaryPaymentOrderDto;

@Service
public class BeneficiaryPaymentOrderServiceImpl implements IBeneficiaryPaymentOrderService {

    @Autowired
    IBeneficiaryPaymentOrderRepository beneficiaryPaymentOrderRepository;
    @Autowired
    private BankMasterService bankMasterService;
    @Autowired
    private ILocationMasService locMasService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ServiceMasterService iServiceMasterService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private SeqGenFunctionUtility seqGenFunctionUtility;
    @Autowired
    private IWorkflowActionService workflowActionService;
    @Autowired
    private IWorkFlowTypeService iWorkFlowTypeService;
    @Autowired
    private IFileUploadService fileUploadService;
    @Autowired
    private TbDepartmentService deptService;
    @Autowired
    private TbTaxMasService taxMasService;
    @Autowired
    TbAcVendormasterService tbAcVendormasterService;

    private static final Logger logger = Logger.getLogger(BeneficiaryPaymentOrderServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public List<BeneficiaryPaymentOrderDto> filterSearchData(final Long serviceId, final Long paymentscheId, final Long year,
            final Long month,
            final Long orgId) {

        final List<BeneficiaryPaymentOrderDto> benefiDetailList = new ArrayList<>();
        try {
            final List<Object[]> beneficiaryList = beneficiaryPaymentOrderRepository.filterSearchDatas(serviceId, paymentscheId,
                    orgId);
            if (beneficiaryList != null && !beneficiaryList.isEmpty()) {
                beneficiaryList.stream().forEach(k -> {
                    BeneficiaryPaymentOrderDto dto = new BeneficiaryPaymentOrderDto();
                    if (k[0] != null) {
                        dto.setBeneficiaryName(k[0].toString());
                    }
                    if (k[8] != null) {
                        dto.setBeneficiaryNumber(k[8].toString());
                    }
                    if (k[3] != null) {
                        dto.setAccountNumber(Long.parseLong(k[3].toString()));
                    }
                    if (k[4] != null) {
                        dto.setAmount((BigDecimal) k[4]);
                    }
                    if (k[2] != null) {
                        Long m = Long.parseLong(k[2].toString());// I need to create m as a variable because it can't allow to use
                                                                 // k[2] direct
                        final List<BankMasterEntity> bankdetails = bankMasterService.getBankList().stream()
                                .filter(l -> l.getBankId().equals(m))
                                .collect(Collectors.toList());
                        dto.setBankName(bankdetails.get(0).getBank());
                        // check
                        dto.setIfscCode(bankdetails.get(0).getIfsc());
                        // check
                        dto.setBranchName(bankdetails.get(0).getBranch());
                        dto.setBankId(bankdetails.get(0).getBankId());

                    }
                    if (k[5] != null) {
                        // this is serviceId use as a schemeId
                        dto.setSchemeId(Long.parseLong(k[5].toString()));
                    }
                    if (k[6] != null) {
                        dto.setPaymentScheduleId(Long.parseLong(k[6].toString()));
                    }
                    if (k[7] != null) {
                        // this is of application master number
                        dto.setApplicationNumber(k[7].toString());
                    }
                    benefiDetailList.add(dto);
                });
            }
        } catch (Exception ex) {
            throw new FrameworkException(
                    "While fetching details error occured please check", ex);
        }
        return benefiDetailList;
    }

    @Override
    @Transactional
    public void accountBillEntryforSocialSecurity(BeneficiaryPaymentOrderDto dto, Organisation org) {
        try {
            ResponseEntity<?> responseEntity = null;
            VendorBillApprovalDTO approvalDTO = new VendorBillApprovalDTO();
            List<VendorBillExpDetailDTO> expDetListDto = new ArrayList<>();
            VendorBillExpDetailDTO billExpDetailDTO = new VendorBillExpDetailDTO();
            approvalDTO.setBillEntryDate(Utility.dateToString(new Date()));
            approvalDTO
                    .setBillTypeId(CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix(MainetConstants.SocialSecurity.MISCELLANEOUS,
                            MainetConstants.ABT, dto.getOrgId()));
            approvalDTO.setOrgId(dto.getOrgId());
            if (dto.getAccPostBatchInd().equalsIgnoreCase("INV")) {
                approvalDTO
                        .setNarration(
                                dto.getWorkOrderNumber() + "/" + dto.getWorkOrdrerDate() + "/" + dto.getBeneficiaryName() + "/"
                                        + dto.getBeneficiaryNumber());
            } else {
                approvalDTO.setNarration(dto.getWorkOrderNumber() + "/" + dto.getWorkOrdrerDate());
            }
            approvalDTO.setCreatedBy(dto.getEmpId());
            approvalDTO.setCreatedDate(Utility.dateToString(new Date()));
            approvalDTO.setLgIpMacAddress(dto.getIpAddress());
            approvalDTO.setInvoiceAmount(dto.getAmount());
            // code for getting vendor sub type
            Long vendorSubtypeId = CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix(MainetConstants.SocialSecurity.PENSION,
                    MainetConstants.SocialSecurity.VST,
                    dto.getOrgId());
            // getting vendor status
            final Long vendorStatus = CommonMasterUtility
                    .getLookUpFromPrefixLookUpValue(AccountConstants.AC.getValue(), PrefixConstants.VSS,
                            dto.getLangId().intValue(), org)
                    .getLookUpId();
            // get vendorId by using vendor sub type id
            final Long vendorId = tbAcVendormasterService.getAllActiveVendors(dto.getOrgId(), vendorStatus).stream()
                    .filter(s -> s.getCpdVendorSubType().equals(vendorSubtypeId)).collect(Collectors.toList()).get(0)
                    .getVmVendorid();
            approvalDTO.setVendorId(vendorId);
            final Department dept = deptService.findDepartmentByCode(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
            // find the tax code for the for the pension
            Long tdpPrefixIdPSN = CommonMasterUtility.lookUpIdByLookUpCodeAndPrefix(
                    MainetConstants.SocialSecurity.TAX_PREFIX, MainetConstants.SocialSecurity.TAX_MASTER_PREFIX_TXN,
                    dto.getOrgId());
            List<TbTaxMas> txMasListPSN = taxMasService.findAllByDescId(tdpPrefixIdPSN,
                    dto.getOrgId(), dept.getDpDeptid());
            Long accountCodePSNId = taxMasService.fetchSacHeadIdForReceiptDet(dto.getOrgId(), txMasListPSN.get(0).getTaxId(),
                    "A");
            billExpDetailDTO.setBudgetCodeId(accountCodePSNId);
            billExpDetailDTO.setSanctionedAmount(dto.getAmount());
            billExpDetailDTO.setAmount(dto.getAmount());
            expDetListDto.add(billExpDetailDTO);
            approvalDTO.setExpDetListDto(expDetListDto);
            long fieldId = 0;
            if (dto.getLocId() != null) {
                final TbLocationMas locMas = locMasService.findById(dto.getLocId());
                if ((locMas.getLocRevenueWZMappingDto() != null) && !locMas.getLocRevenueWZMappingDto().isEmpty()) {
                    fieldId = locMas.getLocRevenueWZMappingDto().get(0).getCodIdRevLevel1();
                }
            }
            if (fieldId == 0) {
                throw new NullPointerException("fieldId is not linked with Location Master for[locId="
                        + dto.getLocId());
            }
            approvalDTO.setFieldId(fieldId);
            approvalDTO.setDepartmentId(dept.getDpDeptid());

            try {
                responseEntity = RestClient.callRestTemplateClient(approvalDTO, ServiceEndpoints.SALARY_POSTING);
                if (responseEntity != null && responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                    beneficiaryPaymentOrderRepository.updateAccountStatusAndBillNumber(responseEntity.getBody().toString(),
                            dto.getOrgId(), dto.getRtgsTransId(), "Y");

                }
            } catch (Exception exception) {
                throw new FrameworkException("error occured while bill posting to account module ", exception);
            }

        } catch (Exception e) {
            throw new FrameworkException("error occured validating all data required by account posting  ", e);
        }

    }

    @Override
    @Transactional
    public void saveBeneficiaryDetails(List<BeneficiaryPaymentOrderDto> dtoList) {
        List<BeneficiaryPaymentDetailEntity> list = new ArrayList<>();
        if (dtoList != null && !dtoList.isEmpty()) {
            Long squenceNo = seqGenFunctionUtility.generateSequenceNo(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE,
                    "TB_SWD_RTGS_PAYMENT", "WORK_ORDER_NUMBER", dtoList.get(0).getOrgId(), MainetConstants.FlagC, null);
            dtoList.stream().parallel().forEachOrdered(k -> {
                k.setWorkOrderNumber(squenceNo);
                k.setWorkOrdrerDate(new Date());
                k.setApplicationNumber(squenceNo.toString());
                list.add(BeneficiaryPaymentDetMapper.dtoToEntity(k));
                k.getApplicationNumber();
            });
            beneficiaryPaymentOrderRepository.save(list);
        }
    }

    @Override
    @Transactional
    public void initiateWorkFlowForFreeService(BeneficiaryPaymentOrderDto dto) {
        TbDepartment deptObj = departmentService.findDeptByCode(dto.getOrgId(), MainetConstants.FlagA,
                MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
        ServiceMaster sm = iServiceMasterService
                .getServiceMasterByShortCode(MainetConstants.SocialSecurity.BENEFICIARY_SERVICE_CODE, dto.getOrgId());
        ApplicationMetadata applicationData = new ApplicationMetadata();
        applicationData.setIsCheckListApplicable(false);
        applicationData.setOrgId(dto.getOrgId());
        applicationData
                .setReferenceId(dto.getWorkOrderNumber().toString());
        dto.getApplicant().setServiceId(sm.getSmServiceId());
        dto.getApplicant().setDepartmentId(deptObj.getDpDeptid());
        dto.getApplicant().setUserId(dto.getEmpId());
        dto.getApplicant().setOrgId(dto.getOrgId());
        commonService.initiateWorkflowfreeService(applicationData, dto.getApplicant());
    }

    @Override
    @Transactional(readOnly = true)
    public BeneficiaryPaymentOrderDto getViewDataFromRtgsPayment(Long orgId, String applicationId) {

        BeneficiaryPaymentOrderDto parentDto = new BeneficiaryPaymentOrderDto();
        List<BeneficiaryPaymentDetailEntity> entity = beneficiaryPaymentOrderRepository.getViewDataFromRtgsPayment(orgId,
                Long.parseLong(applicationId));
        entity.stream().parallel().forEachOrdered(k -> {
            final List<BankMasterEntity> bankdetails = bankMasterService.getBankList().stream()
                    .filter(l -> l.getBankId().equals(k.getBankId()))
                    .collect(Collectors.toList());
            BeneficiaryPaymentOrderDto dto = BeneficiaryPaymentDetMapper.entityToDto(k);
            dto.setBankName(bankdetails.get(0).getBank());
            parentDto.getDtoList().add(dto);
        });
        parentDto.setSchemeId(parentDto.getDtoList().get(0).getSchemeId());
        parentDto.setPaymentScheduleId(parentDto.getDtoList().get(0).getPaymentScheduleId());
        parentDto.setRemark(parentDto.getDtoList().get(0).getRemark());
        return parentDto;
    }

    @Override
    @Transactional
    public boolean saveDecision(BeneficiaryPaymentOrderDto bpoDto, Long orgId, Employee emp, WorkflowTaskAction workFlowActionDto,
            RequestDTO requestDTO, Organisation org) {
        boolean status = false;
        try {
            workflowActionService.updateWorkFlow(workFlowActionDto, emp, orgId, requestDTO.getServiceId());
            // for saving the documents
            if ((bpoDto.getDocumentList() != null) && !bpoDto.getDocumentList().isEmpty()) {
                 fileUploadService.doFileUpload(bpoDto.getDocumentList(), requestDTO); 
                
            }
            
            
            // if user is last who reject or approve according to that we can update our own table flag
            if (iWorkFlowTypeService.isLastTaskInCheckerTaskList(workFlowActionDto.getTaskId())) {
                beneficiaryPaymentOrderRepository.updateApprovalFlag(Long.parseLong(workFlowActionDto.getReferenceId()), orgId,
                        workFlowActionDto.getDecision().substring(0, 1));

            }
            status = true;
        } catch (Exception ex) {
            throw new FrameworkException(
                    "exception occurs while updating workflow,upload docs,updating table", ex);
        }
        return status;
    }

    @Override
    public Boolean checkAccountActiveOrNot() {
        boolean accountCodeStatus = false;
        LookUp defaultVal = CommonMasterUtility.getDefaultValue(BUG_HEAD_OPENING_BALANCE_MASTER.SLI_PREFIX_VALUE);
        if (defaultVal != null) {
            String accountCode = defaultVal.getLookUpCode();
            if (accountCode.equals(MainetConstants.FlagL)) {
                accountCodeStatus = true;
            }
        }
        return accountCodeStatus;
    }

    // this check is for workflow is not define if it is true then only it gives true
    @Override
    public Boolean checkWorkflowIsNotDefine(Organisation org, Long orgId, String serviceCode) {
        boolean status = false;
        try {
            ServiceMaster sm = iServiceMasterService.getServiceMasterByShortCode(serviceCode, orgId);
            final List<LookUp> bptPrefixList = CommonMasterUtility.getListLookup(PrefixConstants.LookUp.BPT, org);
            List<LookUp> lookUp = bptPrefixList.stream().filter(s -> Long.valueOf(s.getLookUpId()).equals(sm.getSmProcessId()))
                    .collect(Collectors.toList());
            if (lookUp.get(0).getLookUpCode().trim().equals("NA")) {
                status = true;
            }
        } catch (Exception ex) {
            throw new FrameworkException(
                    "exception occurs while saving beneficiary form data", ex);
        }
        return status;
    }
 
    
 
}
