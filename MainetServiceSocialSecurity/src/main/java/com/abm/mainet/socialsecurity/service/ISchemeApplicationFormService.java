package com.abm.mainet.socialsecurity.service;

import java.util.List;

import javax.jws.WebService;

import com.abm.mainet.common.domain.Employee;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.utility.LookUp;
import com.abm.mainet.common.workflow.dto.WorkflowTaskAction;
import com.abm.mainet.socialsecurity.ui.dto.ApplicationFormDto;

@WebService
public interface ISchemeApplicationFormService {

    public List<LookUp> FindSecondLevelPrefixByFirstLevelPxCode(Long orgId, String parentPx, Long ParentpxId, Long level);

    public ApplicationFormDto saveApplicationDetails(final ApplicationFormDto dto);

    public ApplicationFormDto prepareAndSaveApplicationMaster(ApplicationFormDto dto);

    public void initiateWorkFlowForFreeService(ApplicationFormDto dto);

    public ApplicationFormDto findApplicationdetails(Long applicationId, Long orgId);

    public boolean saveDecision(ApplicationFormDto applicationformdto, Long orgId, Employee emp,
            WorkflowTaskAction workFlowActionDto, RequestDTO requestDto);

    // newly added for data legacy form save

    public ApplicationFormDto saveDataLegacyFormDetails(final ApplicationFormDto dto);

    String getbeneficiarynumber(String ulbName, String serviceSortCode, Long orgId);

    Long getDeptIdByServiceShortName(Long orgId);

    List<Object[]> getSchemeName(ApplicationFormDto dto);

}
