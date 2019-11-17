/**
 * 
 */
package com.abm.mainet.socialsecurity.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.domain.Employee;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.domain.ServiceMaster;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.master.dto.TbDepartment;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.service.ApplicationService;
import com.abm.mainet.common.service.CommonService;
import com.abm.mainet.common.service.IOrganisationService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.utility.ApplicationContextProvider;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.SeqGenFunctionUtility;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.workflow.dto.ApplicationMetadata;
import com.abm.mainet.common.workflow.dto.WorkflowRequest;
import com.abm.mainet.common.workflow.dto.WorkflowTaskAction;
import com.abm.mainet.common.workflow.service.IWorkFlowTypeService;
import com.abm.mainet.common.workflow.service.IWorkflowActionService;
import com.abm.mainet.common.workflow.service.IWorkflowRequestService;
import com.abm.mainet.smsemail.dto.SMSAndEmailDTO;
import com.abm.mainet.smsemail.service.ISMSAndEmailService;
import com.abm.mainet.socialsecurity.domain.SocialSecurityApplicationForm;
import com.abm.mainet.socialsecurity.mapper.ApplicationFormDetailsMapper;
import com.abm.mainet.socialsecurity.repository.SchemeApplicationFormRepository;
import com.abm.mainet.socialsecurity.ui.dto.ApplicationFormDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author priti.singh
 *
 */
@Produces({ "application/xml", "application/json" })
@WebService(endpointInterface = "com.abm.mainet.socialsecurity.service.ISchemeApplicationFormService")
@Api(value = "/schemeApplicationFormService")
@Path("/schemeApplicationFormService")
@Service(value = "SchemeApplicationFormService")
public class SchemeApplicationFormServiceImpl implements ISchemeApplicationFormService {

    private static final Logger logger = Logger.getLogger(SchemeApplicationFormServiceImpl.class);

    @Autowired
    private IOrganisationService organisationService;
    @Autowired
    private SchemeApplicationFormRepository schemeApplicationFormRepository;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private ServiceMasterService iServiceMasterService;
    @Autowired
    private TbDepartmentService iTbDepartmentService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private IFileUploadService fileUploadService;
    @Autowired
    private IWorkflowActionService iWorkflowActionService;
    @Autowired
    private IWorkFlowTypeService iWorkFlowTypeService;
    @Autowired
    private SeqGenFunctionUtility seqGenFunctionUtility;
    @Autowired
    private ISMSAndEmailService ismsAndEmailService;
    @Autowired
    private ServiceMasterService serviceMasterService;

    @Override
    @POST
    @WebMethod
    @Consumes("application/json")
    @Path("/FindSecondLevelPrefixByFirstLevelPxCode/{orgId}/{parentPx}/{parentpxId}/{level}")
    @Transactional(readOnly = true)
    public List<LookUp> FindSecondLevelPrefixByFirstLevelPxCode(@PathParam(value = "orgId") Long orgId,
            @PathParam(value = "parentPx") String parentPx, @PathParam(value = "parentpxId") Long parentpxId,
            @PathParam(value = "level") Long level) {
        List<LookUp> subType;
        try {
            subType = ApplicationSession.getInstance().getChildsByOrgPrefixTopParentLevel(orgId.intValue(),
                    parentPx, parentpxId, level);
        } catch (Exception ex) {
            logger.warn("Prefix Not Defined child ULB level:" + ex);
            subType = ApplicationSession.getInstance().getChildsByOrgPrefixTopParentLevel(
                    (int) organisationService.getSuperUserOrganisation().getOrgid(),
                    parentPx, parentpxId, level);
        }
        return subType;
    }

    @Override
    @POST
    @ApiOperation(value = MainetConstants.SocialSecurity.SAVE_UPDATE_SOCIAL_SECURITY_APPLICATION, notes = MainetConstants.SocialSecurity.SAVE_UPDATE_SOCIAL_SECURITY_APPLICATION)
    @Path("/saveSocialSecurityApplication")
    @Transactional

    public ApplicationFormDto saveApplicationDetails(ApplicationFormDto dto) {
        ApplicationFormDto newDto;
        try {
            dto.setBeneficiaryno(getbeneficiarynumber(dto.getUlbName(), dto.getServiceCode(), dto.getOrgId()));
            newDto = prepareAndSaveApplicationMaster(dto);
            SocialSecurityApplicationForm entity = ApplicationFormDetailsMapper.dtoToEntity(newDto);
            entity.setDatalegacyflag("S");
            entity.setSapiStatus("P");
            schemeApplicationFormRepository.save(entity);
            if ((dto.getDocumentList() != null) && !dto.getDocumentList().isEmpty()) {
                fileUploadService.doFileUpload(dto.getDocumentList(), prepareRequestDto(newDto));
            }
            initiateWorkFlowForFreeService(newDto);
            newDto.setStatusFlag(true);
        } catch (Exception ex) {
            throw new FrameworkException(
                    "Exception occured while saving the scheme application Details so please check all mandatory fields", ex);
        }

        return newDto;
    }

