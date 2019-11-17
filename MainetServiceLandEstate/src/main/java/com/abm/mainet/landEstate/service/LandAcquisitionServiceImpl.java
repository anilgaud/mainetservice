package com.abm.mainet.landEstate.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebMethod;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abm.mainet.common.constant.MainetConstants;
import com.abm.mainet.common.constant.PrefixConstants;
import com.abm.mainet.common.constant.ServiceEndpoints;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.domain.ServiceMaster;
import com.abm.mainet.common.domain.TbTaxMasEntity;
import com.abm.mainet.common.dto.ApplicantDetailDTO;
import com.abm.mainet.common.exception.FrameworkException;
import com.abm.mainet.common.integration.asset.dto.AssetDetailsDTO;
import com.abm.mainet.common.integration.dms.service.IFileUploadService;
import com.abm.mainet.common.integration.dto.DocumentDetailsVO;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.master.service.TbTaxMasService;
import com.abm.mainet.common.service.ApplicationService;
import com.abm.mainet.common.service.CommonService;
import com.abm.mainet.common.service.ServiceMasterService;
import com.abm.mainet.common.utility.ApplicationContextProvider;
import com.abm.mainet.common.utility.CommonMasterUtility;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.utility.RestClient;
import com.abm.mainet.common.utility.Utility;
import com.abm.mainet.common.workflow.dto.ApplicationMetadata;
import com.abm.mainet.landEstate.dao.ILandAcquisitionDao;
import com.abm.mainet.landEstate.domain.LandAcquisition;
import com.abm.mainet.landEstate.dto.LandAcquisitionDto;
import com.abm.mainet.landEstate.repository.LandAcquisitionRepository;
import com.abm.mainet.smsemail.service.ISMSAndEmailService;

@Service
public class LandAcquisitionServiceImpl implements ILandAcquisitionService {

    private static Logger log = Logger.getLogger(LandAcquisitionServiceImpl.class);

    @Autowired
    LandAcquisitionRepository acqRepository;

    @Resource
    private IFileUploadService fileUploadService;

    @Autowired
    ILandAcquisitionDao iLandAcquisitionDao;

    @Autowired
    private CommonService commonService;

    @Autowired
    ServiceMasterService serviceMasterService;

    @Autowired
    private ISMSAndEmailService ismsAndEmailService;

    @Autowired
    ILandAcquisitionDao acquisitionDao;

    @Transactional
    public LandAcquisitionDto saveLandAcquisition(LandAcquisitionDto acquisitionDto, ServiceMaster serviceMaster,
            List<DocumentDetailsVO> checkList, RequestDTO requestDTO) {

        try {
            Long appicationId = null;

            appicationId = ApplicationContextProvider.getApplicationContext().getBean(ApplicationService.class)
                    .createApplication(requestDTO);

            LandAcquisition acquisition = new LandAcquisition();
            acquisitionDto.setApmApplicationId(appicationId);
            BeanUtils.copyProperties(acquisitionDto, acquisition);

            acqRepository.save(acquisition);

            requestDTO.setIdfId("LAQ" + MainetConstants.WINDOWS_SLASH + acquisition.getLnaqId());

            if ((checkList != null) && !checkList.isEmpty()) {
                // fileUploadService.doFileUpload(checkList, requestDTO);
                fileUploadService.doMasterFileUpload(checkList, requestDTO);
            }

        } catch (final Exception ex) {
            throw new FrameworkException("error when save acquisition ", ex);

        }

        return acquisitionDto;

    }

