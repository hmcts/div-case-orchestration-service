package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.DocumentGenerationInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.UNREASONABLE_BEHAVIOUR;

public class AosPackOfflineDocumentsTest {

    @Test
    public void shouldHaveDocumentGenerationInfoForAllDivorceFacts() {
        assertThat(DivorceFact.values().length, is(5));

        assertDocumentGenerationInfoIsAsExpected(SEPARATION_TWO_YEARS,
            AOS_OFFLINE_TWO_YEAR_SEPARATION,
            AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE,
            AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME
        );
        assertDocumentGenerationInfoIsAsExpected(SEPARATION_FIVE_YEARS,
            AOS_OFFLINE_FIVE_YEAR_SEPARATION,
            AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE,
            AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME
        );
        assertDocumentGenerationInfoIsAsExpected(ADULTERY,
            AOS_OFFLINE_ADULTERY_RESPONDENT,
            AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE,
            AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME
        );
        assertDocumentGenerationInfoIsAsExpected(UNREASONABLE_BEHAVIOUR,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME
        );
        assertDocumentGenerationInfoIsAsExpected(DESERTION,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME
        );
    }

    private void assertDocumentGenerationInfoIsAsExpected(DivorceFact fact,
                                                          DocumentType expectedDocumentType,
                                                          String expectedDocumentTypeForm,
                                                          String expectedFileName) {

        Optional<DocumentGenerationInfo> actualDocumentGenerationInfo = AosPackOfflineDocuments.getDocumentGenerationInfoByDivorceFact(fact);

        DocumentGenerationInfo expectedInfo = new DocumentGenerationInfo(expectedDocumentType, expectedDocumentTypeForm, expectedFileName);
        assertThat(actualDocumentGenerationInfo.isPresent(), is(true));
        assertThat(actualDocumentGenerationInfo.get(), equalTo(expectedInfo));
    }

}