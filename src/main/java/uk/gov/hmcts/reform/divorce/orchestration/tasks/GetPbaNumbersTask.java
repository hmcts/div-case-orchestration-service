package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PbaValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.PBAOrganisationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.isSolicitorPaymentMethodPba;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetPbaNumbersTask implements Task<Map<String, Object>> {

    private final PbaValidationClient pbaValidationClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final IdamClient idamClient;
    private final AuthUtil authUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        if (isSolicitorPaymentMethodPba(caseData)) {
            String caseId = getCaseId(context);
            String bearerAuthToken = authUtil.getBearerToken(getAuthToken(context));
            String solicitorEmail = idamClient.getUserDetails(bearerAuthToken).getEmail();

            log.info("CaseId: {}. About to retrieve PBA numbers for solicitor", caseId);

            List<String> pbaNumbers = pbaNumbersFor(solicitorEmail, bearerAuthToken);

            if (CollectionUtils.isEmpty(pbaNumbers)) {
                log.info("CaseId: {}. No PBA numbers found for this solicitor", caseId);
                caseData.remove(PBA_NUMBERS); // Ensures previously retrieved PBA numbers are not used
            } else {
                log.info("CaseId: {}. Successfully retrieved {} PBA numbers for solicitor", caseId, pbaNumbers.size());
                caseData.put(PBA_NUMBERS, asDynamicList(pbaNumbers));
            }
        }

        return caseData;
    }

    private List<String> pbaNumbersFor(String email, String bearerAuthToken) {
        ResponseEntity<PBAOrganisationResponse> responseEntity = pbaValidationClient.retrievePbaNumbers(
            bearerAuthToken,
            serviceAuthGenerator.generate(),
            email
        );

        PBAOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());
        return pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();
    }
}
