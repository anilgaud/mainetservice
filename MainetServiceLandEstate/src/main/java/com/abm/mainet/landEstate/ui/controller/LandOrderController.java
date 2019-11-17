package com.abm.mainet.landEstate.ui.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.abm.mainet.cfc.scrutiny.ui.controller.ApplicationAuthorizationController;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.domain.CFCApplicationAddressEntity;
import com.abm.mainet.common.domain.TbCfcApplicationMstEntity;
import com.abm.mainet.common.dto.TbApprejMas;
import com.abm.mainet.common.dto.TbWorkOrder;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.master.dto.TbDepartment;
import com.abm.mainet.common.master.service.TbDepartmentService;
import com.abm.mainet.common.master.service.TbServicesMstService;
import com.abm.mainet.common.service.CommonService;
import com.abm.mainet.common.service.ICFCApplicationAddressService;
import com.abm.mainet.common.service.ICFCApplicationMasterService;
import com.abm.mainet.common.service.TbApprejMasService;
import com.abm.mainet.common.service.TbWorkOrderService;
import com.abm.mainet.common.ui.controller.telosys.AbstractController;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.landEstate.service.ILandAcquisitionService;

@Controller
@RequestMapping(value = { "/LandOrder.html" })
public class LandOrderController extends AbstractController {

    // --- Variables names ( to be used in JSP with Expression Language )
    private static final String MAIN_ENTITY_NAME = "tbWorkOrder";
    private static final String MAIN_PRIFEX_WPC = "WOR";
    // --- JSP pages names ( View name in the MVC model )
    private static final String JSP_LIST = "landOrder/list";

    String REDIRECT_LAND_ORDER_HTML = "redirect:/LandOrder.html?generatLandOrder";

