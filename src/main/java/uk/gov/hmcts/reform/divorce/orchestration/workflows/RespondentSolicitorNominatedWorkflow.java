package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
public class RespondentSolicitorNominatedWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final RespondentPinGenerator respondentPinGenerator;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final RespondentAosPackPrinterTask respondentAosPackPrinterTask;
    private final ModifyDueDate modifyDueDate;
    private final ResetRespondentLinkingFields resetRespondentLinkingFields;

    @Autowired
    public RespondentSolicitorNominatedWorkflow(RespondentPinGenerator respondentPinGenerator,
                                                RespondentLetterGenerator respondentLetterGenerator,
                                                CaseFormatterAddDocuments caseFormatterAddDocuments,
                                                FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore,
                                                RespondentAosPackPrinterTask respondentAosPackPrinterTask,
                                                ModifyDueDate modifyDueDate,
                                                ResetRespondentLinkingFields resetRespondentLinkingFields) {
        this.respondentPinGenerator = respondentPinGenerator;
        this.respondentLetterGenerator = respondentLetterGenerator;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
        this.fetchPrintDocsFromDmStore = fetchPrintDocsFromDmStore;
        this.respondentAosPackPrinterTask = respondentAosPackPrinterTask;
        this.modifyDueDate = modifyDueDate;
        this.resetRespondentLinkingFields = resetRespondentLinkingFields;
    }

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();
        final Map<String, Object> caseData = caseDetails.getCaseData();

        tasks.add(respondentPinGenerator);
        tasks.add(respondentLetterGenerator);
        tasks.add(caseFormatterAddDocuments);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.add(respondentAosPackPrinterTask);
        tasks.add(modifyDueDate);
        tasks.add(resetRespondentLinkingFields);

        return this.execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }
}
