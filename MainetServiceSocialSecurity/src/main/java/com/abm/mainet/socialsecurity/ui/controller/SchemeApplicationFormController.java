/**
 * 
 */
package com.abm.mainet.socialsecurity.ui.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.dto.JsonViewObject;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.integration.brms.datamodel.CheckListModel;
import com.abm.mainet.common.integration.brms.service.BRMSCommonService;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.integration.dto.DocumentDetailsVO;
import com.abm.mainet.common.integration.dto.WSRequestDTO;
import com.abm.mainet.common.integration.dto.WSResponseDTO;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.ui.controller.AbstractFormController;
import com.abm.mainet.common.utility.ApplicationContextProvider;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.RestClient;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.common.workflow.service.IWorkFlowTypeService;
import com.abm.mainet.socialsecurity.service.ISchemeApplicationFormService;
import com.abm.mainet.socialsecurity.ui.dto.ApplicationFormDto;
import com.abm.mainet.socialsecurity.ui.model.SchemeApplicationFormModel;
import com.abm.mainet.socialsecurity.ui.validator.ApplicationFormValidator;

/**
 * @author priti.singh
 *
 */
@Controller
@RequestMapping("SchemeApplicationForm.html")
public class SchemeApplicationFormController extends AbstractFormController<SchemeApplicationFormModel> {

    @Autowired
    private ISchemeApplicationFormService schemeApplicationFormService;

    @Autowired
    private BRMSCommonService brmsCommonService;

    @Autowired
    private IFileUploadService fileUpload;

    @Autowired
    TbDepartmentService tbDepartmentService;

    @Autowired
    ServiceMasterService serviceMasterService;

    private static final Logger LOGGER = Logger.getLogger(SchemeApplicationFormController.class);

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView index(final Model model, final HttpServletRequest httpServletRequest) {
        sessionCleanup(httpServletRequest);
        fileUpload.sessionCleanUpForFileUpload();
        SchemeApplicationFormModel appModel = this.getModel();
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
        appModel.getAndSetPrefix(orgId, langId, org);
        this.getModel().setCommonHelpDocs("SchemeApplicationForm.html");
        return index();

    }

