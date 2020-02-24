package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MarkJourneyAsOffline;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ModifyDueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_PARTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter.DOCUMENT_TYPES_TO_PRINT;

@Component
@Slf4j
public class IssueAosPackOfflineWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final String AOS_PACK_OFFLINE_RESPONDENT_LETTER_TYPE = "aos-pack-offline-respondent";
    private static final String AOS_PACK_OFFLINE_CO_RESPONDENT_LETTER_TYPE = "aos-pack-offline-co-respondent";

    @Autowired
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Autowired
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Autowired
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Autowired
    private BulkPrinter bulkPrinter;

    @Autowired
    private MarkJourneyAsOffline markJourneyAsOffline;

    @Autowired
    private ModifyDueDate modifyDueDate;

    @Autowired
    private DocumentTemplateService documentTemplateService;

    private Map<String, DocumentGenerator> documentGenerator;
    private static final String ERROR_MESSAGE = "No record for 'reason for divorce' %s";

    @PostConstruct
    public void populateDocumenGenerator() {
        documentGenerator = ImmutableMap.of(
                SEPARATION_TWO_YEARS,
                new DocumentGenerator(DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION_TEMPLATE_ID,
                        AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE,
                        AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME),

                SEPARATION_FIVE_YEARS,
                new DocumentGenerator(DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION_TEMPLATE_ID,
                        AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE,
                        AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME),

                ADULTERY,
                new DocumentGenerator(DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT_TEMPLATE_ID,
                        AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE,
                        AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME),

                UNREASONABLE_BEHAVIOUR,
                new DocumentGenerator(DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID,
                        AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
                        AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME),

                DESERTION,
                new DocumentGenerator(DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_TEMPLATE_ID,
                        AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
                        AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME)
            );
    }

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty) throws WorkflowException {
        final Map<String, Object> caseData = caseDetails.getCaseData();
        final String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);

        final String letterType = getLetterType(divorceParty);
        final List<DocumentGenerationRequest> documentGenerationRequestsList =
                getDocumentGenerationRequestsList(divorceParty, reasonForDivorce, caseData);
        final List<String> documentTypesToPrint = documentGenerationRequestsList.stream()
            .map(DocumentGenerationRequest::getDocumentType)
            .collect(Collectors.toList());

        final List<Task> tasks = new ArrayList<>();

        tasks.add(documentsGenerationTask);
        tasks.add(caseFormatterAddDocuments);
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.add(bulkPrinter);
        tasks.add(markJourneyAsOffline);
        if (divorceParty.equals(RESPONDENT)) {
            tasks.add(modifyDueDate);
        }

        return execute(tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
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
        } else if (divorceParty.equals(CO_RESPONDENT) && ADULTERY.equals(reasonForDivorce)) {
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
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
                CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE,
                CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME));

        templateId = getTemplateId(documentTemplateService,
                DocumentType.AOS_OFFLINE_ADULTERY_CO_RESPONDENT_TEMPLATE_ID,
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_DOCUMENT_TYPE,
            AOS_OFFLINE_ADULTERY_CO_RESPONDENT_FILENAME));
    }

    private void updateRespondentDocumentGenerationRequests(String reasonForDivorce, Map<String, Object> caseData,
                                                                                    List<DocumentGenerationRequest> documentGenerationRequestList) {
        String templateId = getTemplateId(documentTemplateService,
                DocumentType.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
                caseData.get(LANGUAGE_PREFERENCE_WELSH));

        documentGenerationRequestList.add(new DocumentGenerationRequest(templateId,
                RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE,
                RESPONDENT_AOS_INVITATION_LETTER_FILENAME));

        log.debug("reasonForDivorce is {}", reasonForDivorce);

        documentGenerationRequestList.add(Optional.ofNullable(documentGenerator.get(reasonForDivorce))
                .map(documentGen -> documentGen.getDocumentGenerationRequest(caseData))
                .orElseThrow(() -> new IllegalArgumentException(String.format(ERROR_MESSAGE, reasonForDivorce))));
    }

    @AllArgsConstructor
    class DocumentGenerator {
        private final DocumentType documentType;
        private final String documentTypeForm;
        private final String documentFileName;

        public DocumentGenerationRequest getDocumentGenerationRequest(Map<String, Object> caseData) {
            String templateId = getTemplateId(documentTemplateService, documentType,
                    caseData.get(LANGUAGE_PREFERENCE_WELSH));
            return new DocumentGenerationRequest(templateId, documentTypeForm, documentFileName);
        }
    }
}