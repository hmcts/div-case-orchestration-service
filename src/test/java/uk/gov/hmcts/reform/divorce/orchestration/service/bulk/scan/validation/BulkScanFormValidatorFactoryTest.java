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

    @Mock
    private AosOffline5yrSepCaseValidator aosOffline5yrSepCaseValidator;

    @Mock
    private AosOfflineBehaviourDesertionCaseValidator aosOfflineBehaviourDesertionCaseValidator;

    @Mock
    private AosOfflineAdulteryCoRespCaseValidator aosOfflineAdulteryCoRespCaseValidator;

    @Mock
    private AosOfflineAdulteryRespCaseValidator aosOfflineAdulteryRespCaseValidator;

    @InjectMocks
    private BulkScanFormValidatorFactory classUnderTest;

    @Before
    public void setUp() {
        classUnderTest.initBean();
    }

    @Test
    public void shouldReturnValidatorForD8Form() {
        BulkScanFormValidator validator = classUnderTest.getValidator("D8");

        assertThat(validator, is(instanceOf(D8FormValidator.class)));
        assertThat(validator, is(d8FormValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOffline2YearSeparationForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("DAOS1");

        assertThat(validator, is(instanceOf(AosOffline2yrSepCaseValidator.class)));
        assertThat(validator, is(aosOffline2yrSepCaseValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOffline5YearSeparationForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("DAOS2");

        assertThat(validator, is(instanceOf(AosOffline5yrSepCaseValidator.class)));
        assertThat(validator, is(aosOffline5yrSepCaseValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOfflineBehaviourDesertionSeparationForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("DAOS3");

        assertThat(validator, is(instanceOf(AosOfflineBehaviourDesertionCaseValidator.class)));
        assertThat(validator, is(aosOfflineBehaviourDesertionCaseValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOfflineAdulteryRespondentForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("DAOS4");

        assertThat(validator, is(instanceOf(AosOfflineAdulteryRespCaseValidator.class)));
        assertThat(validator, is(aosOfflineAdulteryRespCaseValidator));
    }

    @Test
    public void shouldReturnValidatorForAosOfflineAdulteryCoRespSeparationForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("DAOS5");

        assertThat(validator, is(instanceOf(AosOfflineAdulteryCoRespCaseValidator.class)));
        assertThat(validator, is(aosOfflineAdulteryCoRespCaseValidator));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() throws UnsupportedFormTypeException {
        expectedException.expect(UnsupportedFormTypeException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        classUnderTest.getValidator("unsupportedFormType");
    }
}
