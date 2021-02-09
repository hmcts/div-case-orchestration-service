package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import lombok.Getter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.WELSH;

public enum DocumentType {

    DIVORCE_MINI_PETITION("divorceMiniPetition", "divorceminipetition", "FL-DIV-GNO-WEL-00256.docx"),
    DIVORCE_DRAFT_MINI_PETITION("divorceDraftMiniPetition", "divorcedraftminipetition", "FL-DIV-GNO-WEL-00256.docx"),
    AOS_INVITATION("aosInvitation", "aosinvitation", "FL-DIV-LET-WEL-00257.docx"),
    RESPONDENT_ANSWERS("respondentAnswers", "respondentAnswers", "FL-DIV-LET-WEL-00260.docx"),
    CO_RESPONDENT_ANSWERS("coRespondentAnswers", "co-respondent-answers", "FL-DIV-LET-WEL-00258.docx"),
    CO_RESPONDENT_INVITATION("coRespondentInvitation", "co-respondentinvitation", "FL-DIV-LET-WEL-00259.docx"),
    COSTS_ORDER("costsOrderTemplateId", "FL-DIV-DEC-ENG-00060.docx", "FL-DIV-DEC-WEL-00240.docx"),
    DECREE_NISI("decreeNisiTemplateId", "FL-DIV-GNO-ENG-00021.docx", "FL-DIV-GNO-WEL-00239.docx"),
    DECREE_NISI_ANSWER("decreeNisiAnswerTemplateId", "FL-DIV-GNO-ENG-00022.docx", "FL-DIV-APP-WEL-00253.docx"),
    DECREE_NISI_REFUSAL_ORDER_CLARIFICATION(
        "decreeNisiRefusalOrderClarificationTemplateId", "FL-DIV-DEC-ENG-00088.docx", "FL-DIV-DEC-WEL-00251.docx"),
    DECREE_NISI_REFUSAL_ORDER_REJECTION("decreeNisiRefusalOrderRejectionTemplateId", "FL-DIV-DEC-ENG-00098.docx", "FL-DIV-DEC-WEL-00252.docx"),
    DECREE_ABSOLUTE("decreeAbsoluteTemplateId", "FL-DIV-GOR-ENG-00062.docx", "FL-DIV-GOR-WEL-00242.docx"),
    SOLICITOR_PERSONAL_SERVICE_LETTER("solicitorPersonalServiceLetterTemplateId", "FL-DIV-GNO-ENG-00073.docx", "FL-DIV-GNO-WEL-00245.docx"),
    BULK_LIST_FOR_PRONOUNCEMENT("bulkListForPronouncementTemplateId", "FL-DIV-GNO-ENG-00059.docx", "FL-DIV-GNO-WEL-00241.docx"),
    RESPONDENT_AOS_INVITATION_LETTER("respondentAOSInvitationLetterTemplateId", "FL-DIV-LET-ENG-00075.doc", "FL-DIV-LET-WEL-00243.docx"),
    CO_RESPONDENT_AOS_INVITATION_LETTER("coRespondentAOSInvitationLetterTemplateId", "FL-DIV-LET-ENG-00076.doc", "FL-DIV-LET-WEL-00244.docx"),
    AOS_OFFLINE_TWO_YEAR_SEPARATION("aosOfflineTwoYearSeparationTemplateId", "FL-DIV-APP-ENG-00080.docx", "FL-DIV-APP-WEL-00246.docx"),
    AOS_OFFLINE_FIVE_YEAR_SEPARATION("aosOfflineFiveYearSeparationTemplateId", "FL-DIV-APP-ENG-00081.docx", "FL-DIV-APP-WEL-00247.docx"),
    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION(
        "aosOfflineUnreasonableBehaviourAndDesertionTemplateId", "FL-DIV-APP-ENG-00082.docx", "FL-DIV-APP-WEL-00248.docx"),
    AOS_OFFLINE_ADULTERY_RESPONDENT("aosOfflineAdulteryRespondentTemplateId", "FL-DIV-APP-ENG-00083.docx", "FL-DIV-APP-WEL-00249.docx"),
    AOS_OFFLINE_ADULTERY_CO_RESPONDENT("aosOfflineAdulteryCORespondentTemplateId", "FL-DIV-APP-ENG-00084.docx", "FL-DIV-APP-WEL-00250.docx"),
    COE("coe", "FL-DIV-GNO-ENG-00020.docx", "FL-DIV-GNO-WEL-00238.docx"),
    CASE_LIST_FOR_PRONOUNCEMENT("caseListForPronouncement", "FL-DIV-GNO-ENG-00059.docx", "FL-DIV-GNO-ENG-00059.docx"),

