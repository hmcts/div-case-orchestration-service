package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Optional;

/**
 * Interface used for extracting case details into CSV files.
 */
public interface CSVExtractor {

    String getHeaderLine();

    String getDestinationEmailAddress();

    String getFileNamePrefix();

    Optional<String> mapCaseData(CaseDetails caseDetails) throws TaskException;

}