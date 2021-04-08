package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.OrganisationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerSolicitorDigital;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateSelectedOrganisationTask implements Task<Map<String, Object>> {

    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final String authToken = getAuthToken(context);
        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        final String caseId = caseDetails.getCaseId();

        if (!isPetitionerSolicitorDigital(caseData)) {
            log.error("CaseId: {}, Petitioner org policy is not populated", caseId);
            throw new TaskException("Please select an organisation");
        }

        log.info("CaseId: {}, Petitioner solicitor organisation selected", caseId);
        assertUserBelongsToSelectedOrganisation(caseDetails, authToken);

        return caseData;
    }

    private void assertUserBelongsToSelectedOrganisation(CaseDetails caseDetails, String authToken) {
        final String caseId = caseDetails.getCaseId();

        OrganisationPolicy petitionerOrganisationPolicy = new ObjectMapper().convertValue(
            caseDetails.getCaseData().get(PETITIONER_SOLICITOR_ORGANISATION_POLICY),
            OrganisationPolicy.class
        );

        final String selectedOrgId = petitionerOrganisationPolicy.getOrganisation().getOrganisationID();

        log.info("CaseId: {}, Selected org id = {}", caseId, selectedOrgId);

        userBelongsToSelectedOrganisation(authToken, selectedOrgId, caseId);
    }

    private void userBelongsToSelectedOrganisation(String authToken, String selectedOrgId, String caseId) {
        String userOrgId;

        try {
            userOrgId = organisationClient
                .getMyOrganisation(authToken, authTokenGenerator.generate())
                .getOrganisationIdentifier();

        } catch (Exception exception) {
            log.error("CaseId: {}, problem with getting organisation details", caseId);
            throw new TaskException("PRD API call failed", exception);
        }

        log.info("CaseId: {}, User belongs to org id = {}", caseId, userOrgId);

        if (!selectedOrgId.equalsIgnoreCase(userOrgId)) {
            log.error("CaseId: {}, wrong organisation selected {} != {}", caseId, selectedOrgId, userOrgId);
            throw new TaskException("Please select an organisation you belong to");
        }

        log.info("CaseId: {}, User selected an org they belong to", caseId);
    }
}
