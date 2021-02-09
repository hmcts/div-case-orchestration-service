package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class BulkPrintConfig {
    private final String bulkPrintLetterType;
    private final List<String> documentTypesToPrint;
}
