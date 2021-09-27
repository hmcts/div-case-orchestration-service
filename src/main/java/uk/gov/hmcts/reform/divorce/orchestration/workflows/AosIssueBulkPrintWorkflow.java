package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AosPackDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ServiceMethodValidationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.UpdateNoticeOfProceedingsDetailsTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.helper.RepresentedRespondentJourneyHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigital;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosIssueBulkPrintWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final ServiceMethodValidationTask serviceMethodValidationTask;
    private final FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    private final RespondentAosPackPrinterTask respondentAosPackPrinterTask;
    private final CoRespondentAosPackPrinterTask coRespondentAosPackPrinterTask;
    private final AosPackDueDateSetterTask aosPackDueDateSetterTask;
    private final UpdateNoticeOfProceedingsDetailsTask updateNoticeOfProceedingsDetailsTask;

    private final CaseDataUtils caseDataUtils;
    private final RepresentedRespondentJourneyHelper representedRespondentJourneyHelper;

    public Map<String, Object> run(final String authToken, CaseDetails caseDetails) throws WorkflowException {
        final List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(serviceMethodValidationTask);
        tasks.add(fetchPrintDocsFromDmStoreTask);

        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        if (!isRespondentSolicitorDigital(caseData)) {
            log.info("Case id {}: Sending AOS pack to bulk print, for non digital respondent solicitor", caseId);
            respondentAosPackPrinterTask.setDocumentTypesToPrint(asList(DOCUMENT_TYPE_PETITION));
        } else {
            log.info("Case id {}: Sending AOS pack to bulk print, for respondent", caseId);
        }

        tasks.add(respondentAosPackPrinterTask);

        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            tasks.add(coRespondentAosPackPrinterTask);
        }

        tasks.add(aosPackDueDateSetterTask);

        if (representedRespondentJourneyHelper.shouldUpdateNoticeOfProceedingsDetails(caseData)) {
            log.info("CaseId: {} adding updateNoticeOfProceedingsDetailsTask", caseId);
            tasks.add(updateNoticeOfProceedingsDetailsTask);
        }

        return this.execute(tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(CASE_STATE_JSON_KEY, caseDetails.getState())
        );
    }

}