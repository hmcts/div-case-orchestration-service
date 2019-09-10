package uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Optional;

/**
 * Interface used for transforming case details.
 */
public interface CaseDetailsMapper {

    Optional<String> mapCaseData(CaseDetails caseDetails) throws TaskException;

}
