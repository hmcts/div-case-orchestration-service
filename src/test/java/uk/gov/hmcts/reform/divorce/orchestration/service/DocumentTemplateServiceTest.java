package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.CASE_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.COSTS_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DECREE_NISI_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DIVORCE_DRAFT_MINI_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.DIVORCE_MINI_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocumentTemplateServiceTest {

    @Autowired
    private DocumentTemplateService service;

    private final Map<String, Object> englishPreferenceCaseData = Map.of(LANGUAGE_PREFERENCE_WELSH, NO_VALUE);
    private final Map<String, Object> welshPreferenceCaseData = Map.of(LANGUAGE_PREFERENCE_WELSH, YES_VALUE);

    @Test
    public void verifyIllegalArgument() {
        DocumentTemplates documentTemplates = mock(DocumentTemplates.class);
        DocumentTemplateService service = new DocumentTemplateService(documentTemplates);

        Map<LanguagePreference, Map<String, String>> templates = singletonMap(LanguagePreference.ENGLISH,
            singletonMap("decreeNisiTemplateId", "FL-DIV-GNO-ENG-00021.docx"));

        when(documentTemplates.getTemplates()).thenReturn(templates);

        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> service.getTemplateId(englishPreferenceCaseData, DIVORCE_MINI_PETITION)
        );//TODO - this test will probably no longer make sense once the template configuration is in Java
        assertThat(illegalArgumentException.getMessage(), is("No template found for languagePreference "
            + "english and template name divorceMiniPetition"));
    }

    @Test
    public void verifyEnglishTemplates() {
        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DIVORCE_MINI_PETITION, "divorceminipetition");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DIVORCE_DRAFT_MINI_PETITION, "divorcedraftminipetition");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_INVITATION, "aosinvitation");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, CO_RESPONDENT_ANSWERS, "co-respondent-answers");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, CO_RESPONDENT_INVITATION, "co-respondentinvitation");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, COSTS_ORDER_TEMPLATE_ID, "FL-DIV-DEC-ENG-00060.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DECREE_NISI_TEMPLATE_ID, "FL-DIV-GNO-ENG-00021.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DECREE_NISI_ANSWER_TEMPLATE_ID, "FL-DIV-GNO-ENG-00022.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, "FL-DIV-DEC-ENG-00088.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID, "FL-DIV-DEC-ENG-00098.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, DECREE_ABSOLUTE_TEMPLATE_ID, "FL-DIV-GOR-ENG-00062.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID, "FL-DIV-GNO-ENG-00073.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID, "FL-DIV-GNO-ENG-00059.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, "FL-DIV-LET-ENG-00075.doc");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, "FL-DIV-LET-ENG-00076.doc");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID, "FL-DIV-APP-ENG-00080.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID, "FL-DIV-APP-ENG-00081.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID, "FL-DIV-APP-ENG-00082.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID, "FL-DIV-APP-ENG-00083.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID, "FL-DIV-APP-ENG-00084.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, COE, "FL-DIV-GNO-ENG-00020.docx");

        verifyTemplateIdMatchesExpected(englishPreferenceCaseData, CASE_LIST_FOR_PRONOUNCEMENT, "FL-DIV-GNO-ENG-00059.docx");
    }

    @Test
    public void verifyWelshTemplates() {
        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DIVORCE_MINI_PETITION, "divorceminipetitionWelsh");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DIVORCE_DRAFT_MINI_PETITION, "divorcedraftminipetitionWelsh");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_INVITATION, "aosinvitationWelsh");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, CO_RESPONDENT_ANSWERS, "co-respondent-answers-Welsh");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, CO_RESPONDENT_INVITATION, "co-respondentinvitation-Welsh");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, COSTS_ORDER_TEMPLATE_ID, "FL-DIV-DEC-WEL-00240.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DECREE_NISI_TEMPLATE_ID, "FL-DIV-GNO-WEL-00239.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DECREE_NISI_ANSWER_TEMPLATE_ID, "FL-DIV-APP-WEL-00253.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID, "FL-DIV-DEC-WEL-00251.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID, "FL-DIV-DEC-WEL-00252.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, DECREE_ABSOLUTE_TEMPLATE_ID, "FL-DIV-GOR-WEL-00242.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID, "FL-DIV-GNO-WEL-00245.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID, "FL-DIV-GNO-WEL-00241.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, "FL-DIV-LET-WEL-00243.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, "FL-DIV-LET-WEL-00244.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID, "FL-DIV-APP-WEL-00246.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID, "FL-DIV-APP-WEL-00247.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID, "FL-DIV-APP-WEL-00248.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID, "FL-DIV-APP-WEL-00249.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID, "FL-DIV-APP-WEL-00250.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, COE, "FL-DIV-GNO-WEL-00238.docx");

        verifyTemplateIdMatchesExpected(welshPreferenceCaseData, CASE_LIST_FOR_PRONOUNCEMENT, "FL-DIV-GNO-ENG-00059.docx");
    }

    private void verifyTemplateIdMatchesExpected(Map<String, Object> caseData, DocumentType documentType, String expectedTemplateId) {
        String templateId = service.getTemplateId(caseData, documentType);
        assertThat(templateId, equalTo(expectedTemplateId));
    }

}//TODO - I can extend this for my "hardcoding templates" job - next PR