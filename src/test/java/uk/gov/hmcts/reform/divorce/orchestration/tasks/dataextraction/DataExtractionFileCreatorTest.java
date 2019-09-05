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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.eq;
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

    private final DecreeAbsoluteDataExtractor caseDetailsMapper = new DecreeAbsoluteDataExtractor();

    private DataExtractionFileCreator classUnderTest;

    @Before
    public void setUp() {
        classUnderTest = new DataExtractionFileCreator(caseMaintenanceClient, caseDetailsMapper);
    }

    @Test
    public void shouldAddFileToContext() throws TaskException, IOException {
        Map<String, Object> firstCaseData = new HashMap<>();
        firstCaseData.put("D8caseReference", "TEST1");
        firstCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-12T16:49:00.015");
        firstCaseData.put("DecreeNisiGrantedDate", "2017-08-17");
        Map<String, Object> secondCaseData = new HashMap<>();
        secondCaseData.put("D8caseReference", "TEST2");
        secondCaseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        secondCaseData.put("DecreeNisiGrantedDate", "2017-08-26");

        SearchResult searchResult = SearchResult.builder().cases(newArrayList(
            CaseDetails.builder().caseData(firstCaseData).build(),
            CaseDetails.builder().caseData(secondCaseData).build()
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
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST1,12/06/2018,17/08/2017,petitioner"));
        assertThat(fileLines.get(2), is("TEST2,24/06/2018,26/08/2017,petitioner"));
    }

    @Test
    public void shouldUseDecreeNisiGrantedDate_WhenDecreeAbsoluteApplicationDate_IsNotProvided() throws TaskException, IOException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("D8caseReference", "TEST1");
        caseData.put("DecreeNisiGrantedDate", "2017-08-17");

        SearchResult searchResult = SearchResult.builder().cases(newArrayList(
            CaseDetails.builder().caseData(caseData).build()
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
        assertThat(fileLines.get(0), is("CaseReferenceNumber,DAApplicationDate,DNPronouncementDate,PartyApplyingForDA"));
        assertThat(fileLines.get(1), is("TEST1,17/08/2017,17/08/2017,petitioner"));
    }

    @Test
    public void shouldThrowTaskExceptionWhenMandatoryFieldIsNotFound() throws TaskException {
        expectedException.expect(TaskException.class);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("DecreeAbsoluteApplicationDate", "2018-06-24T16:49:00.015");
        caseData.put("DecreeNisiGrantedDate", "2017-08-26");
        SearchResult searchResult = SearchResult.builder().cases(newArrayList(
            CaseDetails.builder().caseData(caseData).build()
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
    }

}