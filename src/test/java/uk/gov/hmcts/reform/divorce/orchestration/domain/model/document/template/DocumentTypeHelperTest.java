package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;

public class DocumentTypeHelperTest {

    private final Map<String, Object> englishPreferenceCaseData = Map.of(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
    private final Map<String, Object> welshPreferenceCaseData = Map.of(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

    @Test
    public void shouldReturnLanguageSpecificTemplateForGivenDocumentType() {
        String englishTemplate = DocumentTypeHelper.getLanguageAppropriateTemplate(englishPreferenceCaseData, COSTS_ORDER);
        assertThat(englishTemplate, is("FL-DIV-DEC-ENG-00060.docx"));

        String welshTemplate = DocumentTypeHelper.getLanguageAppropriateTemplate(welshPreferenceCaseData, COSTS_ORDER);
        assertThat(welshTemplate, is("FL-DIV-DEC-WEL-00240.docx"));
    }

}