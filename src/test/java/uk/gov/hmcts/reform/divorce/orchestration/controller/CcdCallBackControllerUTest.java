package uk.gov.hmcts.reform.divorce.orchestration.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.PetitionIssuedCallBackService;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallBackControllerUTest {
    @Mock
    private PetitionIssuedCallBackService petitionIssuedCallBackService;

    @InjectMocks
    private CcdCallBackController classUnderTest;

    @Test
    public void whenPetitionIssued_thenProceedAsExpected() {
        final String authToken = "authtoken";
        final Map<String, Object> caseData = Collections.emptyMap();
        final CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .build();

        final CreateEvent createEvent = new CreateEvent();
        createEvent.setCaseDetails(caseDetails);

        final CCDCallbackResponse expected = CCDCallbackResponse.builder().build();

        when(petitionIssuedCallBackService.issuePetition(caseData, authToken)).thenReturn(expected);


        ResponseEntity<CCDCallbackResponse> actual = classUnderTest.petitionIssued(authToken, createEvent);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(expected, actual.getBody());
    }
}