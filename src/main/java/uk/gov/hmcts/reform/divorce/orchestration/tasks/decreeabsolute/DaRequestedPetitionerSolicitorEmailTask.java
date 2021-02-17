package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;
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
