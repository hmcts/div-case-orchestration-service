package uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextraction;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_REQUESTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_GRANTED;
import static uk.gov.hmcts.reform.divorce.orchestration.service.SearchSourceFactory.buildCMSBooleanSearchSource;
import static uk.gov.hmcts.reform.divorce.orchestration.workflows.dataextraction.FamilyManDataExtractionWorkflow.FILE_TO_PUBLISH;

@RunWith(MockitoJUnitRunner.class)
public class DataExtractionFileCreatorTest {

    private static final String TEST_AUTHORISATION_TOKEN = "testToken";
    private static final String DATE_TO_EXTRACT_KEY = "dateToExtract";

    private final String testLastModifiedDate = "2019-04-12";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private CaseMaintenanceClient caseMaintenanceClient;

    @Mock
    private DecreeAbsoluteDataExtractor mockCaseDetailsMapper;

    private DataExtractionFileCreator classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new DataExtractionFileCreator(caseMaintenanceClient, mockCaseDetailsMapper);

        when(mockCaseDetailsMapper.getHeaderLine()).thenReturn("header");
        when(mockCaseDetailsMapper.mapCaseData(any())).thenReturn(
            Optional.of(System.lineSeparator() + "line1"),
            Optional.empty(),
            Optional.of(System.lineSeparator() + "line2"),
            Optional.empty()
        );
    }

    @Test
    public void givenTransformedCaseDetails_shouldAddExtractionFileToContext() throws TaskException, IOException {
        SearchResult searchResult = SearchResult.builder().cases(newArrayList(
            CaseDetails.builder().build(),
            CaseDetails.builder().build(),
            CaseDetails.builder().build(),
            CaseDetails.builder().build()
        )).build();
        when(caseMaintenanceClient.searchCases(eq(TEST_AUTHORISATION_TOKEN), eq(
            buildCMSBooleanSearchSource(0, 50,
                QueryBuilders.termQuery("last_modified", testLastModifiedDate),
                QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        ))).thenReturn(searchResult);

        DefaultTaskContext taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, TEST_AUTHORISATION_TOKEN);
        taskContext.setTransientObject(DATE_TO_EXTRACT_KEY, LocalDate.parse(testLastModifiedDate));

        classUnderTest.execute(taskContext, null);

        File createdFile = taskContext.getTransientObject(FILE_TO_PUBLISH);
        assertThat(createdFile, is(notNullValue()));
        List<String> fileLines = Files.readAllLines(createdFile.toPath());
        assertThat(fileLines.get(0), is("header"));
        assertThat(fileLines.get(1), is("line1"));
        assertThat(fileLines.get(2), is("line2"));
        verify(caseMaintenanceClient).searchCases(eq(TEST_AUTHORISATION_TOKEN), eq(
            buildCMSBooleanSearchSource(0, 50,
                QueryBuilders.termQuery("last_modified", testLastModifiedDate),
                QueryBuilders.termsQuery("state", DA_REQUESTED.toLowerCase(), DIVORCE_GRANTED.toLowerCase()))
        ));
    }
}