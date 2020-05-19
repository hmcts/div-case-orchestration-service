package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.Getter;

@Getter
public enum AOSPackOfflineConstants {

    RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE("aosinvitationletter-offline-resp"),
    RESPONDENT_AOS_INVITATION_LETTER_FILENAME("aos-invitation-letter-offline-respondent"),
    RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID("FL-DIV-LET-ENG-00075.doc"),

    CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE("aosinvitationletter-offline-co-resp"),
    CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME("aos-invitation-letter-offline-co-respondent"),
    CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID("FL-DIV-LET-ENG-00076.doc"),

    AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE("two-year-separation-aos-form"),
    AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME("two-year-separation-aos-form-resp"),
    AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID("FL-DIV-APP-ENG-00080.docx"),

    AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE("five-year-separation-aos-form"),
    AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME("five-year-separation-aos-form-resp"),
    AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID("FL-DIV-APP-ENG-00081.docx"),

    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE("behaviour-desertion-aos-form"),
    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME("behaviour-desertion-aos-form-resp"),
    AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID("FL-DIV-APP-ENG-00082.docx"),

    AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE("adultery-respondent-aos-form"),
    AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME("adultery-aos-form-resp"),
    AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID("FL-DIV-APP-ENG-00083.docx"),

    AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE("adultery-co-respondent-aos-form"),
    AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME("adultery-aos-form-co-resp"),
    AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID("FL-DIV-APP-ENG-00084.docx");

    private final String value;
    AOSPackOfflineConstants(String value) {
        this.value = value;
    }
}
