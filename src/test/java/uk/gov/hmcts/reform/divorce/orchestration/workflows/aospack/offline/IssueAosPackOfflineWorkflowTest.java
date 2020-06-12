package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.config.IssueAosPackOfflineDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MarkJourneyAsOffline;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.DocumentGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_PARTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;

@RunWith(MockitoJUnitRunner.class)
public class IssueAosPackOfflineWorkflowTest {

    //Bulk print letter types
    private static final String AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE = "aos-pack-offline-respondent";
    private static final String AOS_PACK_OFFLINE_CO_RESPONDENT_LETTER_TYPE = "aos-pack-offline-co-respondent";

    //Document generation parameters

    //Invitation letters
    private static final String RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00075.doc";
    private static final String RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE = "aosinvitationletter-offline-resp";
    private static final String RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME = "aos-invitation-letter-offline-respondent";
    private static final DocumentGenerationRequest EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER = new DocumentGenerationRequest(
        RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
        RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE,
        RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME
    );

    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID = "FL-DIV-LET-ENG-00076.doc";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE = "aosinvitationletter-offline-co-resp";
    private static final String CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME = "aos-invitation-letter-offline-co-respondent";

    //Forms
    private static final String RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_TEMPLATE_ID = "FL-DIV-APP-ENG-00080.docx";
    private static final String RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_DOCUMENT_TYPE = "two-year-separation-aos-form";
    private static final String RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_FILENAME = "two-year-separation-aos-form-resp";

    private static final String RESPONDENT_FIVE_YEAR_SEPARATION_FORM_TEMPLATE_ID = "FL-DIV-APP-ENG-00081.docx";
    private static final String RESPONDENT_FIVE_YEAR_SEPARATION_FORM_DOCUMENT_TYPE = "five-year-separation-aos-form";
    private static final String RESPONDENT_FIVE_YEAR_SEPARATION_FORM_FILENAME = "five-year-separation-aos-form-resp";

    private static final String RESPONDENT_BEHAVIOUR_DESERTION_FORM_TEMPLATE_ID = "FL-DIV-APP-ENG-00082.docx";
    private static final String RESPONDENT_BEHAVIOUR_DESERTION_FORM_DOCUMENT_TYPE = "behaviour-desertion-aos-form";
    private static final String RESPONDENT_BEHAVIOUR_DESERTION_FORM_FILENAME = "behaviour-desertion-aos-form-resp";

    private static final String RESPONDENT_ADULTERY_FORM_TEMPLATE_ID = "FL-DIV-APP-ENG-00083.docx";
    private static final String RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE = "adultery-respondent-aos-form";
    private static final String RESPONDENT_ADULTERY_FORM_FILENAME = "adultery-aos-form-resp";

    private static final String CO_RESPONDENT_ADULTERY_FORM_TEMPLATE_ID = "FL-DIV-APP-ENG-00084.docx";
    private static final String CO_RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE = "adultery-co-respondent-aos-form";
    private static final String CO_RESPONDENT_ADULTERY_FORM_FILENAME = "adultery-aos-form-co-resp";

    @Mock
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Mock
    private AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Mock
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Mock
    private BulkPrinterTask bulkPrinterTask;

    @Mock
    private ModifyDueDate modifyDueDate;

    @Mock
    private MarkJourneyAsOffline markJourneyAsOffline;

    @Mock
    private DocumentTemplateService documentTemplateService;

    @Mock
    private IssueAosPackOfflineDocuments issueAosPackOfflineDocuments;

    @InjectMocks
    private IssueAosPackOfflineWorkflow classUnderTest;

    @Captor
    private ArgumentCaptor<TaskContext> taskContextArgumentCaptor;

    private String testAuthToken = "authToken";
    private CaseDetails caseDetails;

    @Before
    public void setUp() throws TaskException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("testKey", "testValue");
        when(documentsGenerationTask.execute(any(), any())).thenReturn(singletonMap("returnedKey1", "returnedValue1"));
        when(addNewDocumentsToCaseDataTask.execute(any(), any())).thenReturn(singletonMap("returnedKey2", "returnedValue2"));
        when(fetchPrintDocsFromDmStore.execute(any(), any())).thenReturn(singletonMap("returnedKey3", "returnedValue3"));
        when(bulkPrinterTask.execute(any(), any())).thenReturn(singletonMap("returnedKey4", "returnedValue4"));
        when(markJourneyAsOffline.execute(any(), any())).thenReturn(singletonMap("returnedKey5", "returnedValue5"));
        when(modifyDueDate.execute(any(), any())).thenReturn(singletonMap("returnedKey6", "returnedValue6"));

