package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCertificateOfEntitlementNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCertificateOfEntitlementNotificationEmail;

import java.util.Map;

@Component
@Slf4j
public class CaseLinkedForHearingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String CASE_ID_KEY = "caseId";

    @Autowired
    private SendPetitionerCertificateOfEntitlementNotificationEmail sendPetitionerCertificateOfEntitlementNotificationEmail;

    @Autowired
    private SendRespondentCertificateOfEntitlementNotificationEmail sendRespondentCertificateOfEntitlementNotificationEmail;

    @Autowired
    SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;

    public Map<String, Object> run(CaseDetails caseDetails) throws WorkflowException {

        Map<String, Object> returnedPayload = this.execute(
                new Task[]{
                    sendPetitionerCertificateOfEntitlementNotificationEmail,
                    sendRespondentCertificateOfEntitlementNotificationEmail,
                    sendCoRespondentGenericUpdateNotificationEmail
                },
            caseDetails.getCaseData(),
            ImmutablePair.of(CASE_ID_KEY, caseDetails.getCaseId()));

        log.info("Running CaseLinkedForHearingWorkflow for case id {}.", caseDetails.getCaseId());

        return returnedPayload;
    }
}
