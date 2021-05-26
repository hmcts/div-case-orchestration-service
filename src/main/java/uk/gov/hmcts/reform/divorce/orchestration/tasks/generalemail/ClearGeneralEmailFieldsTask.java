package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;

@Component
public class ClearGeneralEmailFieldsTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return List.of(GENERAL_EMAIL_PARTIES, GENERAL_EMAIL_DETAILS, GENERAL_EMAIL_OTHER_RECIPIENT_NAME, GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL);
    }
}
