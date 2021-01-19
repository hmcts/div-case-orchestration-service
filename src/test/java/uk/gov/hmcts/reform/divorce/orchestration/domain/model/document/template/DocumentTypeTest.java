package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OVERDUE_COVER_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.BULK_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CASE_LIST_FOR_PRONOUNCEMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE_CO_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE_RESPONDENT_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COST_ORDER_CO_RESPONDENT_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CO_RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DEEMED_AS_SERVED_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DEEMED_SERVICE_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DISPENSE_WITH_SERVICE_REFUSED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DIVORCE_DRAFT_MINI_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DIVORCE_MINI_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DN_GRANTED_COVER_LETTER_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DN_GRANTED_COVER_LETTER_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.RESPONDENT_AOS_INVITATION_LETTER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.SOLICITOR_PERSONAL_SERVICE_LETTER;

public class DocumentTypeTest {

    @Test
    public void documentTypesShouldHaveExpectedTemplates() {
        assertDocumentTypeTemplates(DIVORCE_MINI_PETITION, "divorceminipetition", "FL-DIV-GNO-WEL-00256.docx");
        assertDocumentTypeTemplates(DIVORCE_DRAFT_MINI_PETITION, "divorcedraftminipetition", "FL-DIV-GNO-WEL-00256.docx");
        assertDocumentTypeTemplates(AOS_INVITATION, "aosinvitation", "FL-DIV-LET-WEL-00257.docx");
        assertDocumentTypeTemplates(RESPONDENT_ANSWERS, "respondentAnswers", "FL-DIV-LET-WEL-00260.docx");
        assertDocumentTypeTemplates(CO_RESPONDENT_ANSWERS, "co-respondent-answers", "FL-DIV-LET-WEL-00258.docx");
        assertDocumentTypeTemplates(CO_RESPONDENT_INVITATION, "co-respondentinvitation", "FL-DIV-LET-WEL-00259.docx");
        assertDocumentTypeTemplates(COSTS_ORDER, "FL-DIV-DEC-ENG-00060.docx", "FL-DIV-DEC-WEL-00240.docx");
        assertDocumentTypeTemplates(DECREE_NISI, "FL-DIV-GNO-ENG-00021.docx", "FL-DIV-GNO-WEL-00239.docx");
        assertDocumentTypeTemplates(DECREE_NISI_ANSWER, "FL-DIV-GNO-ENG-00022.docx", "FL-DIV-APP-WEL-00253.docx");
        assertDocumentTypeTemplates(DECREE_NISI_REFUSAL_ORDER_CLARIFICATION, "FL-DIV-DEC-ENG-00088.docx", "FL-DIV-DEC-WEL-00251.docx");
        assertDocumentTypeTemplates(DECREE_NISI_REFUSAL_ORDER_REJECTION, "FL-DIV-DEC-ENG-00098.docx", "FL-DIV-DEC-WEL-00252.docx");
        assertDocumentTypeTemplates(DECREE_ABSOLUTE, "FL-DIV-GOR-ENG-00062.docx", "FL-DIV-GOR-WEL-00242.docx");
        assertDocumentTypeTemplates(SOLICITOR_PERSONAL_SERVICE_LETTER, "FL-DIV-GNO-ENG-00073.docx", "FL-DIV-GNO-WEL-00245.docx");
        assertDocumentTypeTemplates(BULK_LIST_FOR_PRONOUNCEMENT, "FL-DIV-GNO-ENG-00059.docx", "FL-DIV-GNO-WEL-00241.docx");
        assertDocumentTypeTemplates(RESPONDENT_AOS_INVITATION_LETTER, "FL-DIV-LET-ENG-00075.doc", "FL-DIV-LET-WEL-00243.docx");
        assertDocumentTypeTemplates(CO_RESPONDENT_AOS_INVITATION_LETTER, "FL-DIV-LET-ENG-00076.doc", "FL-DIV-LET-WEL-00244.docx");
        assertDocumentTypeTemplates(AOS_OFFLINE_TWO_YEAR_SEPARATION, "FL-DIV-APP-ENG-00080.docx", "FL-DIV-APP-WEL-00246.docx");
        assertDocumentTypeTemplates(AOS_OFFLINE_FIVE_YEAR_SEPARATION, "FL-DIV-APP-ENG-00081.docx", "FL-DIV-APP-WEL-00247.docx");
        assertDocumentTypeTemplates(AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION, "FL-DIV-APP-ENG-00082.docx", "FL-DIV-APP-WEL-00248.docx");
        assertDocumentTypeTemplates(AOS_OFFLINE_ADULTERY_RESPONDENT, "FL-DIV-APP-ENG-00083.docx", "FL-DIV-APP-WEL-00249.docx");
        assertDocumentTypeTemplates(AOS_OFFLINE_ADULTERY_CO_RESPONDENT, "FL-DIV-APP-ENG-00084.docx", "FL-DIV-APP-WEL-00250.docx");
        assertDocumentTypeTemplates(COE, "FL-DIV-GNO-ENG-00020.docx", "FL-DIV-GNO-WEL-00238.docx");
        assertDocumentTypeTemplates(CASE_LIST_FOR_PRONOUNCEMENT, "FL-DIV-GNO-ENG-00059.docx", "FL-DIV-GNO-ENG-00059.docx");

        //No Welsh translation yet
        assertDocumentTypeTemplates(DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER, "FL-DIV-GOR-ENG-00355.docx", "FL-DIV-GOR-ENG-00355.docx");
        assertDocumentTypeTemplates(DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER, "FL-DIV-GOR-ENG-00353.docx", "FL-DIV-GOR-ENG-00353.docx");
        assertDocumentTypeTemplates(AOS_OVERDUE_COVER_LETTER, "FL-DIV-LET-ENG-00537.odt", "FL-DIV-LET-ENG-00537.odt");
        assertDocumentTypeTemplates(COE_CO_RESPONDENT_LETTER, "FL-DIV-GNO-ENG-00449.docx", "FL-DIV-GNO-ENG-00449.docx");
        assertDocumentTypeTemplates(COE_RESPONDENT_LETTER, "FL-DIV-LET-ENG-00360.docx", "FL-DIV-LET-ENG-00360.docx");
        assertDocumentTypeTemplates(COE_RESPONDENT_SOLICITOR_LETTER, "FL-DIV-GNO-ENG-00370.docx", "FL-DIV-GNO-ENG-00370.docx");
        assertDocumentTypeTemplates(COST_ORDER_CO_RESPONDENT_LETTER, "FL-DIV-LET-ENG-00358A.docx", "FL-DIV-LET-ENG-00358A.docx");
        assertDocumentTypeTemplates(COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER, "FL-DIV-GNO-ENG-00423.docx", "FL-DIV-GNO-ENG-00423.docx");
        assertDocumentTypeTemplates(DN_GRANTED_COVER_LETTER_RESPONDENT, "FL-DIV-LET-ENG-00357.docx", "FL-DIV-LET-ENG-00357.docx");
        assertDocumentTypeTemplates(DN_GRANTED_COVER_LETTER_RESPONDENT_SOLICITOR, "FL-DIV-GNO-ENG-00356.docx", "FL-DIV-GNO-ENG-00356.docx");
        assertDocumentTypeTemplates(GENERAL_ORDER, "FL-DIV-GOR-ENG-00572.docx", "FL-DIV-GOR-ENG-00572.docx");
        assertDocumentTypeTemplates(DEEMED_AS_SERVED_GRANTED, "FL-DIV-DEC-ENG-00534.docx", "FL-DIV-DEC-ENG-00534.docx");
        assertDocumentTypeTemplates(DEEMED_SERVICE_REFUSED, "FL-DIV-GNO-ENG-00533.docx", "FL-DIV-GNO-ENG-00533.docx");
        assertDocumentTypeTemplates(DISPENSE_WITH_SERVICE_REFUSED, "FL-DIV-GNO-ENG-00535.docx", "FL-DIV-GNO-ENG-00535.docx");
        assertDocumentTypeTemplates(DISPENSE_WITH_SERVICE_GRANTED, "FL-DIV-DEC-ENG-00531.docx", "FL-DIV-DEC-ENG-00531.docx");
    }

    private void assertDocumentTypeTemplates(DocumentType documentType, String expectedEnglishTemplate, String expectedWelshTemplate) {
        assertThat(documentType.getTemplateByLanguage(ENGLISH), is(expectedEnglishTemplate));
        assertThat(documentType.getTemplateByLanguage(WELSH), is(expectedWelshTemplate));
    }

    @Test
    public void shouldRetrieveDocumentTypeByTemplateName() {
        Optional<DocumentType> documentType = DocumentType.getDocumentTypeByTemplateLogicalName("costsOrderTemplateId");
        assertThat(documentType.get(), is(COSTS_ORDER));
    }

    @Test
    public void testInvalidDocumentType() {
        Optional<DocumentType> unknown = DocumentType.getDocumentTypeByTemplateLogicalName("unknown");
        assertFalse("match not found", unknown.isPresent());
    }

}