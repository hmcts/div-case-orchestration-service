package uk.gov.hmcts.reform.divorce.orchestration.event.bulk;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.context.ApplicationEvent;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;

@Value
@EqualsAndHashCode(callSuper = true)
public class BulkCaseAcceptedCasesEvent extends ApplicationEvent {
    private final transient CaseDetails caseDetails;

    public BulkCaseAcceptedCasesEvent(Object source, CaseDetails caseDetails) {
        super(source);

        this.caseDetails = caseDetails;
    }
}
