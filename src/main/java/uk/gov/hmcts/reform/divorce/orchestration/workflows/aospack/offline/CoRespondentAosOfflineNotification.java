package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoRespondentRespondedNotificationEmail;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@AllArgsConstructor
public class CoRespondentAosOfflineNotification {

    private final SendPetitionerCoRespondentRespondedNotificationEmail petitionerEmailTask;

    public void addAOSEmailTasks(final List<Task<Map<String, Object>>> tasks,
                                 CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();

        log.info("CaseId: {} Added petitioner notification email task about offline co-respondent AoS submission", caseId);
        tasks.add(petitionerEmailTask);
    }

}
