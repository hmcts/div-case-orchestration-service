package uk.gov.hmcts.reform.divorce.orchestration.tasks.decreeabsolute;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.getRepresentedSubject;
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
        // you need to create a hashmap where
        // key is name of template var
        // value is retrieved from case data
        return new HashMap<>();
    }

    @Override
    protected String getSubject(TaskContext context, Map<String, Object> caseData) {
        return "Pet Sol applied for DA";
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.DA_APPLICATION_HAS_BEEN_RECEIVED;
    }
}
