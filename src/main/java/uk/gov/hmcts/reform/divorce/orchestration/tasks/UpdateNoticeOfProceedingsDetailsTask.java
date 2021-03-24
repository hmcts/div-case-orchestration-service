package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_DIGITAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.getRespondentSolicitorEmail;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getRespondentSolicitorOrganisation;

@Component
public class UpdateNoticeOfProceedingsDetailsTask implements Task<Map<String, Object>> {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        caseData.put(NOTICE_OF_PROCEEDINGS_DIGITAL, YES_VALUE);
        caseData.put(NOTICE_OF_PROCEEDINGS_EMAIL, getRespondentSolicitorEmail(caseData));
        caseData.put(NOTICE_OF_PROCEEDINGS_FIRM, getRespondentSolicitorOrganisation(caseData)
            .getOrganisation().getOrganisationName());

        return caseData;
    }
}