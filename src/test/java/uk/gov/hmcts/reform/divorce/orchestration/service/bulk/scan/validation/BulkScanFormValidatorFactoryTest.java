package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

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
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormValidatorFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private D8FormValidator d8FormValidator;

    @Mock
    private AosOffline2yrSepCaseValidator aosOffline2yrSepCaseValidator;

    @InjectMocks
    private BulkScanFormValidatorFactory classUnderTest;

    @Before
    public void setUp() {
        classUnderTest.initBean();
    }

    @Test
    public void shouldReturnValidatorForD8Form() {
        BulkScanFormValidator validator = classUnderTest.getValidator("d8Form");

        assertThat(validator, is(instanceOf(D8FormValidator.class)));
        assertThat(validator, is(d8FormValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOffline2YearSeparationForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("aosPackOffline2YearSeparation");

        assertThat(validator, is(instanceOf(AosOffline2yrSepCaseValidator.class)));
        assertThat(validator, is(aosOffline2yrSepCaseValidator));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() throws UnsupportedFormTypeException {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        classUnderTest.getValidator("unsupportedFormType");
    }
}
