package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResult {
    private int total;
    private List<CaseDetails> cases;
}
