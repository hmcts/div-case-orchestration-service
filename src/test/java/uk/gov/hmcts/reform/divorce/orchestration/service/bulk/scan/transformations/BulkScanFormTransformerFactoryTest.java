package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.exception.bulk.scan.UnsupportedFormTypeException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.BulkScanForms.NEW_DIVORCE_CASE;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormTransformerFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @InjectMocks
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    @Before
    public void setUp() {
        bulkScanFormTransformerFactory.init();
    }

    @Test
    public void shouldReturnRightTransformationStrategy() {
        assertThat(bulkScanFormTransformerFactory.getTransformer(NEW_DIVORCE_CASE), is(d8FormToCaseTransformer));
    }

    @Test
    public void shouldThrowExceptionForUnsupportedFormType() {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("Form type \"unsupportedFormType\" is not supported.");

        bulkScanFormTransformerFactory.getTransformer("unsupportedFormType");
    }

}