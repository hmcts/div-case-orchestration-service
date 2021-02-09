package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataExtractionRequest.Status.DA;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.STATUS_KEY;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionFileCreatorTest {

    private static final String TEST_AUTHORISATION_TOKEN = "testToken";
    private static final String DATE_TO_EXTRACT_KEY = "dateToExtract";
    private static final String TEST_RELEVANT_STATE = "TEST_RELEVANT_STATE";

    private final LocalDate testLastModifiedDate = LocalDate.parse("2019-04-12");

    @Mock
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    @Mock
    private CSVExtractor csvExtractor;

    @Mock
    private CSVExtractorFactory csvExtractorFactory;

    @InjectMocks
    private DataExtractionFileCreator classUnderTest;

    @Before
    public void setUp() throws TaskException {
        when(csvExtractor.getHeaderLine()).thenReturn("header");
        when(csvExtractor.getRelevantCaseStates()).thenReturn(Stream.of(TEST_RELEVANT_STATE));
        when(csvExtractor.mapCaseData(any())).thenReturn(
            Optional.of("line1"),
            Optional.empty(),
            Optional.of("line2"),
            Optional.empty()
        );
        when(csvExtractorFactory.getCSVExtractorForStatus(DA)).thenReturn(csvExtractor);
    }

    @Test
    public void givenTransformedCaseDetails_shouldAddExtractionFileToContext() throws TaskException, IOException {
        when(cmsElasticSearchSupport.searchCMSCases(eq(TEST_AUTHORISATION_TOKEN), any(), any()))
            .thenReturn(Stream.of(
                CaseDetails.builder().build(),
                CaseDetails.builder().build(),
                CaseDetails.builder().build(),
                CaseDetails.builder().build()
            ));

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, testLastModifiedDate);
        taskContext.setTransientObject(STATUS_KEY, DA);
        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("header"));
        assertThat(fileLines.get(1), is("line1"));
        assertThat(fileLines.get(2), is("line2"));

        verify(cmsElasticSearchSupport).searchCMSCases(eq(TEST_AUTHORISATION_TOKEN),
            eq(QueryBuilders.termQuery("last_modified", testLastModifiedDate)),
            eq(QueryBuilders.termsQuery("state", singletonList(TEST_RELEVANT_STATE.toLowerCase())))
        );
    }
}
