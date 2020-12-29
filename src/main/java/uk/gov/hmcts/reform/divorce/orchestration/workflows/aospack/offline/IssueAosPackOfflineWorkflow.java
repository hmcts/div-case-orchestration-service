package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.config.IssueAosPackOfflineDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AosPackDueDateSetterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MarkJourneyAsOffline;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.constants.TaskContextConstants.DIVORCE_PARTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.template.TemplateUtils.getTemplateId;

@Component
@AllArgsConstructor
@Slf4j
public class IssueAosPackOfflineWorkflow extends DefaultWorkflow<Map<String, Object>> {

    public static final String AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE = "aos-pack-offline-respondent";
    public static final String AOS_PACK_OFFLINE_CO_RESPONDENT_LETTER_TYPE = "aos-pack-offline-co-respondent";

    private static final String ERROR_MESSAGE = "No record for 'reason for divorce' %s";

    private final MultipleDocumentGenerationTask documentsGenerationTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final BulkPrinterTask bulkPrinterTask;
    private final MarkJourneyAsOffline markJourneyAsOffline;
    private final AosPackDueDateSetterTask aosPackDueDateSetterTask;
    private final DocumentTemplateService documentTemplateService;
    private final IssueAosPackOfflineDocuments issueAosPackOfflineDocuments;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty)
        throws WorkflowException {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        final String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);

        final String letterType = getLetterType(divorceParty);
        final List<DocumentGenerationRequest> documentGenerationRequestsList =
            getDocumentGenerationRequestsList(divorceParty, reasonForDivorce, caseData);
        final List<String> documentTypesToPrint = documentGenerationRequestsList.stream()
            .map(DocumentGenerationRequest::getDocumentType)
            .collect(Collectors.toList());

        final List<Task> tasks = new ArrayList<>();

        log.warn("CaseId {}, documentGenerationRequestsList = {}", caseId, documentGenerationRequestsList);
        log.warn("CaseId {}, documentTypesToPrint = {}", caseId, documentTypesToPrint);

        tasks.add(documentsGenerationTask);
        tasks.add(addNewDocumentsToCaseDataTask);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.add(bulkPrinterTask);
        tasks.add(markJourneyAsOffline);

        if (divorceParty.equals(RESPONDENT)) {
            log.warn("CaseId {}, modify modifyDueDate", caseId);
            tasks.add(aosPackDueDateSetterTask);
        }

        log.warn("CaseId {}, number of tasks to be executed {}", caseId, tasks.size());

        return execute(tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(DOCUMENT_GENERATION_REQUESTS_KEY, documentGenerationRequestsList),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, letterType),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, documentTypesToPrint),
            ImmutablePair.of(DIVORCE_PARTY, divorceParty)
        );
    }

    private String getLetterType(DivorceParty divorceParty) throws WorkflowException {
        String letterType;
        switch (divorceParty) {
            case RESPONDENT:
                letterType = AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE;
                break;
            case CO_RESPONDENT:
                letterType = AOS_PACK_OFFLINE_CO_RESPONDENT_LETTER_TYPE;
                break;
            default:
                throw new WorkflowException("Could not find letterType for party " + divorceParty.name());
        }
        return letterType;
    }

    private List<DocumentGenerationRequest> getDocumentGenerationRequestsList(DivorceParty divorceParty,
                                                                              String reasonForDivorce, Map<String, Object> caseData) {
        List<DocumentGenerationRequest> documentGenerationRequestList = new ArrayList<>();

        if (divorceParty.equals(RESPONDENT)) {
            updateRespondentDocumentGenerationRequests(reasonForDivorce, caseData, documentGenerationRequestList);
        } else if (divorceParty.equals(CO_RESPONDENT) && ADULTERY.getValue().equals(reasonForDivorce)) {
            updateCORespondentDocumentGenerationRequests(caseData, documentGenerationRequestList);
        } else {
            documentGenerationRequestList = emptyList();
        }

        return documentGenerationRequestList;
    }

    private void updateCORespondentDocumentGenerationRequests(Map<String, Object> caseData,
                                                           List<DocumentGenerationRequest> documentGenerationRequestList) {
        String templateId = getTemplateId(documentTemplateService,
                DocumentType.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
                caseData);

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
                CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(),
                CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue()));

        templateId = getTemplateId(documentTemplateService,
                DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
                caseData);

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE.getValue(),
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME.getValue()));
    }

    private void updateRespondentDocumentGenerationRequests(String reasonForDivorce, Map<String, Object> caseData,
                                                                                    List<DocumentGenerationRequest> documentGenerationRequestList) {
        String templateId = getTemplateId(documentTemplateService,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
                caseData);

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
                RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE.getValue(),
                RESPONDENT_AOS_INVITATION_LETTER_FILENAME.getValue()));

        log.debug("reasonForDivorce is {}", reasonForDivorce);
        DivorceFacts divorceFact = DivorceFacts.getDivorceFact(reasonForDivorce);

        documentGenerationRequestList.add(Optional.ofNullable(
                issueAosPackOfflineDocuments.getIssueAosPackOffLine().get(divorceFact))
                .map(documentGen -> documentGen.getDocumentGenerationRequest(documentTemplateService, caseData))
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERROR_MESSAGE, reasonForDivorce))));
    }
}