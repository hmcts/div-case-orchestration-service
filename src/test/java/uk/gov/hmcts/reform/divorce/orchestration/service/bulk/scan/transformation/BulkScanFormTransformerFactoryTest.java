package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_2_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_5_YR_SEP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_ADULTERY_CO_RESP;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_OFFLINE_BEHAVIOUR_DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormTransformerFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @Mock
    private AosOffline2YrSepFormToCaseTransformer aosOffline2YrSepFormToCaseTransformer;

    @Mock
    private AosOffline5YrSepFormToCaseTransformer aosOffline5YrSepFormToCaseTransformer;

    @Mock
    private AosOfflineBehaviourDesertionFormToCaseTransformer aosOfflineBehaviourDesertionFormToCaseTransformer;

    @Mock
    private AosOfflineAdulteryCoRespFormToCaseTransformer aosOfflineAdulteryCoRespFormToCaseTransformer;

    @InjectMocks
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Before
    public void setUp() {
        bulkScanFormTransformerFactory.init();
    }

    @Test
    public void shouldReturnRightTransformationStrategy() {
        assertThat(bulkScanFormTransformerFactory.getTransformer(D8_FORM), is(d8FormToCaseTransformer));
        assertThat(bulkScanFormTransformerFactory.getTransformer(AOS_OFFLINE_2_YR_SEP), is(aosOffline2YrSepFormToCaseTransformer));
        assertThat(bulkScanFormTransformerFactory.getTransformer(AOS_OFFLINE_5_YR_SEP), is(aosOffline5YrSepFormToCaseTransformer));
        assertThat(bulkScanFormTransformerFactory.getTransformer(AOS_OFFLINE_BEHAVIOUR_DESERTION),
            is(aosOfflineBehaviourDesertionFormToCaseTransformer));
        assertThat(bulkScanFormTransformerFactory.getTransformer(AOS_OFFLINE_ADULTERY_CO_RESP), is(aosOfflineAdulteryCoRespFormToCaseTransformer));
    }

    @Test
    public void shouldThrowExceptionForUnsupportedFormType() {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("Form type \"unsupportedFormType\" is not supported.");

        bulkScanFormTransformerFactory.getTransformer("unsupportedFormType");
    }
}