    @Override
    public LandAcquisitionDto getAcquisitionChargesFromBrmsRule(LandAcquisitionDto acqDto) {
        Organisation organisation = new Organisation();
        organisation.setOrgid(acqDto.getOrgId());
        Date todayDate = new Date();
        final LookUp chargeApplicableAt = CommonMasterUtility.getValueFromPrefixLookUp(
                MainetConstants.ChargeApplicableAt.APPLICATION,
                PrefixConstants.LookUpPrefix.CAA, organisation);

        ServiceMaster sm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
                .getServiceMasterByShortCode(
                        MainetConstants.LandEstate.LandAcquisition.SERVICE_SHOT_CODE,
                        acqDto.getOrgId());

        final List<TbTaxMasEntity> taxesMaster = ApplicationContextProvider.getApplicationContext().getBean(TbTaxMasService.class)
                .fetchAllApplicableServiceCharge(sm.getSmServiceId(),
                        organisation.getOrgid(),
                        chargeApplicableAt.getLookUpId());

        /*
         * List<MLNewTradeLicense> masterList = new ArrayList<>(); if (masDto.getTrdId() != null) { final LookUp
         * chargeApplicableAtScrutiny = CommonMasterUtility.getValueFromPrefixLookUp(
         * PrefixConstants.NewWaterServiceConstants.SCRUTINY, PrefixConstants.NewWaterServiceConstants.CAA, organisation);
         * ServiceMaster smm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
         * .getServiceMasterByShortCode( MainetConstants.TradeLicense.SERVICE_SHORT_CODE, masDto.getOrgid()); final
         * List<TbTaxMasEntity> taxesMasterr = ApplicationContextProvider.getApplicationContext() .getBean(TbTaxMasService.class)
         * .fetchAllApplicableServiceCharge(smm.getSmServiceId(), organisation.getOrgid(),
         * chargeApplicableAtScrutiny.getLookUpId()); List<TradeLicenseItemDetailDTO> tradeLicenseItemDetailDTO =
         * masDto.getTradeLicenseItemDetailDTO(); List<LookUp> lookupListLevel1 = new ArrayList<LookUp>(); List<LookUp>
         * lookupListLevel2 = new ArrayList<LookUp>(); List<LookUp> lookupListLevel3 = new ArrayList<LookUp>(); List<LookUp>
         * lookupListLevel4 = new ArrayList<LookUp>(); List<LookUp> lookupListLevel5 = new ArrayList<LookUp>(); try {
         * lookupListLevel1 = CommonMasterUtility.getNextLevelData(MainetConstants.TradeLicense.ITC, 1, masDto.getOrgid());
         * lookupListLevel2 = CommonMasterUtility.getNextLevelData(MainetConstants.TradeLicense.ITC, 2, masDto.getOrgid());
         * lookupListLevel3 = CommonMasterUtility.getNextLevelData(MainetConstants.TradeLicense.ITC, 3, masDto.getOrgid());
         * lookupListLevel4 = CommonMasterUtility.getNextLevelData(MainetConstants.TradeLicense.ITC, 4, masDto.getOrgid());
         * lookupListLevel5 = CommonMasterUtility.getNextLevelData(MainetConstants.TradeLicense.ITC, 5, masDto.getOrgid()); }
         * catch (Exception e) { LOGGER.info("prefix level not found"); } //
         * tradeLicenseItemDetailDTO.parallelStream().forEach(dto -> { for (TradeLicenseItemDetailDTO dto :
         * tradeLicenseItemDetailDTO) { MLNewTradeLicense license = new MLNewTradeLicense(); license.setOrgId(dto.getOrgid());
         * license.setServiceCode(MainetConstants.TradeLicense.SERVICE_CODE);
         * license.setTaxType(MainetConstants.TradeLicense.TAX_TYPE); license.setTaxCode(taxesMasterr.get(0).getTaxCode()); // for
         * getting scrutiny level tax code(category and // subcategory)
         * license.setTaxCategory(CommonMasterUtility.getHierarchicalLookUp(taxesMasterr.get(0).getTaxCategory1(),
         * organisation).getDescLangFirst());
         * license.setTaxSubCategory(CommonMasterUtility.getHierarchicalLookUp(taxesMasterr.get(0).getTaxCategory2(),
         * organisation).getDescLangFirst()); license.setRateStartDate(todayDate.getTime());
         * license.setDeptCode(MainetConstants.TradeLicense.MARKET_LICENSE);
         * license.setApplicableAt(chargeApplicableAtScrutiny.getDescLangFirst()); if (dto.getTriCod1() != null &&
         * dto.getTriCod1() != 0) { List<LookUp> level1 = lookupListLevel1.parallelStream() .filter(clList -> clList != null &&
         * clList.getLookUpId() == dto.getTriCod1().longValue()) .collect(Collectors.toList()); if (level1 != null &&
         * !level1.isEmpty()) { license.setItemCategory1(level1.get(0).getDescLangFirst());
         * dto.setItemCategory1(level1.get(0).getDescLangFirst()); } } else {
         * dto.setItemCategory1(MainetConstants.TradeLicense.NOT_APPLICABLE); } if (dto.getTriCod2() != null && dto.getTriCod2()
         * != 0) { List<LookUp> level2 = lookupListLevel2.parallelStream() .filter(clList -> clList != null &&
         * clList.getLookUpId() == dto.getTriCod2().longValue()) .collect(Collectors.toList()); if (level2 != null &&
         * !level2.isEmpty()) { license.setItemCategory2(level2.get(0).getDescLangFirst());
         * dto.setItemCategory2(level2.get(0).getDescLangFirst()); } } else {
         * dto.setItemCategory2(MainetConstants.TradeLicense.NOT_APPLICABLE); } if (dto.getTriCod3() != null && dto.getTriCod3()
         * != 0) { List<LookUp> level3 = lookupListLevel3.parallelStream() .filter(clList -> clList != null &&
         * clList.getLookUpId() == dto.getTriCod3().longValue()) .collect(Collectors.toList()); if (level3 != null &&
         * !level3.isEmpty()) { license.setItemCategory3(level3.get(0).getDescLangFirst());
         * dto.setItemCategory2(level3.get(0).getDescLangFirst()); } } else {
         * dto.setItemCategory3(MainetConstants.TradeLicense.NOT_APPLICABLE); } if (dto.getTriCod4() != null && dto.getTriCod4()
         * != 0) { List<LookUp> level4 = lookupListLevel4.parallelStream() .filter(clList -> clList != null &&
         * clList.getLookUpId() == dto.getTriCod4().longValue()) .collect(Collectors.toList()); if (level4 != null &&
         * !level4.isEmpty()) { license.setItemCategory4(level4.get(0).getDescLangFirst());
         * dto.setItemCategory4(level4.get(0).getDescLangFirst()); } } else {
         * dto.setItemCategory4(MainetConstants.TradeLicense.NOT_APPLICABLE); } if (dto.getTriCod5() != null && dto.getTriCod5()
         * != 0) { List<LookUp> level5 = lookupListLevel5.parallelStream() .filter(clList -> clList != null &&
         * clList.getLookUpId() == dto.getTriCod5().longValue()) .collect(Collectors.toList()); if (level5 != null &&
         * !level5.isEmpty()) { license.setItemCategory5(level5.get(0).getDescLangFirst());
         * dto.setItemCategory5(level5.get(0).getDescLangFirst()); } } else {
         * dto.setItemCategory5(MainetConstants.TradeLicense.NOT_APPLICABLE); } masterList.add(license); // }); } } else { for
         * (TbTaxMasEntity taxes : taxesMaster) { MLNewTradeLicense license = new MLNewTradeLicense();
         * license.setOrgId(masDto.getOrgid()); license.setServiceCode(MainetConstants.TradeLicense.SERVICE_CODE);
         * license.setTaxType(MainetConstants.TradeLicense.TAX_TYPE); license.setTaxCode(taxes.getTaxCode()); //
         * license.setTaxCategory(taxes.getTaxCategory1().toString()); //
         * license.setTaxSubCategory(taxes.getTaxCategory2().toString()); settingTaxCategories(license, taxes, organisation);
         * license.setRateStartDate(todayDate.getTime()); license.setApplicableAt(chargeApplicableAt.getDescLangFirst());
         * license.setDeptCode(MainetConstants.TradeLicense.MARKET_LICENSE); masterList.add(license); } }
         * LOGGER.info("brms ML getTradeLicenceChargesFromBrmsRule execution start.."); WSResponseDTO responseDTO = new
         * WSResponseDTO(); WSRequestDTO wsRequestDTO = new WSRequestDTO(); List<MLNewTradeLicense> master = new ArrayList<>();
         * wsRequestDTO.setDataModel(masterList); try { LOGGER.info("brms ML request DTO  is :" + wsRequestDTO.toString());
         * responseDTO = RestClient.callBRMS(wsRequestDTO, ServiceEndpoints.ML_NEW_TRADE_LICENSE); if
         * (responseDTO.getWsStatus().equals(MainetConstants.WebServiceStatus.SUCCESS)) { master =
         * setTradeLicenceChargesDTO(responseDTO); } else { throw new FrameworkException(responseDTO.getErrorMessage()); } } catch
         * (Exception ex) { throw new FrameworkException("unable to process request for Trade Licence Fee", ex); }
         * LOGGER.info("brms ML getTradeLicenceChargesFromBrmsRule execution End."); setChargeToItemsDtoList(master, masDto); for
         * (TbTaxMasEntity tbTaxMas : taxesMaster) { masDto.getFeeIds().put(tbTaxMas.getTaxId(),
         * masDto.getTotalApplicationFee().doubleValue()); } return masDto;
         */
        return null;
    }

