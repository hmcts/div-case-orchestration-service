package uk.gov.hmcts.reform.divorce.orchestration.courtallocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

/*
    This simple test to give more confidence doing court allocation changes
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CourtAllocatorITest {

    private static final String SERVICE_CENTER = "serviceCentre";
    private static final int SAMPLES_NUMBER = 10000;

    @Autowired
    private CourtConfigAllocator courtAllocator;

    @Test
    public void givenDesertionCase_whenAllocateCase_thenReturnServiceCenter() {
        for (int i = 0 ; i < SAMPLES_NUMBER; i++) {
            assertEquals(SERVICE_CENTER, courtAllocator.selectCourtForGivenDivorceFact(DESERTION));
        }
    }

    @Test
    public void given5YearsSep_whenAllocateCase_thenReturnServiceCenter() {
        for (int i = 0 ; i < SAMPLES_NUMBER; i++) {
            assertEquals(SERVICE_CENTER, courtAllocator
                .selectCourtForGivenDivorceFact(SEPARATION_FIVE_YEARS));
        }
    }

    @Test
    public void givenBehaviour_whenAllocateCase_thenReturnServiceCenter() {
        for (int i = 0 ; i < SAMPLES_NUMBER; i++) {
            assertEquals(SERVICE_CENTER, courtAllocator
                .selectCourtForGivenDivorceFact(UNREASONABLE_BEHAVIOUR));
        }
    }

    @Test
    public void givenAdultery_whenAllocateCase_thenReturnServiceCenter() {
        for (int i = 0 ; i < SAMPLES_NUMBER; i++) {
            assertEquals(SERVICE_CENTER, courtAllocator
                .selectCourtForGivenDivorceFact(ADULTERY));
        }
    }

    @Test
    public void given2YearsSep_whenAllocateCase_thenReturnServiceCenter() {
        for (int i = 0 ; i < SAMPLES_NUMBER; i++) {
            assertEquals(SERVICE_CENTER, courtAllocator
                .selectCourtForGivenDivorceFact(SEPARATION_TWO_YEARS));
        }
    }
}
