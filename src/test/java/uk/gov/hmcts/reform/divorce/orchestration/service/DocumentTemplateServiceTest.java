package uk.gov.hmcts.reform.divorce.orchestration.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.DocumentTemplates;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocumentTemplateServiceTest {

    @Autowired
    private DocumentTemplateService service;

    @Test
    public void verifyIllegalArgument() {
        DocumentTemplates documentTemplates = Mockito.mock(DocumentTemplates.class);
        DocumentTemplateService service = new DocumentTemplateService(documentTemplates);

        Map<LanguagePreference, Map<String, String>> templates = singletonMap(LanguagePreference.ENGLISH,
            singletonMap("decreeNisiTemplateId", "FL-DIV-GNO-ENG-00021.docx"));

        when(documentTemplates.getTemplates()).thenReturn(templates);

        IllegalArgumentException illegalArgumentException = assertThrows(
            IllegalArgumentException.class,
            () -> service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DIVORCE_MINI_PETITION)
        );
        assertThat(illegalArgumentException.getMessage(), is("No template found for languagePreference "
            + "english and template name divorceMiniPetition"));
    }

    @Test
    public void verifyEnglishTemplates() {
        String templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DIVORCE_MINI_PETITION);
        assertEquals("divorceminipetition", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DIVORCE_DRAFT_MINI_PETITION);
        assertEquals("divorcedraftminipetition", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.AOS_INVITATION);
        assertEquals("aosinvitation", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.CO_RESPONDENT_ANSWERS);
        assertEquals("co-respondent-answers", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.CO_RESPONDENT_INVITATION);
        assertEquals("co-respondentinvitation", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.COSTS_ORDER_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00060.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00021.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00022.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00088.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-ENG-00098.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID);
        assertEquals("FL-DIV-GOR-ENG-00062.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00073.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-ENG-00059.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00075.doc", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-ENG-00076.doc", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00080.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00081.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH,
            DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00082.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH,
            DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00083.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-ENG-00084.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.COE);
        assertEquals("FL-DIV-GNO-ENG-00020.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.ENGLISH, DocumentType.CASE_LIST_FOR_PRONOUNCEMENT);
        assertEquals("FL-DIV-GNO-ENG-00059.docx", templateId);
    }

    @Test
    public void verifyWelshTemplates() {
        String templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DIVORCE_MINI_PETITION);
        assertEquals("divorceminipetitionWelsh", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DIVORCE_DRAFT_MINI_PETITION);
        assertEquals("divorcedraftminipetitionWelsh", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.AOS_INVITATION);
        assertEquals("aosinvitationWelsh", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.CO_RESPONDENT_ANSWERS);
        assertEquals("co-respondent-answers-Welsh", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.CO_RESPONDENT_INVITATION);
        assertEquals("co-respondentinvitation-Welsh", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.COSTS_ORDER_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00240.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DECREE_NISI_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00239.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DECREE_NISI_ANSWER_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00253.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00251.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID);
        assertEquals("FL-DIV-DEC-WEL-00252.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.DECREE_ABSOLUTE_TEMPLATE_ID);
        assertEquals("FL-DIV-GOR-WEL-00242.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00245.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID);
        assertEquals("FL-DIV-GNO-WEL-00241.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-WEL-00243.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        assertEquals("FL-DIV-LET-WEL-00244.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00246.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00247.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH,
            DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00248.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00249.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID);
        assertEquals("FL-DIV-APP-WEL-00250.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.COE);
        assertEquals("FL-DIV-GNO-WEL-00238.docx", templateId);

        templateId = service.getConfiguredTemplateId(LanguagePreference.WELSH, DocumentType.CASE_LIST_FOR_PRONOUNCEMENT);
        assertEquals("FL-DIV-GNO-ENG-00059.docx", templateId);
    }
}
