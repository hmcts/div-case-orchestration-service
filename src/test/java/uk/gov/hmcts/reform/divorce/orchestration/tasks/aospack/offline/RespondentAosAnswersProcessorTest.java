package uk.gov.hmcts.reform.divorce.orchestration.tasks.aospack.offline;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_COMPLETED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_SUBMITTED_AWAITING_ANSWER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RECEIVED_AOS_FROM_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_ADMIT_OR_CONSENT_TO_FACT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_WILL_DEFEND_DIVORCE_OFFLINE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.STATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;

public class RespondentAosAnswersProcessorTest {

    private final RespondentAosAnswersProcessor respondentAosAnswersProcessor = new RespondentAosAnswersProcessor();

    @Test
    public void shouldReturnNewField_AndState_WhenRespondentDefendsDivorce() throws TaskException {
        Map<String, Object> payload = new HashMap<String, Object>() {
            {
                put("testKey", "testValue");
                put(D_8_REASON_FOR_DIVORCE, ADULTERY);
                put(RESP_WILL_DEFEND_DIVORCE_OFFLINE, YES_VALUE);
            }
        };
        Map<String, Object> returnedPayload = respondentAosAnswersProcessor.execute(null, payload);

        assertThat(returnedPayload, allOf(
            hasEntry("testKey", "testValue"),
            hasEntry(RECEIVED_AOS_FROM_RESP, YES_VALUE),
            hasEntry(STATE_CCD_FIELD, AOS_SUBMITTED_AWAITING_ANSWER)
        ));
    }

    @Test
    public void shouldReturnNewField_AndState_WhenRespondentDoesNotDefend_ButDoesNotAdmit_ForAdultery() throws TaskException {
        Map<String, Object> payload = new HashMap<String, Object>() {
            {
                put("testKey", "testValue");
                put(D_8_REASON_FOR_DIVORCE, ADULTERY);
                put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
            }
        };
        Map<String, Object> returnedPayload = respondentAosAnswersProcessor.execute(null, payload);

        assertThat(returnedPayload, allOf(
            hasEntry("testKey", "testValue"),
            hasEntry(RECEIVED_AOS_FROM_RESP, YES_VALUE),
            hasEntry(STATE_CCD_FIELD, AOS_COMPLETED)
        ));
    }

    @Test
    public void shouldReturnNewField_AndState_WhenRespondentDoesNotDefend_ButDoesNotAdmit_ForTwoYearsSeparation() throws TaskException {
        Map<String, Object> payload = new HashMap<String, Object>() {
            {
                put("testKey", "testValue");
                put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS);
                put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
            }
        };
        Map<String, Object> returnedPayload = respondentAosAnswersProcessor.execute(null, payload);

        assertThat(returnedPayload, allOf(
            hasEntry("testKey", "testValue"),
            hasEntry(RECEIVED_AOS_FROM_RESP, YES_VALUE),
            hasEntry(STATE_CCD_FIELD, AOS_COMPLETED)
        ));
    }

    @Test
    public void shouldReturnNewField_AndState_WhenRespondentDoesNotDefend_ButDoesNotAdmit_AndReasonIsNotAdultery_OrTwoYearsSeparation()
        throws TaskException {

        Map<String, Object> payload = new HashMap<String, Object>() {
            {
                put("testKey", "testValue");
                put(D_8_REASON_FOR_DIVORCE, DESERTION);
                put(RESP_ADMIT_OR_CONSENT_TO_FACT, NO_VALUE);
            }
        };
        Map<String, Object> returnedPayload = respondentAosAnswersProcessor.execute(null, payload);

        assertThat(returnedPayload, allOf(
            hasEntry("testKey", "testValue"),
            hasEntry(RECEIVED_AOS_FROM_RESP, YES_VALUE),
            hasEntry(STATE_CCD_FIELD, AWAITING_DECREE_NISI)
        ));
    }

    @Test
    public void shouldReturnNewField_AndState_WhenRespondentDoesNotDefend_AndAdmitsOrConsents_ForTwoYearsSeparation() throws TaskException {
        Map<String, Object> payload = new HashMap<String, Object>() {
            {
                put("testKey", "testValue");
                put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS);
                put(RESP_ADMIT_OR_CONSENT_TO_FACT, YES_VALUE);
            }
        };
        Map<String, Object> returnedPayload = respondentAosAnswersProcessor.execute(null, payload);

        assertThat(returnedPayload, allOf(
            hasEntry("testKey", "testValue"),
            hasEntry(RECEIVED_AOS_FROM_RESP, YES_VALUE),
            hasEntry(STATE_CCD_FIELD, AWAITING_DECREE_NISI)
        ));
    }

}