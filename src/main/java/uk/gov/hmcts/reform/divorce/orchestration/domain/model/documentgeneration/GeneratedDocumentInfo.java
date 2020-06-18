package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedDocumentInfo {

    private String url;
    private String documentType;
    private String mimeType;
    private String createdOn;
    private String fileName;

    @JsonIgnore
    private byte[] bytes;

    @Override
    public String toString() {
        return String.format("GeneratedDocumentInfo{url='%s', documentType='%s'}", url, documentType);
    }

}