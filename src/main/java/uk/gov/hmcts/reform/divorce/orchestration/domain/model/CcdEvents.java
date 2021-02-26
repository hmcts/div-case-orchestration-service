package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdEvents {

    public static final String AMEND_PETITION_EVENT_ID = "amendPetition";
    public static final String AMEND_PETITION_FOR_REFUSAL_EVENT_ID = "amendPetitionForRefusalRejection";
    public static final String AWAITING_DN_AOS_EVENT_ID = "aosSubmittedUndefended";
    public static final String AWAITING_ANSWER_AOS_EVENT_ID = "aosSubmittedDefended";
    public static final String AOS_NOMINATE_SOLICITOR_ID = "aosNominateSol";
    public static final String AOS_NOT_RECEIVED_FOR_PROCESS_SERVER_EVENT_ID = "aosNotReceivedForProcessServer";
    public static final String AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD_EVENT_ID = "aosNotReceivedForAltMethod";
    public static final String AOS_START_FROM_OVERDUE_ID = "startAosFromOverdue";
    public static final String AOS_START_FROM_REISSUE_ID = "startAosFromReissue";
    public static final String AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED_EVENT_ID = "startAosFromServiceAppRejected";
    public static final String BO_WELSH_AOS_SUBMITTED_DEFENDED_EVENT_ID = "boWelshAosSubmittedDefended";
    public static final String BO_WELSH_AOS_SUBMITTED_UNDEFENDED_EVENT_ID = "boWelshAosSubmittedUndefended";
    public static final String BO_WELSH_AOS_RECEIVED_NO_AD_CON_STARTED_EVENT_ID = "boWelshAosReceivedNoAdConStarted";
    public static final String BO_WELSH_DN_RECEIVED_EVENT_ID = "boDnReceived";
    public static final String BO_WELSH_DN_RECEIVED_AOS_COMPLETED_EVENT_ID = "boDnReceivedAosCompleted";
    public static final String BO_WELSH_DN_RECEIVED_REVIEW_ID = "boWelshDnReceivedReview";
    public static final String BO_WELSH_GRANT_DN_MAKE_DECISION_ID = "boWelshGrantDnMakeDecision";
    public static final String BO_WELSH_SUBMIT_DN_CLARIFICATION_EVENT_ID = "boSubmitDnClarification";
    public static final String BO_WELSH_REVIEW_ID = "boWelshReview";
    public static final String DECREE_ABSOLUTE_REQUESTED_EVENT_ID = "RequestDA";
    public static final String DN_RECEIVED_ID = "dnReceived";
    public static final String DN_RECEIVED_AOS_COMPLETE_ID = "dnReceivedAosCompleted";
    public static final String DN_RECEIVED_CLARIFICATION_ID = "submitDnClarification";
    public static final String COMPLETED_AOS_EVENT_ID = "aosReceivedNoAdConStarted";
    public static final String CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE_EVENT_ID = "co-RespAOSReceivedFromAwaitingAlternativeService";
    public static final String CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE_EVENT_ID = "co-RespAOSReceivedFromAwaitingProcessServerService";
    public static final String CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE_EVENT_ID = "co-RespAOSReceivedFromAwaitingDWPResponse";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_AWAITING_EVENT_ID = "co-RespAOSReceivedAwaiting";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_STARTED_EVENT_ID = "co-RespAOSReceivedStarted";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT_EVENT_ID = "co-RespAOSReceivedAwaitingAnswer";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_OVERDUE_EVENT_ID = "co-RespAOSReceivedOverdue";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_DEFENDED_EVENT_ID = "co-RespAOSReceivedDefended";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_COMPLETED_EVENT_ID = "co-RespAOSCompleted";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_DN_EVENT_ID = "co-RespAwaitingDN";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_LA_EVENT_ID = "co-RespAwaitingLAReferral";
    public static final String ISSUE_AOS_EVENT_ID = "issueAos";
    public static final String ISSUE_AOS_FROM_REISSUE_EVENT_ID = "issueAosFromReissue";
    public static final String LINK_RESPONDENT_GENERIC_EVENT_ID = "linkRespondent";
    public static final String MAKE_CASE_DA_OVERDUE_EVENT_ID = "DecreeAbsoluteOverdue";
    public static final String MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER_EVENT_ID = "MakeEligibleForDA_Petitioner";
    public static final String NOT_RECEIVED_AOS_EVENT_ID = "aosNotReceived";
    public static final String NOT_RECEIVED_AOS_STARTED_EVENT_ID = "aosNotReceivedStarted";
    public static final String PAYMENT_MADE_EVENT_ID = "paymentMade";
    public static final String START_AOS_EVENT_ID = "startAos";
    public static final String SOL_AOS_SUBMITTED_DEFENDED_EVENT_ID = "solAosSubmittedDefended";
    public static final String SOL_AOS_RECEIVED_NO_ADCON_STARTED_EVENT_ID = "solAosReceivedNoAdConStarted";
    public static final String SOL_AOS_SUBMITTED_UNDEFENDED_EVENT_ID = "solAosSubmittedUndefended";
    public static final String SOLICITOR_SUBMIT_EVENT_ID = "solicitorStatementOfTruthPaySubmit";
}