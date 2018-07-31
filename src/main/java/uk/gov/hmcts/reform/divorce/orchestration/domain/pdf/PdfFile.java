package uk.gov.hmcts.reform.divorce.orchestration.domain.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public @Data
class PdfFile {
    private String url;
    private String fileName;
}