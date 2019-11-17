package com.abm.mainet.socialsecurity.service;

import java.util.List;

import com.abm.mainet.common.domain.Employee;
import com.abm.mainet.common.domain.Organisation;
import com.abm.mainet.common.integration.dto.RequestDTO;
import com.abm.mainet.common.workflow.dto.WorkflowTaskAction;
import com.abm.mainet.socialsecurity.ui.dto.BeneficiaryPaymentOrderDto;

public interface IBeneficiaryPaymentOrderService {

    List<BeneficiaryPaymentOrderDto> filterSearchData(final Long serviceId, final Long paymentscheId, final Long year,
            final Long month, final Long orgId);

    void accountBillEntryforSocialSecurity(BeneficiaryPaymentOrderDto dto, Organisation org);

    void saveBeneficiaryDetails(List<BeneficiaryPaymentOrderDto> dtoList);

    void initiateWorkFlowForFreeService(BeneficiaryPaymentOrderDto dto);

    BeneficiaryPaymentOrderDto getViewDataFromRtgsPayment(Long orgId, String applicationId);

    boolean saveDecision(BeneficiaryPaymentOrderDto bpoDto, Long orgId, Employee emp, WorkflowTaskAction workFlowActionDto,
            RequestDTO requestDTO, Organisation org);

    Boolean checkAccountActiveOrNot();

    Boolean checkWorkflowIsNotDefine(Organisation org, Long orgId, String serviceCode);

}
