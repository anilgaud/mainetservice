/**
 * 
 */
package com.abm.mainet.socialsecurity.ui.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.dto.JsonViewObject;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.ui.controller.AbstractFormController;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.SeqGenFunctionUtility;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.socialsecurity.service.IPensionSchemeMasterService;
import com.abm.mainet.socialsecurity.ui.dto.PensionEligibilityCriteriaDto;
import com.abm.mainet.socialsecurity.ui.dto.PensionSourceOfFundDto;
import com.abm.mainet.socialsecurity.ui.dto.ViewDtoList;
import com.abm.mainet.socialsecurity.ui.model.PensionSchemeMasterModel;

/**
 * @author satish.rathore
 *
 */
@Controller
@RequestMapping(MainetConstants.SocialSecurity.PENSION_SCHEME_MASTER_URL)
public class PensionSchemeMasterController extends AbstractFormController<PensionSchemeMasterModel> {

    @Autowired
    ServiceMasterService serviceMasterService;
    @Autowired
    TbDepartmentService tbDepartmentService;
    @Autowired
    private SeqGenFunctionUtility seqGenFunctionUtility;
    @Autowired
    private IPensionSchemeMasterService pensionSchemeMasterService;

    private static final Logger LOGGER = Logger.getLogger(PensionSchemeMasterController.class);

