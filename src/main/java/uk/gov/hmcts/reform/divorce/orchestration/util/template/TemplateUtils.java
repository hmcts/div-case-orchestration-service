package uk.gov.hmcts.reform.divorce.orchestration.util.template;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;

public class TemplateUtils {

    //TODO - rethink name and parameters
    public static String getTemplateId(DocumentTemplateService documentTemplateService,
                                       DocumentType documentType, Map<String, Object> caseData) {//TODO - I've not written tests
        LanguagePreference languagePreference = CaseDataUtils.getLanguagePreference(caseData);//TODO - maybe this should be in the service
        return documentTemplateService.getTemplateId(languagePreference, documentType);
    }

}