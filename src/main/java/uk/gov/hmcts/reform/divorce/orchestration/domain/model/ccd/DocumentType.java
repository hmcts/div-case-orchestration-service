package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentType {
    AOS ("aos"),
    COSTS("costs"),
    DA_APPLICATION("daApplication"),
    DA("da"),
    DN_APPLICATION("dnApplication"),
    DN("dn"),
    D30("d30"),
    D79("d79"),
    CORRESPONDENCE("correspondence"),
    MARRIAGE_CERT("marriageCert"),
    MARRIAGE_CERT_TRANSLATION("marriageCertTranslation"),
    NAME_CHANGE("nameChange"),
    PETITION("petition"),
    OTHER("other");

    private final String id;

    DocumentType(String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
