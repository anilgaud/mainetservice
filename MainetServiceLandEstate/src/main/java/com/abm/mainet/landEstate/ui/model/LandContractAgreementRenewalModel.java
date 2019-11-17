package com.abm.mainet.landEstate.ui.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.abm.mainet.common.integration.dms.domain.AttachDocs;
import com.abm.mainet.common.master.dto.ContractAgreementSummaryDTO;
import com.abm.mainet.common.master.dto.ContractDetailDTO;
import com.abm.mainet.common.master.dto.ContractMastDTO;
import com.abm.mainet.common.master.service.IContractAgreementService;
import com.abm.mainet.common.ui.model.AbstractFormModel;
import com.abm.mainet.common.utility.UserSession;
import com.abm.mainet.landEstate.dto.LandContractAgreementRenewalDto;

@Component
@Scope("session")
public class LandContractAgreementRenewalModel extends AbstractFormModel{
	
	@Autowired
	private IContractAgreementService ContractAgreementService;

	private static final long serialVersionUID = -8645649221957987206L;
	
	private List<ContractAgreementSummaryDTO> contractNoList=null;
	private List<Object[]> vendorList=null;
	LandContractAgreementRenewalDto  contactAgreementdto=null;
	ContractMastDTO contractMastDTO=null;
	  private String saveMode;
	  private String modeType;
	  private String removeCommonFileById;
		private List<AttachDocs> attachDocsList = new ArrayList<>();
		private Map<String, File> UploadMap = new HashMap<>();
		
	public String getSaveMode() {
		return saveMode;
	}

	public void setSaveMode(String saveMode) {
		this.saveMode = saveMode;
	}

	public List<ContractAgreementSummaryDTO> getContractNoList() {
		return contractNoList;
	}

	public void setContractNoList(List<ContractAgreementSummaryDTO> contractNoList) {
		this.contractNoList = contractNoList;
	}

	public LandContractAgreementRenewalDto getContactAgreementdto() {
		return contactAgreementdto;
	}

	public void setContactAgreementdto(LandContractAgreementRenewalDto contactAgreementdto) {
		this.contactAgreementdto = contactAgreementdto;
	}

	public List<Object[]> getVendorList() {
		return vendorList;
	}

	public void setVendorList(List<Object[]> vendorList) {
		this.vendorList = vendorList;
	}


	public String getModeType() {
		return modeType;
	}

	public void setModeType(String modeType) {
		this.modeType = modeType;
	}

	public ContractMastDTO getContractMastDTO() {
		return contractMastDTO;
	}

	public void setContractMastDTO(ContractMastDTO contractMastDTO) {
		this.contractMastDTO = contractMastDTO;
	}

	public String getRemoveCommonFileById() {
		return removeCommonFileById;
	}

	public void setRemoveCommonFileById(String removeCommonFileById) {
		this.removeCommonFileById = removeCommonFileById;
	}

	public List<AttachDocs> getAttachDocsList() {
		return attachDocsList;
	}

	public void setAttachDocsList(List<AttachDocs> attachDocsList) {
		this.attachDocsList = attachDocsList;
	}
	
	
	
	public Map<String, File> getUploadMap() {
		return UploadMap;
	}

	public void setUploadMap(Map<String, File> uploadMap) {
		UploadMap = uploadMap;
	}

	@Override
	public boolean saveForm() {
		boolean result = false;
		final ContractMastDTO contractMastDTO = getContractMastDTO();
	     List<ContractDetailDTO> contractDetailList= contractMastDTO.getContractDetailList();
	     LandContractAgreementRenewalDto agreementRenewalDto=getContactAgreementdto();
	     contractDetailList.get(0).setContToDate(agreementRenewalDto.getToDate());
		contractMastDTO.setContractDetailList(contractDetailList);
		
			ContractMastDTO mastDTO = ContractAgreementService.saveContractAgreement(contractMastDTO,
					UserSession.getCurrent().getOrganisation().getOrgid(), UserSession.getCurrent().getLanguageId(),
					UserSession.getCurrent().getEmployee().getEmpId(), getModeType(), UploadMap);
			result = true;
		
		return result;
	}

	
	
	
	
	

}
