package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;

@RunWith(MockitoJUnitRunner.class)
public class BulkScanFormValidatorFactoryTest {

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private NewDivorceCaseValidator newDivorceCaseValidator;

    @InjectMocks
    private BulkScanFormValidatorFactory classUnderTest;

    @Before
    public void setUp() {
        classUnderTest.initBean();
    }

    @Test
    public void shouldReturnValidatorForNewDivorceCaseForm() {
        BulkScanFormValidator validator = classUnderTest.getValidator("newDivorceCase");

        assertThat(validator, is(instanceOf(NewDivorceCaseValidator.class)));
        assertThat(validator, is(newDivorceCaseValidator));
    }

    @Test
    public void shouldThrowExceptionWhenFormTypeIsNotSupported() {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("\"unsupportedFormType\" form type is not supported");

        classUnderTest.getValidator("unsupportedFormType");
    }

}