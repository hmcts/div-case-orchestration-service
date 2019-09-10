package uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

import java.util.Optional;

@Component
public class CaseIdMapper implements CaseDetailsMapper {

    @Override
    public Optional<String> mapCaseData(CaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails.getCaseId());
    }

}