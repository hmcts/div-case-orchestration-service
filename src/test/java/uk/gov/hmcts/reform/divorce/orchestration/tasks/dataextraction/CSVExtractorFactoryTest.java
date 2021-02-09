package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.AOS;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DN;

@RunWith(MockitoJUnitRunner.class)
public class CSVExtractorFactoryTest {

    @Mock
    private DecreeAbsoluteDataExtractor decreeAbsoluteDataExtractor;

    @InjectMocks
    private CSVExtractorFactory csvExtractorFactory;

    @Before
    public void setUp() {
        csvExtractorFactory.init();
    }

    @Test
    public void getRightCSVExtractor() {
        assertThat(csvExtractorFactory.getCSVExtractorForStatus(DA), is(decreeAbsoluteDataExtractor));
    }

    @Test
    public void shouldThrowExceptionWhenTryingToRetrieveUnimplementedCSVExtractor() {
        UnsupportedOperationException exception = assertThrows(
            UnsupportedOperationException.class,
            () -> csvExtractorFactory.getCSVExtractorForStatus(DN)
        );

        assertThat(exception.getMessage(), is("CSVExtractor for DN not implemented."));
    }

    @Test
    public void hasCSVExtractorForStatus() {
        assertThat(csvExtractorFactory.hasCSVExtractorForStatus(DA), is(true));
        assertThat(csvExtractorFactory.hasCSVExtractorForStatus(DN), is(true));
        assertThat(csvExtractorFactory.hasCSVExtractorForStatus(AOS), is(true));
    }

}