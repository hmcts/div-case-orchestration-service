package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.util.template.TemplateUtils.getTemplateId;

@Setter
@EqualsAndHashCode
public class DocumentGenerator {

    private DocumentType documentType;
    private AOSPackOfflineConstants documentTypeForm;
    private AOSPackOfflineConstants documentFileName;

    //TODO - this and template could probably be the same class???
    public DocumentGenerationRequest getDocumentGenerationRequest(DocumentTemplateService documentTemplateService, Map<String, Object> caseData) {
        String templateId = getTemplateId(documentTemplateService, documentType, caseData);
        return new DocumentGenerationRequest(templateId, documentTypeForm.getValue(), documentFileName.getValue());
    }

}