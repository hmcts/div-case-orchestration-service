package uk.gov.hmcts.reform.divorce.orchestration.service;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocumentTemplateServiceTest {

    @Autowired
    private DocumentTemplateService service;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void verifIllegalArgument() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(org.hamcrest.CoreMatchers.equalTo("No template found for languagePreference "
                + "english and template name divorceMiniPetition"));

        DocumentTemplates documentTemplates = Mockito.mock(DocumentTemplates.class);
        DocumentTemplateService service = new DocumentTemplateService(documentTemplates);

        Map<LanguagePreference, Map<String, String>> templates = singletonMap(LanguagePreference.ENGLISH,
                singletonMap("decreeNisiTemplateId", "FL-DIV-GNO-ENG-00021.docx"));

        when(documentTemplates.getTemplates()).thenReturn(templates);

        service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DIVORCE_MINI_PETITION);
    }

    @Test
    public void verifyEnglishTemplates() {
        String templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DIVORCE_MINI_PETITION);
        assertEquals("divorceminipetition", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DIVORCE_DRAFT_MINI_PETITION);
        assertEquals("divorcedraftminipetition", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.AOS_INVITATION);
        assertEquals("aosinvitation", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.CO_RESPONDENT_ANSWERS);
        assertEquals("co-respondent-answers", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.CO_RESPONDENT_INVITATION);
        assertEquals("co-respondentinvitation", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.COSTS_ORDER_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00060.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_NISI_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00021.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00022.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00088.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00098.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID);
        assertEquals("FL-DIV-GOR-ENG-00062.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00073.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00059.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00075.doc", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00076.doc", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00080.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00081.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH),
                DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00082.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH),
                DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00083.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.ENGLISH), DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00084.docx", templateId);
    }

    @Test
    public void verifyWelshTemplates() {
        String templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DIVORCE_MINI_PETITION);
        assertEquals("divorceminipetitionWelsh", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DIVORCE_DRAFT_MINI_PETITION);
        assertEquals("divorcedraftminipetitionWelsh", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.AOS_INVITATION);
        assertEquals("aosinvitationWelsh", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.CO_RESPONDENT_ANSWERS);
        assertEquals("co-respondent-answers-Welsh", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.CO_RESPONDENT_INVITATION);
        assertEquals("co-respondentinvitation-Welsh", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.COSTS_ORDER_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00240.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DECREE_NISI_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00239.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00253.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00251.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00252.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID);
        assertEquals("FL-DIV-GOR-WEL-00242.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00245.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00241.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-WEL-00243.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-WEL-00244.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00246.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00247.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH),
                DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00248.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00249.docx", templateId);

        templateId = service.getTemplateId(Optional.of(LanguagePreference.WELSH), DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00250.docx", templateId);
    }

    @Test
    public void verifyOptionEmptyTemplates() {
        String templateId = service.getTemplateId(Optional.empty(), DocumentType.DIVORCE_MINI_PETITION);
        assertEquals("divorceminipetition", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DIVORCE_DRAFT_MINI_PETITION);
        assertEquals("divorcedraftminipetition", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_INVITATION);
        assertEquals("aosinvitation", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.CO_RESPONDENT_ANSWERS);
        assertEquals("co-respondent-answers", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.CO_RESPONDENT_INVITATION);
        assertEquals("co-respondentinvitation", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.COSTS_ORDER_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00060.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DECREE_NISI_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00021.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00022.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00088.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00098.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID);
        assertEquals("FL-DIV-GOR-ENG-00062.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00073.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00059.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00075.doc", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00076.doc", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00080.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00081.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00082.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00083.docx", templateId);

        templateId = service.getTemplateId(Optional.empty(), DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00084.docx", templateId);
    }
}
