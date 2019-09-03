package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentAosPackPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAosPackPrinter;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSolicitorAosInvitationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_EMAIL;

@Component
@RequiredArgsConstructor
@Slf4j
public class CcdCallbackBulkPrintWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ServiceMethodValidationTask serviceMethodValidationTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final RespondentAosPackPrinter respondentAosPackPrinter;
    private final CoRespondentAosPackPrinter coRespondentAosPackPrinter;
    private final RespondentPinGenerator respondentPinGenerator;
    private final SendRespondentSolicitorAosInvitationEmail respondentSolicitorAosEmailSender;
    private final ModifyDueDate modifyDueDate;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Value("${feature-toggle.toggle.feature_resp_solicitor_details}")
    private final boolean featureToggleRespSolicitor;

    public Map<String, Object> run(final CcdCallbackRequest ccdCallbackRequest, final String authToken) throws WorkflowException {
        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();

        final List<Task> tasks = new ArrayList<>();

        tasks.add(serviceMethodValidationTask);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.addAll(getRespondentAosCommunicationTasks(caseDetails.getCaseData()));
        tasks.add(coRespondentAosPackPrinter);
        tasks.add(modifyDueDate);

        return this.execute(tasks.toArray(new Task[tasks.size()]),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }

    private List<Task> getRespondentAosCommunicationTasks(final Map<String, Object> caseData) {
        if (featureToggleRespSolicitor) {
            final String solicitorEmail = (String) caseData.get(D8_RESPONDENT_SOLICITOR_EMAIL);
            if (isNotEmpty(solicitorEmail)) {
                return asList(
                    respondentPinGenerator,
                    respondentSolicitorAosEmailSender,
                    respondentLetterGenerator, // re-generate the AoS letter with the new PIN
                    caseFormatterAddDocuments);
            }
        }
        return singletonList(respondentAosPackPrinter);
    }
}
