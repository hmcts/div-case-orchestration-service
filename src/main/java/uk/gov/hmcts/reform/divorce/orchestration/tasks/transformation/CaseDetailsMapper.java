package uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

/**
 * Interface used for transforming case details.
 */
public interface CaseDetailsMapper {

    String mapCaseData(CaseDetails caseDetails) throws TaskException;

}
