package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.exception.GeneralOrderServiceException;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderServiceImplTest {

    @InjectMocks
    private GeneralOrdersServiceImpl classUnderTest;

    @Test
    public void generateGeneralOrderShouldReturnValidValue() throws GeneralOrderServiceException {
        CcdCallbackResponse response = classUnderTest.generateGeneralOrder(CaseDetails.builder().build(), AUTH_TOKEN);

        assertNotNull(response);
    }

    @Test
    public void generateGeneralOrderDraftShouldReturnValidValue() throws GeneralOrderServiceException {
        CcdCallbackResponse response = classUnderTest.generateGeneralOrderDraft(CaseDetails.builder().build(), AUTH_TOKEN);

        assertNotNull(response);
    }
}
