package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.Template;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.Map;

@Data
public class DocumentGenerator implements Template {
    private DocumentType documentType;
    private AOSPackOfflineConstants documentTypeForm;
    private AOSPackOfflineConstants documentFileName;

    public DocumentGenerationRequest getDocumentGenerationRequest(DocumentTemplateService documentTemplateService,
                                                                  Map<String, Object> caseData) {
        String templateId = getTemplateId(documentTemplateService, documentType,
                caseData);
        return new DocumentGenerationRequest(templateId, documentTypeForm.getValue(), documentFileName.getValue());
    }
}