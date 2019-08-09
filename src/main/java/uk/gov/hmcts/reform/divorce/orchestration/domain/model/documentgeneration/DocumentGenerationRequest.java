package uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration;

/**
 * This is representation of a request for generating a document as per the COS domain.
 */
public class DocumentGenerationRequest {

    private String documentTemplateId;
    private String documentType;
    private String documentFileName;

    public DocumentGenerationRequest(String documentTemplateId, String documentType, String documentFileName) {
        this.documentTemplateId = documentTemplateId;
        this.documentType = documentType;
        this.documentFileName = documentFileName;
    }

    public String getDocumentTemplateId() {
        return documentTemplateId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentFileName() {
        return documentFileName;
    }
}