        ImmutableMap<DivorceFacts, DocumentGenerator> issueAosPackOffLine = ImmutableMap.of(
                SEPARATION_TWO_YEARS,
                getDocumentGenerator(DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
                        AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE,
                        AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME),

                SEPARATION_FIVE_YEARS,
                getDocumentGenerator(DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID,
                       AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE,
                       AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME),

                ADULTERY,
                getDocumentGenerator(DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID,
                        AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE,
                        AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME),

                UNREASONABLE_BEHAVIOUR,
                getDocumentGenerator(DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID,
                        AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
                        AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME),

                DESERTION,
                getDocumentGenerator(DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID,
                        AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
                        AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME)
                );

        when(issueAosPackOfflineDocuments.getIssueAosPackOffLine()).thenReturn(issueAosPackOffLine);
        caseDetails = CaseDetails.builder().caseData(payload).build();
    }

    private DocumentGenerator getDocumentGenerator(DocumentType documentType, AOSPackOfflineConstants typeForm,
                                                   AOSPackOfflineConstants fileName) {
        DocumentGenerator documentGenerator = new DocumentGenerator();
        documentGenerator.setDocumentType(documentType);
        documentGenerator.setDocumentTypeForm(typeForm);
        documentGenerator.setDocumentFileName(fileName);

        return documentGenerator;
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent_ForTwoYearSeparation() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, SEPARATION_TWO_YEARS.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID))
                .thenReturn(RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_TEMPLATE_ID);

        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey6", "returnedValue6"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
            EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER,
            new DocumentGenerationRequest(
                RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_TEMPLATE_ID,
                RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_DOCUMENT_TYPE,
                RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_FILENAME
            )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsCalled();
        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE,
            asList(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_TWO_YEAR_SEPARATION_AOS_OFFLINE_FORM_DOCUMENT_TYPE));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent_ForFiveYearSeparation() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, SEPARATION_FIVE_YEARS.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID))
                .thenReturn(RESPONDENT_FIVE_YEAR_SEPARATION_FORM_TEMPLATE_ID);

        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey6", "returnedValue6"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
            EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER,
            new DocumentGenerationRequest(
                RESPONDENT_FIVE_YEAR_SEPARATION_FORM_TEMPLATE_ID,
                RESPONDENT_FIVE_YEAR_SEPARATION_FORM_DOCUMENT_TYPE,
                RESPONDENT_FIVE_YEAR_SEPARATION_FORM_FILENAME
            )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsCalled();
        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE,
            asList(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_FIVE_YEAR_SEPARATION_FORM_DOCUMENT_TYPE));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent_ForDesertion() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, DESERTION.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID))
                .thenReturn(RESPONDENT_BEHAVIOUR_DESERTION_FORM_TEMPLATE_ID);


        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey6", "returnedValue6"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
            EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER,
            new DocumentGenerationRequest(
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_TEMPLATE_ID,
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_DOCUMENT_TYPE,
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_FILENAME
            )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsCalled();
        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE,
            asList(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_BEHAVIOUR_DESERTION_FORM_DOCUMENT_TYPE));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent_ForUnreasonableBehaviour() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID))
                .thenReturn(RESPONDENT_BEHAVIOUR_DESERTION_FORM_TEMPLATE_ID);

        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey6", "returnedValue6"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
            EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER,
            new DocumentGenerationRequest(
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_TEMPLATE_ID,
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_DOCUMENT_TYPE,
                RESPONDENT_BEHAVIOUR_DESERTION_FORM_FILENAME
            )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsCalled();
        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE,
            asList(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_BEHAVIOUR_DESERTION_FORM_DOCUMENT_TYPE));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForRespondent_ForAdultery() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID))
                .thenReturn(RESPONDENT_ADULTERY_FORM_TEMPLATE_ID);

        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey6", "returnedValue6"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
            EXPECTED_RESPONDENT_AOS_OFFLINE_INVITATION_LETTER,
            new DocumentGenerationRequest(
                RESPONDENT_ADULTERY_FORM_TEMPLATE_ID,
                RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE,
                RESPONDENT_ADULTERY_FORM_FILENAME
            )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsCalled();
        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE,
            asList(RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE));
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForCoRespondent_WithouAdultery() throws WorkflowException,
            TaskException {
        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, CO_RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey5", "returnedValue5"));
        verify(documentsGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(caseDetails.getCaseData()));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        List<DocumentGenerationRequest> documentGenerationRequestList = taskContext.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);
        assertThat("list is empty ", documentGenerationRequestList, empty());
    }

    @Test
    public void testTasksAreCalledWithTheCorrectParams_ForCoRespondent_ForAdultery() throws WorkflowException, TaskException {
        caseDetails.getCaseData().put(D_8_REASON_FOR_DIVORCE, ADULTERY.getValue());

        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID))
                .thenReturn(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID);
        when(documentTemplateService.getTemplateId(LanguagePreference.ENGLISH,
                DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID))
                .thenReturn(CO_RESPONDENT_ADULTERY_FORM_TEMPLATE_ID);

        Map<String, Object> returnedPayload = classUnderTest.run(testAuthToken, caseDetails, CO_RESPONDENT);
        assertThat(returnedPayload, hasEntry("returnedKey5", "returnedValue5"));

        List<DocumentGenerationRequest> expectedDocumentGenerationRequests = asList(
                new DocumentGenerationRequest(
                        CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
                        CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE,
                        CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_FILENAME
                ),
                new DocumentGenerationRequest(
                        CO_RESPONDENT_ADULTERY_FORM_TEMPLATE_ID,
                        CO_RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE,
                        CO_RESPONDENT_ADULTERY_FORM_FILENAME
                )
        );
        verifyDocumentGeneratorReceivesExpectedParameters(expectedDocumentGenerationRequests);
        verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(CO_RESPONDENT);
        verifyTasksAreCalledInOrder();
        verifyModifyDueDateIsNotCalled();

        verifyBulkPrintIsCalledAsExpected(AOS_PACK_OFFLINE_CO_RESPONDENT_LETTER_TYPE,
                asList(CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, CO_RESPONDENT_ADULTERY_FORM_DOCUMENT_TYPE));
    }

    private void verifyDocumentGeneratorReceivesExpectedParameters(List<DocumentGenerationRequest> expectedDocumentGenerationRequests)
        throws TaskException {

        verify(documentsGenerationTask).execute(taskContextArgumentCaptor.capture(), eq(caseDetails.getCaseData()));
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(AUTH_TOKEN_JSON_KEY), is(testAuthToken));
        assertThat(taskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));
        List<DocumentGenerationRequest> documentGenerationRequestList = taskContext.getTransientObject(DOCUMENT_GENERATION_REQUESTS_KEY);
        assertThat(documentGenerationRequestList, equalTo(expectedDocumentGenerationRequests));

    }

    private void verifyMarkJourneyAsOfflineReceivesExpectedParameterWithValueOf(DivorceParty divorceParty) {
        TaskContext taskContext = taskContextArgumentCaptor.getValue();
        assertThat(taskContext.getTransientObject(DIVORCE_PARTY), is(divorceParty));
    }

    private void verifyTasksAreCalledInOrder() {
        verify(addNewDocumentsToCaseDataTask).execute(any(), argThat(allOf(
            Matchers.<String, Object>hasEntry("returnedKey1", "returnedValue1")
        )));

        verify(fetchPrintDocsFromDmStore).execute(any(), argThat(allOf(
            Matchers.<String, Object>hasEntry("returnedKey2", "returnedValue2")
        )));

        verify(bulkPrinterTask).execute(taskContextArgumentCaptor.capture(), argThat(allOf(
            Matchers.<String, Object>hasEntry("returnedKey3", "returnedValue3")
        )));

        verify(markJourneyAsOffline).execute(taskContextArgumentCaptor.capture(), argThat(allOf(
                Matchers.<String, Object>hasEntry("returnedKey4", "returnedValue4")
        )));
    }

    private void verifyModifyDueDateIsCalled() {
        verify(modifyDueDate).execute(any(), argThat(allOf(
                Matchers.<String, Object>hasEntry("returnedKey5", "returnedValue5")
        )));
    }

    private void verifyModifyDueDateIsNotCalled() {
        verifyZeroInteractions(modifyDueDate);
    }

    private void verifyBulkPrintIsCalledAsExpected(String expectedLetterType, List<String> expectedDocumentTypesToPrint) {
        TaskContext bulkPrintTaskContext = taskContextArgumentCaptor.getValue();
        assertThat(bulkPrintTaskContext.getTransientObject(CASE_DETAILS_JSON_KEY), is(caseDetails));
        assertThat(bulkPrintTaskContext.getTransientObject(BULK_PRINT_LETTER_TYPE), is(expectedLetterType));
        assertThat(bulkPrintTaskContext.getTransientObject(DOCUMENT_TYPES_TO_PRINT), equalTo(expectedDocumentTypesToPrint));
    }

}