    private static final String ERROR_MESSAGE = "Error Occurred while request processing for Application Authorization for Application No.=";
    private static final String SERVICE_URL_NOT_CONFIGURED = "Service action Url is not configured in Service Master against serviceId=";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationAuthorizationController.class);

    /**
     * Default constructor
     */

    public LandOrderController() {
        super(LandOrderController.class, MAIN_ENTITY_NAME);
    }

    @Resource
    private CommonService commonService;

    // --- Main entity service
    @Autowired
    private TbWorkOrderService tbWorkOrderService;

    @Resource
    private TbServicesMstService tbServicesMstService;
    // --- Main entity service
    @Resource
    private TbApprejMasService tbApprejMasService;

    @Resource
    private TbDepartmentService tbDepartmentService;

    @Resource
    private ICFCApplicationMasterService icfcApplicationMasterService;

    @Autowired
    private ICFCApplicationAddressService iCFCApplicationAddressService;

    @Autowired
    private ILandAcquisitionService acquisitionService;

    /**
     * Shows a list with all the occurrences of TbWorkOrder found in the database
     * 
     * @param model Spring MVC model
     * @return
     */
    @RequestMapping(params = "showDetails", method = { RequestMethod.POST, RequestMethod.GET })
    public String workorder(@RequestParam("appNo") final long applicationId,
            @RequestParam("actualTaskId") final Long actualTaskId, final HttpServletRequest request,
            final Model model) {
        final TbWorkOrder tbWorkOrder = new TbWorkOrder();
        String actionURL = null;
        try {
            final List<String> paramList = commonService.findServiceActionUrl(applicationId,
                    UserSession.getCurrent().getOrganisation().getOrgid());

            UserSession.getCurrent().getScrutinyCommonParamMap().put(
                    MainetConstants.SCRUTINY_COMMON_PARAM.APM_APPLICATION_ID, applicationId + MainetConstants.BLANK);
            UserSession.getCurrent().getScrutinyCommonParamMap().put(MainetConstants.SCRUTINY_COMMON_PARAM.CFC_URL,
                    paramList.get(MainetConstants.INDEX.ZERO));
            UserSession.getCurrent().getScrutinyCommonParamMap()
                    .put(MainetConstants.SCRUTINY_COMMON_PARAM.SM_SERVICE_ID, paramList.get(MainetConstants.INDEX.ONE));
            UserSession.getCurrent().getScrutinyCommonParamMap().put(MainetConstants.SCRUTINY_COMMON_PARAM.TASK_ID,
                    actualTaskId.toString());
            if ((paramList.get(MainetConstants.INDEX.ZERO) == null)
                    || paramList.get(MainetConstants.INDEX.ZERO).toString().isEmpty()) {
                throw new FrameworkException(SERVICE_URL_NOT_CONFIGURED + paramList.get(MainetConstants.INDEX.ONE));
            } else {

                final String serviceId = UserSession.getCurrent().getScrutinyCommonParamMap()
                        .get(MainetConstants.SCRUTINY_COMMON_PARAM.SM_SERVICE_ID);
                final String taskId = UserSession.getCurrent().getScrutinyCommonParamMap()
                        .get(MainetConstants.SCRUTINY_COMMON_PARAM.TASK_ID);

                request.getSession().setAttribute(MainetConstants.REQUIRED_PG_PARAM.APPLICATION_NO, applicationId);
                request.getSession().setAttribute(MainetConstants.REQUIRED_PG_PARAM.SERVICE_ID, serviceId);
                request.getSession().setAttribute(MainetConstants.SCRUTINY_COMMON_PARAM.TASK_ID, taskId);

                return new String(REDIRECT_LAND_ORDER_HTML);
                // return new String("redirect:/generatLandOrder");
            }

        } catch (final Exception ex) {
            LOGGER.error(ERROR_MESSAGE + applicationId, ex);
        }
        model.addAttribute(MAIN_ENTITY_NAME, tbWorkOrder);
        return JSP_LIST;

    }

    @RequestMapping(params = "generatLandOrder", method = { RequestMethod.POST, RequestMethod.GET })
    public String generatLandOrder(final HttpServletRequest httpServletRequest, final Model model) {
        Long taskId = null;
        final long applicationId = Long.parseLong(httpServletRequest.getSession()
                .getAttribute(MainetConstants.REQUIRED_PG_PARAM.APPLICATION_NO).toString());
        final long serviceId = Long.parseLong(httpServletRequest.getSession().getAttribute("serviceId").toString());

        final String serviceName = tbServicesMstService.getServiceNameByServiceId(serviceId);

        if (!httpServletRequest.getSession().getAttribute("taskId").equals("null")) {
            taskId = Long.parseLong(httpServletRequest.getSession().getAttribute("taskId").toString());
        }

        final TbWorkOrder tbWorkOrder = new TbWorkOrder();
        long workorderid = 0;
        final long deparmentid = tbServicesMstService.findDepartmentIdByserviceid(serviceId,
                UserSession.getCurrent().getOrganisation().getOrgid());

        TbDepartment department = tbDepartmentService.findById(deparmentid);
        String dept = MainetConstants.BLANK;
        if (UserSession.getCurrent().getLanguageId() == MainetConstants.ENGLISH) {
            dept = department.getDpDeptdesc();
        } else {
            dept = department.getDpNameMar();
        }

        final CFCApplicationAddressEntity address = iCFCApplicationAddressService.getApplicationAddressByAppId(
                Long.valueOf(applicationId + MainetConstants.BLANK),
                UserSession.getCurrent().getOrganisation().getOrgid());
        String mobileNo = address.getApaMobilno() != null ? address.getApaMobilno() : MainetConstants.BLANK;
        final TbCfcApplicationMstEntity tbCfcApplicationMstEntity = icfcApplicationMasterService
                .getCFCApplicationByApplicationId(applicationId, UserSession.getCurrent().getOrganisation().getOrgid());
        final String ApplicantFullName = tbCfcApplicationMstEntity.getApmFname() + MainetConstants.WHITE_SPACE
                + (tbCfcApplicationMstEntity.getApmMname() != null ? tbCfcApplicationMstEntity.getApmMname()
                        : MainetConstants.BLANK)
                + MainetConstants.WHITE_SPACE
                + (tbCfcApplicationMstEntity.getApmLname() != null ? tbCfcApplicationMstEntity.getApmLname()
                        : MainetConstants.BLANK);
        final Date ApplicarionDate = tbCfcApplicationMstEntity.getApmApplicationDate();
        final List<LookUp> lookUpList = CommonMasterUtility.getListLookup(PrefixConstants.WATERMODULEPREFIX.REM,
                UserSession.getCurrent().getOrganisation());
        for (final LookUp Lookup1 : lookUpList) {

            if (Lookup1.getLookUpCode().equalsIgnoreCase(MAIN_PRIFEX_WPC)) {
                workorderid = Lookup1.getLookUpId();
            }
        }
        List<TbApprejMas> apprejMasList = new ArrayList<>();
        apprejMasList = tbApprejMasService.findByRemarkType(serviceId, workorderid);
        tbWorkOrder.setWoServiceId(serviceId);
        tbWorkOrder.setWoDeptId(deparmentid);
        tbWorkOrder.setWoApplicationDateS(ApplicarionDate + MainetConstants.BLANK);
        tbWorkOrder.setWoApplicationId(applicationId);
        tbWorkOrder.setTaskId(taskId);
        model.addAttribute("applicationId", applicationId);
        model.addAttribute("ApplicantFullName", ApplicantFullName);
        model.addAttribute("ApplicarionDate", ApplicarionDate);
        model.addAttribute("apprejMasList", apprejMasList);
        model.addAttribute(MAIN_ENTITY_NAME, tbWorkOrder);
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("deptName", dept);
        model.addAttribute("mobileNo", mobileNo);
        httpServletRequest.getSession().removeAttribute("applicationId");
        httpServletRequest.getSession().removeAttribute("conncetionNo");
        httpServletRequest.getSession().removeAttribute("serviceId");
        httpServletRequest.getSession().removeAttribute("taskId");
        return JSP_LIST;
    }

    /**
     * 'CREATE' action processing. <br>
     * This action is based on the 'Post/Redirect/Get (PRG)' pattern, so it ends by 'http redirect'<br>
     * 
     * @param tbWorkOrder entity to be created
     * @param bindingResult Spring MVC binding result
     * @param model Spring MVC model
     * @param redirectAttributes Spring MVC redirect attributes
     * @param httpServletRequest
     * @return
     */
    @RequestMapping(params = "create", method = RequestMethod.POST) // GET or POST
    public String create(@Valid final TbWorkOrder tbWorkOrder, final BindingResult bindingResult, final Model model,
            final HttpServletRequest httpServletRequest) {

        try {

            if (!bindingResult.hasErrors()) {
                final String woApplicationDate = tbWorkOrder.getWoApplicationDateS();
                final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                final Date date = formatter.parse(woApplicationDate);
                tbWorkOrder.setWoApplicationDate(date);
                final TbWorkOrder tbWorkOrderCreated = tbWorkOrderService.create(tbWorkOrder);
                // update ACQ_STATUS like Acquired (A)
                Long apmApplicationId = tbWorkOrder.getWoApplicationId();
                acquisitionService.updateLandProposalAcqStatusById(UserSession.getCurrent().getEmployee().getEmpId(),
                        UserSession.getCurrent().getEmployee().getEmppiservername(), apmApplicationId);
                model.addAttribute(MAIN_ENTITY_NAME, tbWorkOrderCreated);
                model.addAttribute(MainetConstants.CommonConstants.SUCCESS_URL, "AdminHome.html");
                return MainetConstants.CommonConstants.SUCCESS_PAGE;
            } else {
                return new String("redirect:/AdminHome.html?");
            }
        } catch (final Exception e) {
            log("Action 'create' : Exception - " + e.getMessage());
            messageHelper.addException(model, "tbWorkOrder.error.create", e);
            return new String("redirect:/AdminHome.html?");
        }
    }

}