    @Override
    public LandAcquisitionDto getLandAcqDataByApplicationId(Long applicationId, Long orgId) {

        LandAcquisitionDto landAcquisitionDto = new LandAcquisitionDto();
        // get record using JPA repository
        LandAcquisition laqEntity = acqRepository.getLAQDataByRefNoAndOrgId(applicationId, orgId);
        BeanUtils.copyProperties(laqEntity, landAcquisitionDto);
        return landAcquisitionDto;
    }

    // for duplicate
    @Override
    public int checkDuplicateLand(String lnServno, String lnArea, Long locId, String payTo) {
        // get record using JPA repository
        int laqData = acqRepository.getLandAcquisitiondata(lnServno, lnArea, locId, payTo);
        return laqData;
    }

    @Override
    public List<String> fetchProposalNoList() {
        return acqRepository.fetchProposalNoList();
    }

    @Override
    public List<LandAcquisitionDto> fetchSearchData(String proposalNo, String payTo, String acqStatus, Long locId,
            Long orgid) {
        List<LandAcquisitionDto> landAcquisitionDtos = new ArrayList<LandAcquisitionDto>();
        List<LandAcquisition> acquisitionList = iLandAcquisitionDao.searchLandAcquisitionData(proposalNo, payTo, acqStatus,
                locId, orgid);
        acquisitionList.forEach(acquisition -> {
            LandAcquisitionDto dto = new LandAcquisitionDto();
            BeanUtils.copyProperties(acquisition, dto);
            // Acquisition Status
            if (acquisition.getAcqStatus().equalsIgnoreCase("A")) {
                dto.setAcqStatus("ACQUIRED");
            } else {
                dto.setAcqStatus("TRANSIT");
            }
            dto.setAcqDateDesc(Utility.dateToString(acquisition.getAcqDate()));
            landAcquisitionDtos.add(dto);
        });
        return landAcquisitionDtos;
    }

