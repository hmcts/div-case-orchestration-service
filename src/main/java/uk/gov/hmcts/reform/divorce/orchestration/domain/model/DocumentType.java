package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum DocumentType {
    DIVORCE_MINI_PETITION("divorceMiniPetition"),
    DIVORCE_DRAFT_MINI_PETITION("divorceDraftMiniPetition"),
    AOS_INVITATION("aosInvitation"),
    CO_RESPONDENT_ANSWERS("coRespondentAnswers"),
    CO_RESPONDENT_INVITATION("coRespondentInvitation"),
    COSTS_ORDER_TEMPLATE_ID("costsOrderTemplateId"),
    DECREE_NISI_TEMPLATE_ID("decreeNisiTemplateId"),
    DECREE_NISI_ANSWER_TEMPLATE_ID("decreeNisiAnswerTemplateId"),
    DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID("decreeNisiRefusalOrderClarificationTemplateId"),
    DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID("decreeNisiRefusalOrderRejectionTemplateId"),
    DECREE_ABSOLUTE_TEMPLATE_ID("decreeAbsoluteTemplateId"),
    SOLICITOR_PERSONAL_SERVICE_LETTER_TEMPLATE_ID("solicitorPersonalServiceLetterTemplateId"),
    BULK_LIST_FOR_PRONOUNCEMENT_TEMPLATE_ID("bulkListForPronouncementTemplateId"),
    RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID("respondentAOSInvitationLetterTemplateId"),
    CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID("coRespondentAOSInvitationLetterTemplateId"),
    AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID("aosOfflineTwoYearSeparationTemplateId"),
    AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID("aosOfflineFiveYearSeparationTemplateId"),
    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID("aosOfflineUnreasonableBehaviourAndDesertionTemplateId"),
    AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID("aosOfflineAdulteryRespondentTemplateId"),
    AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID("aosOfflineAdulteryCORespondentTemplateId"),
    RESPONDENT_ANSWERS("respondentAnswers"),
    COE("coe"),
    CASE_LIST_FOR_PRONOUNCEMENT("caseListForPronouncement");
    

    private final String templateName;

    DocumentType(String templateName) {
        this.templateName = templateName;
    }

    public static Optional<DocumentType> getEnum(String value) {
        return Arrays.stream(DocumentType.values())
            .filter(enumValue -> enumValue.getTemplateName().equals(value)).findAny();
    }

}

