package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentSolicitorAosInvitationEmail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@Component
@Slf4j
public class RespondentSolicitorNominatedWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final RespondentPinGenerator respondentPinGenerator;
    private final SendRespondentSolicitorAosInvitationEmail sendRespondentSolicitorNotificationEmail;
    private final ResetRespondentLinkingFields resetRespondentLinkingFields;

    @Autowired
    public RespondentSolicitorNominatedWorkflow(RespondentPinGenerator respondentPinGenerator,
                                                SendRespondentSolicitorAosInvitationEmail sendRespondentSolicitorAosInvitationEmail,
                                                ResetRespondentLinkingFields resetRespondentLinkingFields) {
        this.respondentPinGenerator = respondentPinGenerator;
        this.sendRespondentSolicitorNotificationEmail = sendRespondentSolicitorAosInvitationEmail;
        this.resetRespondentLinkingFields = resetRespondentLinkingFields;
    }

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();

        final Map<String, Object> caseData = caseDetails.getCaseData();

        tasks.add(respondentPinGenerator);
        tasks.add(sendRespondentSolicitorNotificationEmail);
        tasks.add(resetRespondentLinkingFields);

        return this.execute(
            tasks.toArray(new Task[tasks.size()]),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId())
        );
    }
}