    //No Welsh translation yet
    DECREE_ABSOLUTE_GRANTED_CITIZEN_LETTER("daGrantedCitizenLetter", "FL-DIV-GOR-ENG-00355.docx", "FL-DIV-GOR-ENG-00355.docx"),
    DECREE_ABSOLUTE_GRANTED_SOLICITOR_LETTER("daGrantedSolicitorLetter", "FL-DIV-GOR-ENG-00353.docx", "FL-DIV-GOR-ENG-00353.docx"),
    AOS_OVERDUE_COVER_LETTER("aosOverdueCoverLetter", "FL-DIV-LET-ENG-00537.odt", "FL-DIV-LET-ENG-00537.odt"),
    COE_CO_RESPONDENT_LETTER("coe_co_respondent_letter", "FL-DIV-GNO-ENG-00449.docx", "FL-DIV-GNO-ENG-00449.docx"),
    COE_RESPONDENT_LETTER("coe_respondent_letter", "FL-DIV-LET-ENG-00360.docx", "FL-DIV-LET-ENG-00360.docx"),
    COE_RESPONDENT_SOLICITOR_LETTER("coe_respondent_solicitor_letter", "FL-DIV-GNO-ENG-00370.docx", "FL-DIV-GNO-ENG-00370.docx"),
    COST_ORDER_CO_RESPONDENT_LETTER("cost_order_co_respondent_letter", "FL-DIV-LET-ENG-00358A.docx", "FL-DIV-LET-ENG-00358A.docx"),
    COST_ORDER_CO_RESPONDENT_SOLICITOR_LETTER("cost_order_co_respondent_solicitor_letter", "FL-DIV-GNO-ENG-00423.docx", "FL-DIV-GNO-ENG-00423.docx"),
    DN_GRANTED_COVER_LETTER_RESPONDENT("dn_granted_cover_letter_respondent", "FL-DIV-LET-ENG-00357.docx", "FL-DIV-LET-ENG-00357.docx"),
    DN_GRANTED_COVER_LETTER_RESPONDENT_SOLICITOR("dn_granted_cover_letter_respondent_sol", "FL-DIV-GNO-ENG-00356.docx", "FL-DIV-GNO-ENG-00356.docx"),
    GENERAL_ORDER("general_order", "FL-DIV-GOR-ENG-00572.docx", "FL-DIV-GOR-ENG-00572.docx"),
    DEEMED_AS_SERVED_GRANTED("deemed_as_served_granted", "FL-DIV-DEC-ENG-00534.docx", "FL-DIV-DEC-ENG-00534.docx"),
    DEEMED_SERVICE_REFUSED("deemed_service_refused", "FL-DIV-GNO-ENG-00533.docx", "FL-DIV-GNO-ENG-00533.docx"),
    DISPENSE_WITH_SERVICE_REFUSED("dispense_with_service_refused", "FL-DIV-GNO-ENG-00535.docx", "FL-DIV-GNO-ENG-00535.docx"),
    DISPENSE_WITH_SERVICE_GRANTED("dispense_with_service_granted", "FL-DIV-DEC-ENG-00531.docx", "FL-DIV-DEC-ENG-00531.docx");

    @Getter
    private final String templateLogicalName;

    private final Map<LanguagePreference, String> templatePerLanguage;

    DocumentType(String templateLogicalName, @NotNull String englishTemplate, @NotNull String welshTemplate) {
        this.templateLogicalName = templateLogicalName;
        this.templatePerLanguage = Map.of(
            ENGLISH, englishTemplate,
            WELSH, welshTemplate
        );
    }

    public static Optional<DocumentType> getDocumentTypeByTemplateLogicalName(String templateLogicalName) {
        return Arrays.stream(DocumentType.values())
            .filter(enumValue -> enumValue.getTemplateLogicalName().equals(templateLogicalName))
            .findAny();
    }

    public String getTemplateByLanguage(LanguagePreference templateLanguage) {
        return templatePerLanguage.get(templateLanguage);
    }

}