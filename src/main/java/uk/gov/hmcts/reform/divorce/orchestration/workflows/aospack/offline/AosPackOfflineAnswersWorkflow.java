package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentAnswersGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.CoRespondentAosDerivedAddressFormatterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.FormFieldValuesToCoreFieldsRelayTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosAnswersProcessorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline.RespondentAosDerivedAddressFormatterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class AosPackOfflineAnswersWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final RespondentAosAnswersProcessorTask respondentAosAnswersProcessor;
    private final FormFieldValuesToCoreFieldsRelayTask formFieldValuesToCoreFieldsRelay;
    private final CoRespondentAosAnswersProcessorTask coRespondentAosAnswersProcessor;
    private final CoRespondentAosDerivedAddressFormatterTask coRespondentAosDerivedAddressFormatter;
    private final RespondentAosDerivedAddressFormatterTask respondentAosDerivedAddressFormatter;
    private final RespondentAnswersGenerator respondentAnswersGenerator;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        Task[] tasks = getTasks(divorceParty);
        log.info("Processing AosPackOfflineAnswersWorkflow for Case ID: {}", caseId);
        return execute(tasks, caseData, ImmutablePair.of(CASE_ID_JSON_KEY, caseId), ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken));
    }

    private Task[] getTasks(DivorceParty divorceParty) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(formFieldValuesToCoreFieldsRelay);

        if (isRespondent(divorceParty)) {
            tasks.add(respondentAosAnswersProcessor);
            tasks.add(respondentAosDerivedAddressFormatter);
            tasks.add(respondentAnswersGenerator);
            tasks.add(addNewDocumentsToCaseDataTask);
        }

        if (isCoRespondent(divorceParty)) {
            tasks.add(coRespondentAosAnswersProcessor);
            tasks.add(coRespondentAosDerivedAddressFormatter);
        }

        return tasks.toArray(new Task[] {});
    }

    private boolean isRespondent(DivorceParty divorceParty) {
        return RESPONDENT.equals(divorceParty);
    }

    private boolean isCoRespondent(DivorceParty divorceParty) {
        return CO_RESPONDENT.equals(divorceParty);
    }

}