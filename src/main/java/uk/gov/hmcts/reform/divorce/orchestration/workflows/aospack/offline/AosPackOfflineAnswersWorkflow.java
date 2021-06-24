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
import java.util.HashMap;
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
    private final RespondentAosOfflineNotification respondentAosOfflineNotification;
    private final CoRespondentAosOfflineNotification coRespondentAosOfflineNotification;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty) throws WorkflowException {
        final Map<String, Object> caseData = caseDetails.getCaseData();
        final String caseId = caseDetails.getCaseId();

        log.info("Processing AosPackOfflineAnswersWorkflow for Case ID: {}", caseId);

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        Map<String, Object> contextTransientObjects = new HashMap<>();

        addTasks(divorceParty, tasks, contextTransientObjects, caseDetails, authToken);

        // add the previous values for tasks last so they are preserved
        contextTransientObjects.put(CASE_ID_JSON_KEY, caseDetails.getCaseId());
        contextTransientObjects.put(AUTH_TOKEN_JSON_KEY, authToken);

        return execute(tasks.toArray(new Task[] {}), caseData,
            contextTransientObjects.entrySet().stream()
                .map(entry -> new ImmutablePair<>(entry.getKey(), entry.getValue())).toArray(ImmutablePair[]::new));
    }

    private void addTasks(DivorceParty divorceParty, List<Task<Map<String, Object>>> tasks, Map<String, Object> contextTransientObjects,
                          CaseDetails caseDetails, String authToken) throws WorkflowException {

        tasks.add(formFieldValuesToCoreFieldsRelay);

        if (isRespondent(divorceParty)) {
            tasks.add(respondentAosAnswersProcessor);
            tasks.add(respondentAosDerivedAddressFormatter);
            tasks.add(respondentAnswersGenerator);
            tasks.add(addNewDocumentsToCaseDataTask);
            // add notification tasks about offline respondent aos
            respondentAosOfflineNotification.addAOSEmailTasks(contextTransientObjects, tasks, caseDetails, authToken);
        }

        if (isCoRespondent(divorceParty)) {
            tasks.add(coRespondentAosAnswersProcessor);
            tasks.add(coRespondentAosDerivedAddressFormatter);
            // add notification tasks about offline co-respondent aos
            coRespondentAosOfflineNotification.addAOSEmailTasks(tasks, caseDetails);
        }

    }

    private boolean isRespondent(DivorceParty divorceParty) {
        return RESPONDENT.equals(divorceParty);
    }

    private boolean isCoRespondent(DivorceParty divorceParty) {
        return CO_RESPONDENT.equals(divorceParty);
    }

}