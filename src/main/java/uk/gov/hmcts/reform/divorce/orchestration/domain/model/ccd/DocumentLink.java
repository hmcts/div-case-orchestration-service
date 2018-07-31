package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public @Data class DocumentLink {

    @JsonProperty("document_url")
    private String documentUrl;

    @JsonProperty("document_binary_url")
    private String documentBinaryUrl;

    @JsonProperty("document_filename")
    private String documentFilename;
}
