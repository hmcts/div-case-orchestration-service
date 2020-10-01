package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PbaValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.PBAOrganisationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.asDynamicList;

@Component
@Slf4j
public class GetPbaNumbersTask implements Task<Map<String, Object>> {

    private final PbaValidationClient pbaValidationClient;
    private final AuthTokenGenerator serviceAuthGenerator;
    private final IdamClient idamClient;

    @Autowired
    public GetPbaNumbersTask(PbaValidationClient pbaValidationClient,
                             AuthTokenGenerator serviceAuthGenerator,
                             IdamClient idamClient) {
        this.pbaValidationClient = pbaValidationClient;
        this.serviceAuthGenerator = serviceAuthGenerator;
        this.idamClient = idamClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        if (solicitorPayByAccount((String) caseData.get(SOLICITOR_HOW_TO_PAY_JSON_KEY))) {
            String caseId = context.getTransientObject(CASE_ID_JSON_KEY);
            String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
            String solicitorEmail = idamClient.getUserDetails(authToken).getEmail();

            log.info("CaseId: {}. About to retrieve PBA numbers for solicitor", caseId);

            List<String> pbaNumbers = pbaNumbersFor(solicitorEmail, context);

            if (CollectionUtils.isEmpty(pbaNumbers)) {
                log.info("CaseId: {}. No PBA numbers found for this solicitor", caseId);
                caseData.remove(PBA_NUMBERS); // Ensures previously retrieved PBA numbers are not used
            } else {
                log.info("CaseId: {}. Successfully retrieved PBA numbers for solicitor", caseId);
                caseData.put(PBA_NUMBERS, asDynamicList(pbaNumbers));
            }
        }

        return caseData;
    }

    private List<String> pbaNumbersFor(String email, TaskContext context) {
        ResponseEntity<PBAOrganisationResponse> responseEntity = pbaValidationClient.retrievePbaNumbers(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString(),
            serviceAuthGenerator.generate(),
            email
        );

        PBAOrganisationResponse pbaOrganisationResponse = Objects.requireNonNull(responseEntity.getBody());
        return pbaOrganisationResponse.getOrganisationEntityResponse().getPaymentAccount();
    }

    private boolean solicitorPayByAccount(String howPay) {
        return Optional.ofNullable(howPay)
                .map(i -> i.equals(FEE_PAY_BY_ACCOUNT))
                .orElse(false);
    }
}
