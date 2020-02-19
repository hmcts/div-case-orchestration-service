package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;

@Getter
public enum DocumentType {
    DIVORCE_MINI_PETITION("divorceMiniPetition"),
    DIVORCE_DRAFT_MINI_PETITION("divorceDraftMiniPetition"),
    AOS_INVITATION("aosInvitation"),
    CO_RESPONDENT_ANSWERS("coRespondentAnswers"),
    CO_RESPONDENT_INVITATION("coRespondentInvitation"),
    DOCUMENT_TEMPLATE_ID("documentTemplateId"),
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
    AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATEI_D("aosOfflineAdulteryRespondentTemplateId"),
    AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID("aosOfflineAdulteryCORespondentTemplateId");
    

    private final String templateName;

    DocumentType(String templateName) {
        this.templateName = templateName;
    }
}

