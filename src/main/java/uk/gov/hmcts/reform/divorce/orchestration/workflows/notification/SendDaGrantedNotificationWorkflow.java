package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedDocumentsToBulkPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@RequiredArgsConstructor
public class SendDaGrantedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmail;
    private final DocumentGenerationTask documentGenerationTask;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final SendDaGrantedDocumentsToBulkPrintTask sendDaGrantedDocumentsToBulkPrint;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        List<Task> taskList = new ArrayList<>();
        if (isDigitalProcess(caseData)) {
            taskList.add(sendDaGrantedNotificationEmail);
        } else {
            taskList.add(documentGenerationTask);
            taskList.add(caseFormatterAddDocuments);
            taskList.add(sendDaGrantedDocumentsToBulkPrint);
        }

        Task[] taskArr = new Task[taskList.size()];
        return this.execute(
            taskList.toArray(taskArr),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_TYPE, DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, DECREE_ABSOLUTE_LETTER_TEMPLATE_ID),
            ImmutablePair.of(DOCUMENT_FILENAME, DECREE_ABSOLUTE_LETTER_FILENAME)
        );
    }

    public boolean isDigitalProcess(Map<String, Object> caseData) {
        String respContactMethodIsDigital = (String) caseData.get(RESP_IS_USING_DIGITAL_CHANNEL);
        return YES_VALUE.equalsIgnoreCase(respContactMethodIsDigital);
    }
}