    @Override
    @Transactional
    @WebMethod(exclude = true)
    public Map<Long, Double> getLoiCharges(final Long applicationId, final Long serviceId, final Long orgId)
            throws CloneNotSupportedException {
        Map<Long, Double> chargeMap = new HashMap<>();
        LandAcquisitionDto dto = getLandAcqDataByApplicationId(applicationId, orgId);
        Organisation organisation = new Organisation();
        organisation.setOrgid(orgId);
        double amount = 0;
        if (dto.getAcqCost() != null) {
            amount = dto.getAcqCost().doubleValue();
        }
        // get Tax id

        final LookUp chargeApplicableAt = CommonMasterUtility.getValueFromPrefixLookUp(
                MainetConstants.ChargeApplicableAt.SCRUTINY,
                PrefixConstants.LookUp.CHARGE_MASTER_CAA, organisation);

        ServiceMaster sm = ApplicationContextProvider.getApplicationContext().getBean(ServiceMasterService.class)
                .getServiceMasterByShortCode(
                        MainetConstants.LandEstate.LandAcquisition.SERVICE_SHOT_CODE,
                        orgId);

        final List<TbTaxMasEntity> taxesMaster = ApplicationContextProvider.getApplicationContext().getBean(TbTaxMasService.class)
                .fetchAllApplicableServiceCharge(sm.getSmServiceId(),
                        organisation.getOrgid(),
                        chargeApplicableAt.getLookUpId());
        chargeMap.put(taxesMaster.get(0).getTaxId(), amount);

        return chargeMap;
    }

    @Transactional
    public void updateLandValuationData(Long apmApplicationId, BigDecimal acqCost, Long vendorId, String billNo, Long assetId,
            String transferStatus,
            Long orgId) {
        // update cost and vendorId against applicationId
        acquisitionDao.updateLandValuationData(apmApplicationId, acqCost, vendorId, billNo, assetId, transferStatus, orgId);

    }

    @Override
    @Transactional
    public Long pushAssetDetails(AssetDetailsDTO astDet) {
        Long astId = null;
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = RestClient.callRestTemplateClient(astDet, ServiceEndpoints.WMS_ASSET_DETAILS);
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                astId = Long.valueOf(responseEntity.getBody().toString());
            }
        } catch (Exception ex) {
            log.error("Exception occured while pushAssetDetails() : " + ex);
            return astId;
        }
        return astId;
    }

    @Transactional
    public void initiateWorkflowForFreeService(ApplicationMetadata applicationData, ApplicantDetailDTO applicantDetailDto) {
        commonService.initiateWorkflowfreeService(applicationData, applicantDetailDto);
    }

    @Transactional
    public void updateLandProposalAcqStatusById(Long updatedBy, String lgIpMacUpd, Long apmApplicationId) {
        // HERE A -> Acquired
        acqRepository.updateLandProposalAcqStatus("A", updatedBy, lgIpMacUpd, apmApplicationId);

    }

}
