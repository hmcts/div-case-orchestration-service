package uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
public class UploadedFile {
    private int createdBy;
    private Date createdOn;
    private int lastModifiedBy;
    private Date modifiedOn;
    private String fileName;
    private String fileUrl;
    private String mimeType;
    private String status;
    @JsonIgnore
    private String fileType;
}
