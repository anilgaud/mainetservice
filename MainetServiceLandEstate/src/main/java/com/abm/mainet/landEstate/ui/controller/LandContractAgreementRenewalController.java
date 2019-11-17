package com.abm.mainet.landEstate.ui.controller;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.dto.JsonViewObject;
import com.abm.mainet.common.integration.dms.domain.AttachDocs;
import com.abm.mainet.common.integration.dms.service.IAttachDocsService;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.master.dto.ContractAgreementSummaryDTO;
import com.abm.mainet.common.master.dto.ContractMastDTO;
import com.abm.mainet.common.master.service.IContractAgreementService;
import com.abm.mainet.common.ui.controller.AbstractFormController;
import com.abm.mainet.common.utility.ApplicationContextProvider;
import com.abm.mainet.common.utility.ApplicationSession;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.landEstate.dto.LandContractAgreementRenewalDto;
import com.abm.mainet.landEstate.ui.model.LandContractAgreementRenewalModel;

@Controller
@RequestMapping(MainetConstants.LandEstate.LAND_CONTRACTRENEWAL_URL)

public class LandContractAgreementRenewalController extends AbstractFormController<LandContractAgreementRenewalModel>  {
	  @Autowired
	    private IFileUploadService fileUpload;
       
	  @Autowired
		private IContractAgreementService contractAgreementService;
	  
		@Resource
		private IAttachDocsService attachDocsService;
	
	 @RequestMapping(method = { RequestMethod.POST, RequestMethod.GET })
		public ModelAndView index(HttpServletRequest request)throws Exception{
		 sessionCleanup(request);
			this.getModel().bind(request);
			  final Long orgId = UserSession.getCurrent().getOrganisation().getOrgid();
			List<ContractAgreementSummaryDTO> contractNoList=ApplicationContextProvider.getApplicationContext().getBean(IContractAgreementService.class).getContractAgreementSummaryData(orgId, null, null, null, null, null);
			this.getModel().setContractNoList(contractNoList);  
			
			return index();
		}
	 
	    @RequestMapping(params = "form", method = RequestMethod.POST)
	    public ModelAndView form(final HttpServletRequest request)throws Exception {
	    	  
	        fileUpload.sessionCleanUpForFileUpload();
	        getModel().bind(request);
	    	LandContractAgreementRenewalModel model=this.getModel();
	    	LandContractAgreementRenewalDto landContractAgreementRenewalDto=model.getContactAgreementdto();
	    	
	    	model.setContractMastDTO(contractAgreementService.findById(landContractAgreementRenewalDto.getContractId(),UserSession.getCurrent().getOrganisation().getOrgid()));
	    	model.getContractMastDTO()
			.setContractType(CommonMasterUtility.findLookUpCode(PrefixConstants.CONTRACT_TYPE,
					(long)1,
					model.getContractMastDTO().getContType()));
		/* model.setModeType(MainetConstants.RnLCommon.MODE_EDIT); */

			final List<AttachDocs> attachDocs = attachDocsService.findByCode(
					(long)1,
					MainetConstants.CONTRACT + MainetConstants.DOUBLE_BACK_SLACE
							+ landContractAgreementRenewalDto.getContractId());
			this.getModel().setAttachDocsList(attachDocs);
	    	 return new ModelAndView("LandContractAgreementRenewalValidn", MainetConstants.FORM_NAME, model);
	   	
	    }
	    
	    @RequestMapping(params = "form1", method = RequestMethod.POST)
	    public ModelAndView form1(final HttpServletRequest request)throws Exception {
	    	  
	        fileUpload.sessionCleanUpForFileUpload();
	        getModel().bind(request);
	    	LandContractAgreementRenewalModel model=this.getModel();
	    	LandContractAgreementRenewalDto landContractAgreementRenewalDto=model.getContactAgreementdto();
	    	
	    	model.setContractMastDTO(contractAgreementService.findById(landContractAgreementRenewalDto.getContractId1(),UserSession.getCurrent().getOrganisation().getOrgid()));
	    	model.getContractMastDTO()
			.setContractType(CommonMasterUtility.findLookUpCode(PrefixConstants.CONTRACT_TYPE,
					(long)1,
					model.getContractMastDTO().getContType()));
		/* model.setModeType(MainetConstants.RnLCommon.MODE_EDIT); */

			final List<AttachDocs> attachDocs = attachDocsService.findByCode(
					(long)1,
					MainetConstants.CONTRACT + MainetConstants.DOUBLE_BACK_SLACE
							+ landContractAgreementRenewalDto.getContractId1());
			this.getModel().setAttachDocsList(attachDocs);
	    	 return new ModelAndView("LandContractAgreementRenewalValidn", MainetConstants.FORM_NAME, model);
	   	
	    }
	    
	    
		@RequestMapping(params = "saveForm", method = RequestMethod.POST)
		public ModelAndView saveForm(final HttpServletRequest httpServletRequest,
				final HttpServletResponse httpServletResponse) {
			bindModel(httpServletRequest);
			if (getModel().saveForm()) {
				if (getModel().getModeType().equals(MainetConstants.CommonConstants.C)) {
					return jsonResult(JsonViewObject.successResult(
							ApplicationSession.getInstance().getMessage(MainetConstants.ContractAgreement.CONTRACT_CREATE)
									+ MainetConstants.WHITE_SPACE + getModel().getContractMastDTO().getContNo()));
				} else if (getModel().getModeType().equals(MainetConstants.CommonConstants.E)) {
					return jsonResult(JsonViewObject.successResult(ApplicationSession.getInstance()
							.getMessage(MainetConstants.ContractAgreement.CONTRACT_UPDATE) + MainetConstants.WHITE_SPACE
							+ getModel().getContractMastDTO().getContNo() + MainetConstants.WHITE_SPACE
							+ ApplicationSession.getInstance().getMessage(MainetConstants.ContractAgreement.CONTRACT_MSG)));
				}
			}
			return defaultMyResult();
		}
		 


}
