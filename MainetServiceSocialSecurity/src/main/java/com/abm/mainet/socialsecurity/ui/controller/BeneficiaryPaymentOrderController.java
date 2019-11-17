package com.abm.mainet.socialsecurity.ui.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.abm.mainet.cfc.checklist.service.IChecklistVerificationService;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.dto.JsonViewObject;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.ui.controller.AbstractFormController;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.common.workflow.service.IWorkFlowTypeService;
import com.abm.mainet.socialsecurity.service.IBeneficiaryPaymentOrderService;
import com.abm.mainet.socialsecurity.ui.dto.BeneficiaryPaymentOrderDto;
import com.abm.mainet.socialsecurity.ui.model.BeneficiaryPaymentOrderModel;

@Controller
@RequestMapping("BeneficiaryPaymentOrder.html")

public class BeneficiaryPaymentOrderController extends AbstractFormController<BeneficiaryPaymentOrderModel> {

    private static final Logger logger = Logger.getLogger(BeneficiaryPaymentOrderController.class);

    @Autowired
    ServiceMasterService serviceMasterService;
    @Autowired
    TbDepartmentService tbDepartmentService;
    @Autowired
    IBeneficiaryPaymentOrderService beneficiaryPaymentOrderService;
    @Autowired
    private IWorkFlowTypeService iWorkFlowTypeService;
    @Autowired
    private IFileUploadService fileUpload;
    @Autowired
    private IChecklistVerificationService iChecklistVerificationService;

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView index(final Model model, final HttpServletRequest httpServletRequest) {
        fileUpload.sessionCleanUpForFileUpload();
        sessionCleanup(httpServletRequest);
        BeneficiaryPaymentOrderModel bpomodel = this.getModel();
        bpomodel.getAndSetCommonValues();
        return index();
    }

    @RequestMapping(params = "filterSearchData", method = { RequestMethod.POST })
    public @ResponseBody List<BeneficiaryPaymentOrderDto> filterSearchData(final HttpServletRequest request,
            @RequestParam(value = "serviceId") final Long serviceId,
            @RequestParam(value = "paymentscheId") final Long paymentscheId,
            @RequestParam(value = "year") final Long year,
            @RequestParam(value = "month") final Long month) {
        BeneficiaryPaymentOrderModel bpomodel = this.getModel();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        List<BeneficiaryPaymentOrderDto> filterSearchDataList = beneficiaryPaymentOrderService.filterSearchData(serviceId,
                paymentscheId, year, month, orgId);
        bpomodel.setDto(filterSearchDataList);
        return filterSearchDataList;
    }

    @RequestMapping(params = "saveBeneficiaryDetails", method = { RequestMethod.POST })
    public ModelAndView saveBeneficiaryDetails(final HttpServletRequest request,
            @RequestParam(value = "list[]") final String[] list, @RequestParam(value = "remark") final String remark) {
        BeneficiaryPaymentOrderModel bpomodel = this.getModel();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
        String ipMacAddress = Utility.getClientIpAddress(request);
        bpomodel.saveBeneficiaryDetails(orgId, langId, list, remark, ipMacAddress);
        return new ModelAndView("rtgsPaymentAdviceReport", MainetConstants.FORM_NAME, bpomodel);
    }

    // workflow show details
    @RequestMapping(params = "showDetails", method = RequestMethod.POST)
    public ModelAndView showDetails(@RequestParam("appNo") final String applicationId,
            @RequestParam("taskId") final String serviceId,
            @RequestParam(value = "actualTaskId", required = false) final Long actualTaskId,
            final HttpServletRequest request) {
        fileUpload.sessionCleanUpForFileUpload();
        sessionCleanup(request);
        getModel().bind(request);
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        BeneficiaryPaymentOrderModel model = this.getModel();
        model.getWorkflowActionDto().setTaskId(actualTaskId);
        model.getWorkflowActionDto().setReferenceId(applicationId);
        model.getAndSetCommonValues();
        try {

            BeneficiaryPaymentOrderDto dto = beneficiaryPaymentOrderService.getViewDataFromRtgsPayment(orgId, applicationId);
            model.setBpoDto(dto);

        } catch (final Exception ex) {
            logger.error("Problem while rendering form:", ex);
            return defaultExceptionFormView();
        }
        
        return new ModelAndView("beneficiaryPaymentOrderView", MainetConstants.FORM_NAME, model);
    }

    // workflow save decision
    @RequestMapping(params = "saveDecision", method = RequestMethod.POST)
    public ModelAndView approvalDecision(final HttpServletRequest httpServletRequest) {

        JsonViewObject respObj = null;

        this.bindModel(httpServletRequest);

        BeneficiaryPaymentOrderModel aModel = this.getModel();

        String decision = aModel.getWorkflowActionDto().getDecision();

        boolean updFlag = aModel.callWorkFlow();
        if (beneficiaryPaymentOrderService.checkAccountActiveOrNot()
                && decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.APPROVED)
                && iWorkFlowTypeService.isLastTaskInCheckerTaskList(aModel.getWorkflowActionDto().getTaskId())) {
            aModel.accountBillPostingForthesocialSecurity();
        }
        if (updFlag) {
            if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.APPROVED))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("assest.info.application.approved"));
            else if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.REJECTED))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("assest.info.application.reject"));
            else if (decision.equalsIgnoreCase(MainetConstants.WorkFlow.Decision.SEND_BACK))
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("assest.info.application.sendBack"));
        } else {
            respObj = JsonViewObject
                    .successResult(ApplicationSession.getInstance().getMessage("assest.info.application.failure"));
        }

        return new ModelAndView(new MappingJackson2JsonView(), MainetConstants.FORM_NAME, respObj);

    }

}