    @RequestMapping(params = "saveCheckListAppdetails", method = RequestMethod.POST)
    public ModelAndView saveCheckListAppdetails(final Model model, final HttpServletRequest httpServletRequest) {
        bindModel(httpServletRequest);
        JsonViewObject respObj;
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long empId = UserSession.getCurrent().getEmployee().getEmpId();
        Long langId = (long) UserSession.getCurrent().getLanguageId();
        String ulbName = UserSession.getCurrent().getOrganisation().getOrgShortNm();
        String ipMacAddress = Utility.getClientIpAddress(httpServletRequest);
        SchemeApplicationFormModel appModel = this.getModel();
        ApplicationFormDto dto = appModel.getApplicationformdto();
        appModel.validateBean(dto, ApplicationFormValidator.class);
        // this call is for checklist validation
        List<DocumentDetailsVO> docs = appModel.getCheckList();
        if (docs != null) {
            docs = fileUpload.prepareFileUpload(docs);
        }
        dto.setDocumentList(docs);
        fileUpload.validateUpload(appModel.getBindingResult());
        // this call is for checklist validation end
        if (appModel.hasValidationErrors()) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + MainetConstants.FORM_NAME, appModel.getBindingResult());
            return new ModelAndView(MainetConstants.SocialSecurity.CHECK_LIST_FORM, MainetConstants.FORM_NAME, appModel);
        }
        List<Object[]> list = appModel.getServiceList().parallelStream().filter(s -> s[0].equals(dto.getSelectSchemeName()))
                .collect(Collectors.toList());
        list.parallelStream().forEach(l -> {
            dto.setServiceCode(l[2].toString());
        });
        dto.setOrgId(orgId);
        dto.setCreatedBy(empId);
        dto.setCreatedDate(new Date());
        dto.setLgIpMac(ipMacAddress);
        dto.setLangId(langId);
        dto.setUlbName(ulbName);
        if (appModel.saveForm()) {
            respObj = JsonViewObject.successResult(ApplicationSession.getInstance().getMessage(appModel.getSuccessMessage()));

        } else {
            respObj = JsonViewObject
                    .successResult(ApplicationSession.getInstance().getMessage("social.sec.notsave.success"));

        }
        return new ModelAndView(new MappingJackson2JsonView(), MainetConstants.FORM_NAME, respObj);

    }

    // workflow show details
    @RequestMapping(params = "showDetails", method = RequestMethod.POST)
    public ModelAndView executeChangeOfOwner(@RequestParam("appNo") final Long applicationId,
            @RequestParam("taskId") final String serviceId,
            @RequestParam(value = "actualTaskId", required = false) final Long actualTaskId,
            @RequestParam("workflowId") final Long workflowId, 
            final HttpServletRequest request) {
        fileUpload.sessionCleanUpForFileUpload();
        sessionCleanup(request);
        getModel().bind(request);
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long parentOrgId = ApplicationContextProvider.getApplicationContext().getBean(IWorkFlowTypeService.class)
                .findById(workflowId).getCurrentOrgId();
        
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
        SchemeApplicationFormModel model = this.getModel();
        model.getWorkflowActionDto().setTaskId(actualTaskId);
        model.getWorkflowActionDto().setApplicationId(applicationId);
        model.setParentOrgId(parentOrgId);
        try {
            ApplicationFormDto dto = schemeApplicationFormService.findApplicationdetails(applicationId, parentOrgId);
            model.getAndSetPrefix(parentOrgId, langId, org);
            dto.setBplid(model.getBplList().stream().filter(l -> l.getLookUpCode().contains(dto.getIsBplApplicable()))
                    .collect(Collectors.toList()).get(0).getLookUpId());
            model.setApplicationformdto(dto);
            dto.setParentOrgId(parentOrgId);
        } catch (final Exception ex) {
            LOGGER.warn("Problem while rendering form:", ex);
            return defaultExceptionFormView();
        }
        return new ModelAndView("applicationFormView", MainetConstants.FORM_NAME, model);
    }

    @RequestMapping(params = "showCheckList", method = RequestMethod.POST)
    public ModelAndView showCheckList(final Model model, final HttpServletRequest httpServletRequest) {
        bindModel(httpServletRequest);
        SchemeApplicationFormModel schemeAppmodel = this.getModel();
        try {
            Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
            List<DocumentDetailsVO> docs = null;
            final WSRequestDTO initRequestDto = new WSRequestDTO();
            initRequestDto.setModelName(MainetConstants.SocialSecurity.SOCIAL_CHECKLIST);
            WSResponseDTO response = brmsCommonService.initializeModel(initRequestDto);
            if (response.getWsStatus() != null
                    && MainetConstants.WebServiceStatus.SUCCESS.equalsIgnoreCase(response.getWsStatus())) {
                List<Object> checklist = RestClient.castResponse(response, CheckListModel.class, 0);
                CheckListModel checkListModel = (CheckListModel) checklist.get(0);
                checkListModel.setOrgId(orgId);
                /* checkListModel.setServiceCode("IGNDS"); */
                checkListModel.setServiceCode(MainetConstants.SocialSecurity.IGNDS);
                final WSRequestDTO checkRequestDto = new WSRequestDTO();
                checkRequestDto.setDataModel(checkListModel);
                WSResponseDTO checklistResp = brmsCommonService.getChecklist(checkRequestDto);
                if (response.getWsStatus() != null
                        && MainetConstants.WebServiceStatus.SUCCESS.equalsIgnoreCase(response.getWsStatus())) {
                    docs = (List<DocumentDetailsVO>) checklistResp.getResponseObj();
                    if (docs != null && !docs.isEmpty()) {
                        long cnt = 1;
                        for (final DocumentDetailsVO doc : docs) {
                            doc.setDocumentSerialNo(cnt);
                            cnt++;
                        }
                    }
                    schemeAppmodel.setCheckList(docs);

                }
                return new ModelAndView(MainetConstants.SocialSecurity.CHECK_LIST_FORM,
                        MainetConstants.FORM_NAME, schemeAppmodel);

            } else {
                return new ModelAndView(MainetConstants.DEFAULT_EXCEPTION_VIEW);
            }
        } catch (FrameworkException e) {
            LOGGER.warn(e.getErrMsg());
            schemeAppmodel.setCheckList(null);
            return new ModelAndView(MainetConstants.DEFAULT_EXCEPTION_VIEW);
        }

    }

    // workflow save decision call
    @RequestMapping(params = "saveDecision", method = RequestMethod.POST)
    public ModelAndView approvalDecision(final HttpServletRequest httpServletRequest) {
        
        JsonViewObject respObj = null;

        this.bindModel(httpServletRequest);

        SchemeApplicationFormModel asstModel = this.getModel();

        String decision = asstModel.getWorkflowActionDto().getDecision();

        boolean updFlag = asstModel.callWorkFlow();

        if (updFlag) {
            if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.APPROVED))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("social.info.application.approved"));
            else if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.REJECTED))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("social.info.application.reject"));
            else if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.SEND_BACK))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("social.info.application.sendBack"));
        } else {
            respObj = JsonViewObject
                    .successResult(ApplicationSession.getInstance().getMessage("social.info.application.failure"));
        }

        return new ModelAndView(new MappingJackson2JsonView(), MainetConstants.FORM_NAME, respObj);

    }

}
