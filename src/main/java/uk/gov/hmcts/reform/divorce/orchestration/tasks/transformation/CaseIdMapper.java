package uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

@Component
public class CaseIdMapper implements CaseDetailsMapper {

    @Override
    public String mapCaseData(CaseDetails caseDetails) throws TaskException {
        return caseDetails.getCaseId();
    }

}