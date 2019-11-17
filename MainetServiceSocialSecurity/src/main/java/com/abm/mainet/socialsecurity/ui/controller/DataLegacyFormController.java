/**
 * 
 */
package com.abm.mainet.socialsecurity.ui.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.dto.JsonViewObject;
import com.abm.mainet.common.ui.controller.AbstractFormController;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.socialsecurity.ui.dto.ApplicationFormDto;
import com.abm.mainet.socialsecurity.ui.model.DataLegacyModel;
import com.abm.mainet.socialsecurity.ui.validator.ApplicationFormValidator;

/**
 * @author priti.singh
 *
 */
@Controller
@RequestMapping("DataLegacyForm.html")
public class DataLegacyFormController extends AbstractFormController<DataLegacyModel>{
    
    private static final Logger LOGGER = Logger.getLogger(DataLegacyFormController.class);
    
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public ModelAndView index(final Model model, final HttpServletRequest httpServletRequest) {
        sessionCleanup(httpServletRequest);
        DataLegacyModel dataLegacyModel = this.getModel();
        Organisation org = UserSession.getCurrent().getOrganisation();
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        int langId = UserSession.getCurrent().getLanguageId();
       dataLegacyModel.getAndSetPrefix(orgId, langId, org);
        this.getModel().setCommonHelpDocs("DataLegacyForm.html");
        return index();

    }
    
    
    
    @Override
    @RequestMapping(params = "saveform", method = RequestMethod.POST)
    public ModelAndView saveform(final HttpServletRequest httpServletRequest) {
        bindModel(httpServletRequest);
        JsonViewObject respObj;
        Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
        Long empId = UserSession.getCurrent().getEmployee().getEmpId();
        Long langId = (long) UserSession.getCurrent().getLanguageId();
        String ipMacAddress = Utility.getClientIpAddress(httpServletRequest);
        String ulbName = UserSession.getCurrent().getOrganisation().getOrgShortNm();
        DataLegacyModel dataLegacyModel = this.getModel();
        
        ApplicationFormDto dto = dataLegacyModel.getApplicationformdto();
       List<Object[]> list=dataLegacyModel.getServiceList().parallelStream().filter(s->s[0].equals(dto.getSelectSchemeName())).collect(Collectors.toList());
       list.parallelStream().forEach(l->{
           dto.setServiceCode(l[2].toString());
       });
        dto.setOrgId(orgId);
        dto.setCreatedBy(empId);
        dto.setCreatedDate(new Date());
        dto.setLgIpMac(ipMacAddress);
        dto.setLangId(langId);
        dto.setUlbName(ulbName);
        
        dataLegacyModel.validateBean(dto, ApplicationFormValidator.class);
        
        if (dataLegacyModel.saveDataLegacyDetails()) {
        
        respObj = JsonViewObject.successResult(ApplicationSession.getInstance().getMessage(dataLegacyModel.getSuccessMessage()));
            
        } else {
            respObj = JsonViewObject
                    .successResult(ApplicationSession.getInstance().getMessage("social.sec.notsave.success"));

        }
        return new ModelAndView(new MappingJackson2JsonView(), MainetConstants.FORM_NAME, respObj);

    }
     
    
   

}
