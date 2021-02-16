package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_EMAIL_ADDRESS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerSolicitorFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isPetitionerRepresented;

@Component
@Slf4j
public class DaRequestedPetitionerSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {

    public DaRequestedPetitionerSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected boolean canEmailBeSent(Map<String, Object> caseData) {
        return isPetitionerRepresented(caseData);
    }

    @Override
    protected Map<String, String> getPersonalisation(TaskContext taskContext, Map<String, Object> caseData) {
        return new HashMap<>(ImmutableMap.of(
            NOTIFICATION_EMAIL_ADDRESS_KEY, getRecipientEmail(caseData),
            NOTIFICATION_PET_NAME, getPetitionerFullName(caseData),
            NOTIFICATION_RESP_NAME, getRespondentFullName(caseData),
            NOTIFICATION_SOLICITOR_NAME, getPetitionerSolicitorFullName(caseData),
            NOTIFICATION_CCD_REFERENCE_KEY, getCaseId(taskContext)
        ));
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return String.format(
            "%s vs %s: Decree absolute application submitted",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData)
        );
    }

    @Override
    public EmailTemplateNames getTemplate() {
        return EmailTemplateNames.DA_APPLICATION_HAS_BEEN_RECEIVED;
    }
}