package uk.gov.hmcts.reform.divorce.orchestration.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdEvents {

    public static final String AMEND_PETITION = "amendPetition";
    public static final String AMEND_PETITION_FOR_REFUSAL = "amendPetitionForRefusalRejection";
    public static final String AWAITING_DN_AOS = "aosSubmittedUndefended";
    public static final String AWAITING_ANSWER_AOS = "aosSubmittedDefended";
    public static final String AOS_NOMINATE_SOLICITOR = "aosNominateSol";
    public static final String AOS_NOT_RECEIVED_FOR_PROCESS_SERVER = "aosNotReceivedForProcessServer";
    public static final String AOS_NOT_RECEIVED_FOR_ALTERNATIVE_METHOD = "aosNotReceivedForAltMethod";
    public static final String AOS_NOT_RECEIVED_FOR_BAILIFF_APPLICATION = "aosNotReceivedForBailiff";
    public static final String AOS_START_FROM_OVERDUE = "startAosFromOverdue";
    public static final String AOS_START_FROM_REISSUE = "startAosFromReissue";
    public static final String AOS_START_FROM_SERVICE_APPLICATION_NOT_APPROVED = "startAosFromServiceAppRejected";
    public static final String BO_WELSH_AOS_SUBMITTED_DEFENDED = "boWelshAosSubmittedDefended";
    public static final String BO_WELSH_AOS_SUBMITTED_UNDEFENDED = "boWelshAosSubmittedUndefended";
    public static final String BO_WELSH_AOS_RECEIVED_NO_AD_CON_STARTED = "boWelshAosReceivedNoAdConStarted";
    public static final String BO_WELSH_DN_RECEIVED = "boDnReceived";
    public static final String BO_WELSH_DN_RECEIVED_AOS_COMPLETED = "boDnReceivedAosCompleted";
    public static final String BO_WELSH_DN_RECEIVED_REVIEW = "boWelshDnReceivedReview";
    public static final String BO_WELSH_GRANT_DN_MAKE_DECISION = "boWelshGrantDnMakeDecision";
    public static final String BO_WELSH_SUBMIT_DN_CLARIFICATION = "boSubmitDnClarification";
    public static final String BO_WELSH_REVIEW = "boWelshReview";
    public static final String DECREE_ABSOLUTE_REQUESTED = "RequestDA";
    public static final String DN_RECEIVED = "dnReceived";
    public static final String DN_RECEIVED_AOS_COMPLETE = "dnReceivedAosCompleted";
    public static final String DN_RECEIVED_CLARIFICATION = "submitDnClarification";
    public static final String COMPLETED_AOS = "aosReceivedNoAdConStarted";
    public static final String CO_RESP_SUBMISSION_AWAITING_ALTERNATIVE_SERVICE = "co-RespAOSReceivedFromAwaitingAlternativeService";
    public static final String CO_RESP_SUBMISSION_AWAITING_PROCESS_SERVER_SERVICE = "co-RespAOSReceivedFromAwaitingProcessServerService";
    public static final String CO_RESP_SUBMISSION_AWAITING_DWP_RESPONSE = "co-RespAOSReceivedFromAwaitingDWPResponse";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_AWAITING = "co-RespAOSReceivedAwaiting";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_STARTED = "co-RespAOSReceivedStarted";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_SUBMIT_AWAIT = "co-RespAOSReceivedAwaitingAnswer";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_OVERDUE = "co-RespAOSReceivedOverdue";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_DEFENDED = "co-RespAOSReceivedDefended";
    public static final String CO_RESPONDENT_SUBMISSION_AOS_COMPLETED = "co-RespAOSCompleted";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_DN = "co-RespAwaitingDN";
    public static final String CO_RESPONDENT_SUBMISSION_AWAITING_LA = "co-RespAwaitingLAReferral";
    public static final String ISSUE_AOS = "issueAos";
    public static final String ISSUE_AOS_FROM_REISSUE = "issueAosFromReissue";
    public static final String LINK_RESPONDENT_GENERIC = "linkRespondent";
    public static final String MAKE_CASE_DA_OVERDUE = "DecreeAbsoluteOverdue";
    public static final String MAKE_CASE_ELIGIBLE_FOR_DA_PETITIONER = "MakeEligibleForDA_Petitioner";
    public static final String NOT_RECEIVED_AOS = "aosNotReceived";
    public static final String NOT_RECEIVED_AOS_STARTED = "aosNotReceivedStarted";
    public static final String PAYMENT_MADE = "paymentMade";
    public static final String START_AOS = "startAos";
    public static final String SOL_AOS_SUBMITTED_DEFENDED = "solAosSubmittedDefended";
    public static final String SOL_AOS_RECEIVED_NO_ADCON_STARTED = "solAosReceivedNoAdConStarted";
    public static final String SOL_AOS_SUBMITTED_UNDEFENDED = "solAosSubmittedUndefended";
    public static final String SOLICITOR_SUBMIT = "solicitorStatementOfTruthPaySubmit";
}