    private static final String ELIGIBILITY_CRITERIA = "EligibilityCriteria";

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView index(final Model model, final HttpServletRequest httpServletRequest) {
        sessionCleanup(httpServletRequest);
        PensionSchemeMasterModel pSmodel = this.getModel();
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
        Long depId = tbDepartmentService.getDepartmentIdByDeptCode(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
        final LookUp lookUpFieldStatus = CommonMasterUtility.getLookUpFromPrefixLookUpValue(MainetConstants.MASTER.A,
                PrefixConstants.ACN, langId, org);
        final Long activeStatusId = lookUpFieldStatus.getLookUpId();
        pSmodel.setViewList(pensionSchemeMasterService.getAllData(orgId, depId, activeStatusId));
        pSmodel.setServiceList(serviceMasterService.findAllActiveServicesWhichIsNotActual(orgId, depId, activeStatusId, "N"));

        return index();
    }

    @RequestMapping(params = "showPensionSchemeForm", method = { RequestMethod.POST })
    public ModelAndView shemeMasterForm(@RequestParam(value = "type", required = false) String type, final Model model,
            final HttpServletRequest httpServletRequest) {
        sessionCleanup(httpServletRequest);
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
        PensionSchemeMasterModel pSmodel = this.getModel();
        pSmodel.setModeType(type);
        pSmodel.setSourceLookUps(CommonMasterUtility.getNextLevelData(MainetConstants.SocialSecurity.FTR, 1, orgId));
        pSmodel.setSecondLevellookUps(
                CommonMasterUtility.getNextLevelData(MainetConstants.SocialSecurity.FTR, 2, orgId));
        List<LookUp> paymentList = CommonMasterUtility.getLookUps(MainetConstants.SocialSecurity.PAYMENT_PREFIX, org);
        Long depId = tbDepartmentService.getDepartmentIdByDeptCode(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
        final LookUp lookUpFieldStatus = CommonMasterUtility.getLookUpFromPrefixLookUpValue(MainetConstants.MASTER.A,
                PrefixConstants.ACN, langId, org);
        final Long activeStatusId = lookUpFieldStatus.getLookUpId();
        pSmodel.setServiceList(serviceMasterService.findAllActiveServicesWhichIsNotActual(orgId, depId, activeStatusId, "N"));
        pSmodel.setSponcerByList(CommonMasterUtility.getLookUps(MainetConstants.SocialSecurity.SBY, org));
        pSmodel.setPaymentList(paymentList);

        pSmodel.getPensionSchmDto().getPensionSourceFundList().add(new PensionSourceOfFundDto());
        this.getModel().setCommonHelpDocs("PensionSchemeMaster.html");
        return new ModelAndView("PensionSchemeMasterForm", MainetConstants.FORM_NAME, pSmodel);
    }

    @RequestMapping(params = "savePensionScheme", method = RequestMethod.POST)
    public ModelAndView savePensionScheme(final Model model, final HttpServletRequest httpServletRequest) {

        bindModel(httpServletRequest);

        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long squenceNo = seqGenFunctionUtility.generateSequenceNo(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE,
                "TB_SWD_SCHEME_MAST", "SDSCH_ID", orgId, MainetConstants.FlagC, null);
        final PensionSchemeMasterModel pensionModel = this.getModel();

        final List<PensionEligibilityCriteriaDto> viewList = new ArrayList<>();

        pensionModel.getPensionSchmDto().getPensioneligibilityList().stream().forEach(h -> {
            if (h.getCheckBox()) {
                PensionEligibilityCriteriaDto pecdto = new PensionEligibilityCriteriaDto();

                pecdto.setAmt(h.getAmt());
                if (h.getAmt() != null) {
                    pecdto.setAmtq(h.getAmt());
                    pecdto.setAmtq(pecdto.getAmtq());

                    httpServletRequest.getSession().setAttribute("Amount", h.getAmt());
                }
                pecdto.setBatchId(h.getBatchId());
                pecdto.setCheckBox(h.getCheckBox());
                pecdto.setCriteriaDesc(h.getCriteriaDesc());
                pecdto.setCriteriaId(h.getCriteriaId());
                pecdto.setFactorApplicableDesc(h.getFactorApplicableDesc());
                pecdto.setFactorApplicableId(h.getFactorApplicableId());
                pecdto.setPaySchedule(h.getPaySchedule());
                pecdto.setRangeFrom(h.getRangeFrom());
                pecdto.setRangeTo(h.getRangeTo());
                pecdto.setBatchId(squenceNo);
                viewList.add(pecdto);

            }
        });
        List<LookUp> sourceLookUp = pensionSchemeMasterService.filterCriteria(pensionModel.getSourceLookUps(),
                viewList);

        List<PensionEligibilityCriteriaDto> l2 = new ArrayList<PensionEligibilityCriteriaDto>();
        List<Integer> da = new ArrayList<Integer>();
        List<Integer> l3 = new ArrayList<Integer>();

        viewList.stream().forEach(l -> {

            pensionModel.getPensionEligCriteriaDto().setFactorApplicableId(l.getFactorApplicableId());
            pensionModel.getPensionEligCriteriaDto().setCriteriaId(l.getCriteriaId());
            pensionModel.getPensionEligCriteriaDto().setRangeFrom(l.getRangeFrom());
            pensionModel.getPensionEligCriteriaDto().setRangeTo(l.getRangeTo());
            pensionModel.getPensionEligCriteriaDto().setAmt(l.getAmt());

            l2.add(pensionModel.getPensionEligCriteriaDto());

        });

        l2.stream().forEach(l -> {
            if (l.getFactorApplicableId() == 6332) {

                int Data = pensionSchemeMasterService.findfactorApplicable(l.getFactorApplicableId(), l.getCriteriaId(),
                        l.getRangeFrom(), l.getRangeTo(), (BigDecimal) httpServletRequest.getSession().getAttribute("Amount"),
                        orgId);

                da.add(Data);
            } else {
                int Data = pensionSchemeMasterService.findfactorApplicablewithoutamt(l.getFactorApplicableId(), l.getCriteriaId(),
                        l.getRangeFrom(), l.getRangeTo(), orgId);
                da.add(Data);
            }
        });

        da.stream().forEach(l -> {
            if (l > 0)
                l3.add(l);
        });

        ModelAndView modelAndView = null;
        if (da.size() != l3.size()) {
            pensionModel.getSaveDataList().add(viewList);
            pensionModel.setSourceLookUps(sourceLookUp);
            pensionModel.getUpdateBatchIdSet().add(squenceNo);
            pensionModel.getPensionSchmDto().setPensioneligibilityList(new ArrayList<>());
            modelAndView = new ModelAndView(ELIGIBILITY_CRITERIA, MainetConstants.FORM_NAME, pensionModel);
        }

        else {
            /* this.getModel().addValidationError("Duplicate Data"); */
            this.getModel().addValidationError(ApplicationSession.getInstance().getMessage("social.sec.duplicate"));
            modelAndView = new ModelAndView("EligibilityCriteriaValidn", MainetConstants.FORM_NAME, this.getModel());
            modelAndView.addObject(
                    BindingResult.MODEL_KEY_PREFIX
                            + ApplicationSession.getInstance().getMessage(MainetConstants.FORM_NAME),
                    getModel().getBindingResult());
        }
        return modelAndView;

    }

    @RequestMapping(params = "deleteSchemeDetails", method = RequestMethod.POST)
    public ModelAndView deleteSchemeDetails(@RequestParam(value = "batchId", required = true) final Long batchId,
            final Model model, final HttpServletRequest httpServletRequest) {
        bindModel(httpServletRequest);
        final PensionSchemeMasterModel pensionModel = this.getModel();
        List<List<PensionEligibilityCriteriaDto>> schemeList = new ArrayList<>();
        ListIterator<List<PensionEligibilityCriteriaDto>> iterator = pensionModel.getSaveDataList().listIterator();
        pensionModel.setSaveDataList(schemeList);
        while (iterator.hasNext()) {
            List<PensionEligibilityCriteriaDto> list = (List<PensionEligibilityCriteriaDto>) iterator.next();
            List<PensionEligibilityCriteriaDto> finallists = list.stream().parallel()
                    .filter(h -> (!h.getBatchId().equals(batchId))).collect(Collectors.toList());
            if (!finallists.isEmpty()) {
                pensionModel.getSaveDataList().add(finallists);
            }
        }
        pensionModel.getDeletedBatchIdSet().add(batchId);
        return new ModelAndView(ELIGIBILITY_CRITERIA, MainetConstants.FORM_NAME, pensionModel);

    }

    @Override
    @RequestMapping(params = "saveform", method = RequestMethod.POST)
    public ModelAndView saveform(final HttpServletRequest httpServletRequest) {

        bindModel(httpServletRequest);
        JsonViewObject respObj;
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long empId = UserSession.getCurrent().getEmployee().getEmpId();
        String ipMacAddress = Utility.getClientIpAddress(httpServletRequest);
        this.getModel().getPensionSchmDto().setSaveDataList(this.getModel().getSaveDataList());

        int serviceId = pensionSchemeMasterService.findServiceId(this.getModel().getPensionSchmDto().getServiceId(),
                UserSession.getCurrent().getOrganisation().getOrgid());

        // Logic for duplicate scheme name
        if (serviceId == 0) {
            boolean statusFlag = pensionSchemeMasterService.savePensionDetails(orgId, empId, ipMacAddress,
                    this.getModel().getPensionSchmDto());

            if (statusFlag) {
                respObj = JsonViewObject.successResult(ApplicationSession.getInstance().getMessage("social.sec.save.success"));
                this.getModel().setSuccessMessage("Save successfully");
            } else {
                respObj = JsonViewObject
                        .successResult(ApplicationSession.getInstance().getMessage("social.sec.notsave.success"));

            }
            return new ModelAndView(new MappingJackson2JsonView(), MainetConstants.FORM_NAME, respObj);

        } else {
            getModel().addValidationError(ApplicationSession.getInstance().getMessage("social.sec.scheme.exists"));

        }
        return defaultMyResult();

    }

    @RequestMapping(params = "editForm", method = RequestMethod.POST)
    public ModelAndView cdmForm(@RequestParam(value = "id") Long id, @RequestParam(value = "orgId") Long orgId,
            @RequestParam(value = "type") String type, final Model model, final HttpServletRequest httpServletRequest) {
        sessionCleanup(httpServletRequest);
        PensionSchemeMasterModel pSmodel = this.getModel();
        int langId = UserSession.getCurrent().getLanguageId();
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long depId = tbDepartmentService.getDepartmentIdByDeptCode(MainetConstants.SocialSecurity.DEPARTMENT_SORT_CODE);
        final LookUp lookUpFieldStatus = CommonMasterUtility.getLookUpFromPrefixLookUpValue(MainetConstants.MASTER.A,
                PrefixConstants.ACN, langId, org);
        final Long activeStatusId = lookUpFieldStatus.getLookUpId();
        pSmodel.setServiceList(serviceMasterService.findAllActiveServicesByDepartment(orgId, depId, activeStatusId));
        pSmodel.setSponcerByList(CommonMasterUtility.getLookUps(MainetConstants.SocialSecurity.SBY, org));
        List<LookUp> paymentList = CommonMasterUtility.getLookUps(MainetConstants.SocialSecurity.PAYMENT_PREFIX, org);
        pSmodel.setPaymentList(paymentList);
        pSmodel.setSecondLevellookUps(CommonMasterUtility.getNextLevelData(MainetConstants.SocialSecurity.FTR, 2, orgId));

        
        /*
         * // changes pSmodel.getPensionSchmDto().setSchmeMsId(id); pSmodel.getPensionSchmDto().getIsSchmeActive(); if
         * (pSmodel.getPensionSchmDto().getIsSchmeActive().equals("N")) { pSmodel.setModeType("V"); } if
         * (pSmodel.getPensionSchmDto().getIsSchmeActive().equals("Y")) { pSmodel.setModeType(type); }
         */
        
        
        
        pSmodel.getOneDetails(id, orgId, type);
        /* setting schemeid to inactive scheme if checkbox clicked */
        pSmodel.getPensionSchmDto().setSchmeMsId(id);
        pSmodel.getPensionSchmDto().getIsSchmeActive();
        // if scheme is inactive the form will open in view mode

        if (pSmodel.getPensionSchmDto().getIsSchmeActive().equals("N")) {
            pSmodel.setModeType("V");
        }
        if (pSmodel.getPensionSchmDto().getIsSchmeActive().equals("Y")) {
            pSmodel.setModeType(type);
        }

        pSmodel.getModeType();

        List<LookUp> set = pensionSchemeMasterService.filterCriteria(
                CommonMasterUtility.getNextLevelData(MainetConstants.SocialSecurity.FTR, 1, orgId),
                pSmodel.getPensionSchmDto().getPensioneligibilityList());
        pSmodel.getSourceLookUps().addAll(new HashSet<>(set));
        pSmodel.getPensionSchmDto().setPensioneligibilityList(new ArrayList<>());
        return new ModelAndView("PensionSchemeMasterForm", MainetConstants.FORM_NAME, pSmodel);
    }

    @RequestMapping(params = "updatePensionDetails", method = RequestMethod.POST)
    public ModelAndView updatePensionDetails(final Model model, final HttpServletRequest httpServletRequest) {
        bindModel(httpServletRequest);
        ModelAndView mv = null;
        BindingResult bindingResult = this.getModel().getBindingResult();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long empId = UserSession.getCurrent().getEmployee().getEmpId();
        String ipMacAddress = Utility.getClientIpAddress(httpServletRequest);
        this.getModel().getPensionSchmDto().setSaveDataList(this.getModel().getSaveDataList());
        boolean status = false;
        try {
            pensionSchemeMasterService.updatePensionDetails(orgId, empId, ipMacAddress, this.getModel().getPensionSchmDto(),
                    this.getModel().getDeletedBatchIdSet(), this.getModel().getUpdateBatchIdSet());
            status = true;
            mv = updateDetails(httpServletRequest, status);
        } catch (FrameworkException ex) {
            model.addAttribute(BindingResult.MODEL_KEY_PREFIX + MainetConstants.FORM_NAME, bindingResult);
            throw new FrameworkException(
                    "while updating and deleting data problem occurs", ex);
        }

        return mv;
    }

    @RequestMapping(params = "filterServices", method = { RequestMethod.POST })
    public @ResponseBody List<ViewDtoList> filterSearchData(final HttpServletRequest request,
            @RequestParam(value = "serviceId", required = true) final Long serviceId) {
        final List<ViewDtoList> viewDtoList;
        try {
            viewDtoList = this.getModel().getViewList().stream().filter(l -> l.getServiceId().equals(serviceId))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new FrameworkException("services are  not found", ex);
        }
        return viewDtoList;
    }

    private ModelAndView updateDetails(final HttpServletRequest request, final boolean status) {
        if (status) {
            return jsonResult(JsonViewObject
                    .successResult(getApplicationSession().getMessage("asset.maintainanceType.successMessage")));
        } else {
            return jsonResult(JsonViewObject
                    .failureResult(getApplicationSession().getMessage("asset.maintainanceType.failureMessage")));
        }
    }

    @RequestMapping(params = "inactiveScheme", method = RequestMethod.POST)
    public ModelAndView inactiveScheme(final HttpServletRequest httpServletRequest) {
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        PensionSchemeMasterModel pSmodel = this.getModel();
        pensionSchemeMasterService.inactiveScheme(pSmodel.getPensionSchmDto().getSchmeMsId(), orgId);

        return defaultMyResult();
    }

}
