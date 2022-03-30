package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.RespondentAosPackPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByReference;
import uk.gov.hmcts.reform.divorce.orchestration.util.csv.CaseReference;
import uk.gov.hmcts.reform.divorce.orchestration.util.csv.CaseReferenceCsvLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@RunWith(MockitoJUnitRunner.class)
public class PrintRespondentAosPackServiceTest {

    protected static final String CASE_REFERENCE = "1234432112344321";

    PrintRespondentAosPackService printRespondentAosPackService;
    @Mock
    private CaseReferenceCsvLoader csvLoader;
    @Mock
    private FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    @Mock
    private RespondentAosPackPrinterTask respondentAosPackPrinterTask;
    @Mock
    private SearchForCaseByReference searchForCaseByReference;
    @Captor
    ArgumentCaptor<TaskContext> taskContextArgumentCaptor;
    private CaseDetails caseDetails;

    @Before
    public void setUpTest() {
        printRespondentAosPackService =
            new PrintRespondentAosPackService(csvLoader, fetchPrintDocsFromDmStoreTask,
                respondentAosPackPrinterTask, searchForCaseByReference, 10, 1);
        when(csvLoader.loadCaseReferenceList("printAosPackCaseReferenceList.csv")).thenReturn(
            Arrays.asList(CaseReference.builder().caseReference(CASE_REFERENCE).build()));
        caseDetails = CaseDetails.builder().caseData(Map.of("D8PetitionerEmail", "someemailaddress@mail.com")).build();
        when(searchForCaseByReference.searchCasesByCaseReference(CASE_REFERENCE)).thenReturn(Optional.of(List.of(caseDetails)));
    }

    @Test
    public void shouldExecuteAllRelevantTasks() throws InterruptedException {
        printRespondentAosPackService.printAosPacks();
        verify(searchForCaseByReference).searchCasesByCaseReference(CASE_REFERENCE);
        verify(fetchPrintDocsFromDmStoreTask).execute(taskContextArgumentCaptor.capture(), anyMap());
        verify(respondentAosPackPrinterTask).execute(taskContextArgumentCaptor.capture(), anyMap());

        TaskContext taskContextArgumentCaptorValue = taskContextArgumentCaptor.getValue();
        assertThat(taskContextArgumentCaptorValue.getTransientObject(CASE_DETAILS_JSON_KEY), equalTo(caseDetails));
    }

}
