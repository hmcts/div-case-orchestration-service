package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.stream.Stream;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_DRAFTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_PRE_SUBMITTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_HWF_DECISION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_REISSUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.INVALID_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.ISSUED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.REJECTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SOLICITOR_AWAITING_PAYMENT_CONFIRMATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.SUBMITTED;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreviousAmendPetitionStateLoggerHelper {


    public static String getAmendPetitionPreviousState(String stateId) {
        return getAmendPetitionPreStates()
            .filter(state -> state.equalsIgnoreCase(stateId))
            .findFirst()
            .orElse(INVALID_STATE);
    }

    private static Stream<String> getAmendPetitionPreStates() {
        return Stream.of(
            AWAITING_HWF_DECISION,
            SUBMITTED,
            ISSUED,
            REJECTED,
            PENDING_REJECTION,
            SOLICITOR_AWAITING_PAYMENT_CONFIRMATION,
            AOS_AWAITING,
            AOS_STARTED,
            AOS_OVERDUE,
            AWAITING_REISSUE,
            AOS_COMPLETED,
            AOS_AWAITING_SOLICITOR,
            AOS_PRE_SUBMITTED,
            AOS_DRAFTED,
            AWAITING_SERVICE,
            AOS_SUBMITTED_AWAITING_ANSWER,
            AWAITING_DECREE_NISI
        );
    }
}
