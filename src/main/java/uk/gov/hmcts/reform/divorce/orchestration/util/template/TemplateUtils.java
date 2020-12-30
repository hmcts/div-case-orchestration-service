package uk.gov.hmcts.reform.divorce.orchestration.util.template;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.Map;

public class TemplateUtils {

    //TODO - rethink name and parameters
    public static String getTemplateId(DocumentTemplateService documentTemplateService,
                                       DocumentType documentType, Map<String, Object> caseData) {//TODO - I've not written tests
        return documentTemplateService.getTemplateId(caseData, documentType);//TODO - I can probably just call the service directly
    }

}