    @Override
    @Transactional
    public ApplicationFormDto prepareAndSaveApplicationMaster(ApplicationFormDto dto) {

        TbDepartment deptObj = iTbDepartmentService.findDeptByCode(dto.getOrgId(), MainetConstants.FlagA,
                MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
        ServiceMaster sm = iServiceMasterService
                .getServiceMasterByShortCode(MainetConstants.SocialSecurity.SERVICE_CODE, dto.getOrgId());

        final RequestDTO applicantDetailDTO = new RequestDTO();
        applicantDetailDTO.setfName(dto.getNameofApplicant());
        if(dto.getMobNum()!=null) {
        applicantDetailDTO.setMobileNo(dto.getMobNum().toString());
        }
        applicantDetailDTO.setEmail("");
        applicantDetailDTO.setPincodeNo(dto.getPinCode());
        applicantDetailDTO.setServiceId(sm.getSmServiceId());
        applicantDetailDTO.setDeptId(deptObj.getDpDeptid());
        applicantDetailDTO.setUserId(dto.getCreatedBy());
        applicantDetailDTO.setOrgId(dto.getOrgId());
        applicantDetailDTO.setLangId(dto.getLangId());
        applicantDetailDTO.setPayStatus(MainetConstants.FlagF);
        if (dto.getBplid().equals(MainetConstants.FlagY)) {
            applicantDetailDTO.setBplNo(dto.getBplfamily());
        }
        final Long applicationId = applicationService.createApplication(applicantDetailDTO);
        dto.setMasterAppId(applicationId);
        dto.setDeptId(deptObj.getDpDeptid());
        dto.setApplicableServiceId(sm.getSmServiceId());
        return dto;
    }

    @Override
    @Transactional
    public void initiateWorkFlowForFreeService(ApplicationFormDto dto) {
        boolean checklist = false;
        if ((dto.getDocumentList() != null) && !dto.getDocumentList().isEmpty()) {
            checklist = true;
        }
        ApplicationMetadata applicationData = new ApplicationMetadata();
        applicationData.setApplicationId(dto.getMasterAppId());
        applicationData.setIsCheckListApplicable(checklist);
        applicationData.setOrgId(dto.getOrgId());

        dto.getApplicant().setServiceId(dto.getApplicableServiceId());
        dto.getApplicant().setDepartmentId(dto.getDeptId());
        dto.getApplicant().setUserId(dto.getCreatedBy());
        dto.getApplicant().setMobileNo(dto.getMobNum().toString());
        dto.getApplicant().setOrgId(dto.getOrgId());
        commonService.initiateWorkflowfreeService(applicationData, dto.getApplicant());
    }

    // this code is for the document upload
    private RequestDTO prepareRequestDto(ApplicationFormDto dto) {

        final RequestDTO reqDto = new RequestDTO();
        reqDto.setfName(dto.getNameofApplicant());
        reqDto.setMobileNo(dto.getContactNumber().toString());
        reqDto.setPincodeNo(dto.getPinCode());
        reqDto.setServiceId(dto.getApplicableServiceId());
        reqDto.setDeptId(dto.getDeptId());
        reqDto.setUserId(dto.getCreatedBy());
        reqDto.setOrgId(dto.getOrgId());
        reqDto.setLangId(dto.getLangId());
        reqDto.setPayStatus(MainetConstants.FlagF);
        reqDto.setApplicationId(dto.getMasterAppId());
        reqDto.setIsBPL(dto.getIsBplApplicable());
        if (dto.getIsBplApplicable().equals(MainetConstants.FlagY)) {
            reqDto.setBplNo(dto.getBplfamily());
        }
        return reqDto;

    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationFormDto findApplicationdetails(Long applicationId, Long parentOrgId) {
        ApplicationFormDto dto = null;
        try {
            SocialSecurityApplicationForm entity = schemeApplicationFormRepository
                    .findApplicationdetails(applicationId.toString(), parentOrgId);
            dto = ApplicationFormDetailsMapper.entityToDto(entity);
        } catch (Exception ex) {
            throw new FrameworkException(
                    "Exception occurs while finding data from application form please check inputs", ex);
        }
        return dto;

    }

    @Override
    @Transactional
    public boolean saveDecision(ApplicationFormDto applicationformdto, Long orgId, Employee emp,
            WorkflowTaskAction workFlowActionDto, RequestDTO req) {
        boolean status = false;
        try {
            iWorkflowActionService.updateWorkFlow(workFlowActionDto, emp, orgId, req.getServiceId());
            // for saving the documents
            if ((applicationformdto.getDocumentList() != null) && !applicationformdto.getDocumentList().isEmpty()) {
                fileUploadService.doFileUpload(applicationformdto.getDocumentList(), req);
            }

            WorkflowRequest workflowRequest = ApplicationContextProvider.getApplicationContext()
                    .getBean(IWorkflowRequestService.class)
                    .getWorkflowRequestByAppIdOrRefId(applicationformdto.getMasterAppId(), null, orgId);

            // sms n email when application gets rejected

            if (workflowRequest != null
                    && workflowRequest.getLastDecision().equals(MainetConstants.WorkFlow.Decision.REJECTED)) {
                // sms and email (when application form rejects (workflow))
                final SMSAndEmailDTO smsDto = new SMSAndEmailDTO();
                Organisation org = UserSession.getCurrent().getOrganisation();
                smsDto.setMobnumber(applicationformdto.getMobNum().toString());
                smsDto.setAppNo(applicationformdto.getMasterAppId().toString());
                ServiceMaster sm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
                        .getServiceMasterByShortCode(MainetConstants.SocialSecurity.SERVICE_CODE,
                                orgId);
                // setServiceMaster(sm);
                smsDto.setServName(sm.getSmServiceName());
                String url = "SchemeApplicationForm.html";
                org.setOrgid(UserSession.getCurrent().getOrganisation().getOrgid());
                smsDto.setUserId(UserSession.getCurrent().getEmployee().getEmpId());
                int langId = UserSession.getCurrent().getLanguageId();
                ismsAndEmailService.sendEmailSMS("SWD", url,
                        MainetConstants.SocialSecurity.REJECTED, smsDto, org, langId);
                
                schemeApplicationFormRepository.rejectPension(applicationformdto.getMasterAppId().toString(), orgId);
                 
                

            }
            // if user is last who reject or approve according to that we can update our own table flag
            if (iWorkFlowTypeService.isLastTaskInCheckerTaskList(workFlowActionDto.getTaskId())) {
                schemeApplicationFormRepository.updateApprovalFlag(workFlowActionDto.getApplicationId().toString(), applicationformdto.getParentOrgId(),
                        workFlowActionDto.getDecision().substring(0, 1));

                // sms and email (when application form approves (workflow))
                final SMSAndEmailDTO smsDto = new SMSAndEmailDTO();
                Organisation org = UserSession.getCurrent().getOrganisation();
                smsDto.setMobnumber(applicationformdto.getMobNum().toString());
                smsDto.setAppNo(applicationformdto.getMasterAppId().toString());
                ServiceMaster sm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
                        .getServiceMasterByShortCode(MainetConstants.SocialSecurity.SERVICE_CODE,
                                orgId);
                // setServiceMaster(sm);
                smsDto.setServName(sm.getSmServiceName());
                String url = "SchemeApplicationForm.html";
                org.setOrgid(UserSession.getCurrent().getOrganisation().getOrgid());
                smsDto.setUserId(UserSession.getCurrent().getEmployee().getEmpId());
                int langId = UserSession.getCurrent().getLanguageId();
                ismsAndEmailService.sendEmailSMS("SWD", url,
                        PrefixConstants.SMS_EMAIL_ALERT_TYPE.APPROVAL, smsDto, org, langId);
               
            }
            status = true;
        } catch (Exception ex) {
            throw new FrameworkException(
                    "Exception occurs while updating workflow,upload docs,updating table", ex);
        }
        return status;
    }

    @Override
    @Transactional
    public ApplicationFormDto saveDataLegacyFormDetails(ApplicationFormDto dto) {
        try {
            dto.setBeneficiaryno(getbeneficiarynumber(dto.getUlbName(), dto.getServiceCode(), dto.getOrgId()));
            SocialSecurityApplicationForm entity = ApplicationFormDetailsMapper.dtoToEntity(dto);
            entity.setDatalegacyflag(MainetConstants.FlagD);
            entity.setSapiStatus("A");
            schemeApplicationFormRepository.save(entity);
            dto.setStatusFlag(true);
        } catch (Exception ex) {
            throw new FrameworkException(
                    "Exception occured while saving the Data Legacy Form Details so please check all mandatory fields", ex);
        }
        return dto;

    }

    @Override
    public String getbeneficiarynumber(String ulbName, String serviceSortCode, Long orgId) {
        try {
            Long squenceNo = seqGenFunctionUtility.generateSequenceNo(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE,
                    "TB_SWD_SCHEME_APPLICATION", "BENEFICIARY_NUMBER", orgId, MainetConstants.FlagC, null);
            String benefiNo = ulbName + "/" + serviceSortCode + "/" + squenceNo;
            return benefiNo;
        } catch (Exception e) {
            throw new FrameworkException("Proper input not getting to generate beneficiary number", e);
        }

    }
    
    
    @POST
    @Path("/getSchemeName")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<Object[]> getSchemeName(ApplicationFormDto dto) {
        Organisation org = new Organisation();
        final LookUp lookUpFieldStatus = CommonMasterUtility.getLookUpFromPrefixLookUpValue(MainetConstants.MASTER.A,
                PrefixConstants.ACN,
                dto.getLangId().intValue(), org);
        final Long activeStatusId = lookUpFieldStatus.getLookUpId();
        return serviceMasterService.findAllActiveServicesWhichIsNotActual(dto.getOrgId(), dto.getDeptId(), activeStatusId, "N");
    }
      
    @POST
    @Path("/getDeptIdByServiceShortName/{orgId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Long getDeptIdByServiceShortName(@PathParam(value = "orgId") Long orgId) {
        ServiceMaster service = serviceMasterService.getServiceByShortName(MainetConstants.SocialSecurity.SERVICE_CODE, orgId);
        return service.getTbDepartment().getDpDeptid();
    }
     

    

}
