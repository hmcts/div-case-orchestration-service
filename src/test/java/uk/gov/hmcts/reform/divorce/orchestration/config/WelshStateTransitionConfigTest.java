package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_OVERDUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_TRANSLATION_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BO_WELSH_RESPONSE_AWAITING_REVIEW;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PENDING_REJECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SUBMITTED;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WelshStateTransitionConfigTest {

    @Autowired
    private WelshStateTransitionConfig welshStateTransitionConfig;

    @Test
    public void stateTransition_submitted() {
        assertEquals(BO_TRANSLATION_REQUESTED, welshStateTransitionConfig.getWelshStopState().get(SUBMITTED));
    }

    @Test
    public void stateTransition_BOTranslationRequested() {
        assertEquals(SUBMITTED, welshStateTransitionConfig.getWelshStopState().get(BO_TRANSLATION_REQUESTED));
    }

    @Test
    public void stateTransition_pendingRejection() {
        assertEquals(BO_TRANSLATION_REQUESTED, welshStateTransitionConfig.getWelshStopState().get(PENDING_REJECTION));
    }

    @Test
    public void stateTransition_aosStarted() {
        assertEquals(BO_WELSH_RESPONSE_AWAITING_REVIEW, welshStateTransitionConfig.getWelshStopState().get(AOS_STARTED));
    }

    @Test
    public void stateTransition_aosOverdue() {
        assertEquals(BO_WELSH_RESPONSE_AWAITING_REVIEW, welshStateTransitionConfig.getWelshStopState().get(AOS_OVERDUE));
    }
}
