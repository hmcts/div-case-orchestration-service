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
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.AOS_PACK_OFFLINE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.D8_FORM;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormTransformerFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @Mock
    private AosPackOfflineFormToCaseTransformer aosPackOfflineFormToCaseTransformer;

    @InjectMocks
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Before
    public void setUp() {
        bulkScanFormTransformerFactory.init();
    }

    @Test
    public void shouldReturnRightTransformationStrategy() {
        assertThat(bulkScanFormTransformerFactory.getTransformer(D8_FORM), is(d8FormToCaseTransformer));
        assertThat(bulkScanFormTransformerFactory.getTransformer(AOS_PACK_OFFLINE), is(aosPackOfflineFormToCaseTransformer));
    }

    @Test
    public void shouldThrowExceptionForUnsupportedFormType() {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("Form type \"unsupportedFormType\" is not supported.");

        bulkScanFormTransformerFactory.getTransformer("unsupportedFormType");
    }

}