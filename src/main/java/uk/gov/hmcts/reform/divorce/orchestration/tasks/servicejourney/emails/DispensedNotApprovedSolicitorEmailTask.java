package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.PetitionerSolicitorSendEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames.SOL_DISPENSED_NOT_APPROVED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getPetitionerFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.getRespondentFullName;

@Component
@Slf4j
public class DispensedNotApprovedSolicitorEmailTask extends PetitionerSolicitorSendEmailTask {
    protected static String subject = "%s vs %s: Solicitor dispensed application not approved";

    public DispensedNotApprovedSolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

    @Override
    protected String getSubject(Map<String, Object> caseData) {
        return format(
            "%s vs %s: Solicitor dispensed application not approved",
            getPetitionerFullName(caseData),
            getRespondentFullName(caseData)
        );
    }

    @Override
    protected EmailTemplateNames getTemplate() {
        return SOL_DISPENSED_NOT_APPROVED;
    }
}
