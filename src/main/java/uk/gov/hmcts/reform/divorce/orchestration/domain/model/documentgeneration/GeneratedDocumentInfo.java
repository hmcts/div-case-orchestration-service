package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class GeneratedDocumentInfo {
    private String url;
    private String documentType;
    private String mimeType;
    private String createdOn;
    private String fileName;
}
