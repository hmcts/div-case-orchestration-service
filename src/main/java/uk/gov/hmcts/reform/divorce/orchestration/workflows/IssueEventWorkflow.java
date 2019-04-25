package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFee;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetAosLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDate;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_SERVICE_CENTRE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CO_RESPONDENT_NAMED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;

@Component
@Slf4j
public class IssueEventWorkflow extends DefaultWorkflow<Map<String, Object>> {
    private final SetIssueDate setIssueDate;
    private final ValidateCaseData validateCaseData;
    private final PetitionGenerator petitionGenerator;
    private final RespondentPinGenerator respondentPinGenerator;
    private final CoRespondentPinGenerator coRespondentPinGenerator;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CoRespondentLetterGenerator coRespondentLetterGenerator;
    private final CaseFormatterAddDocuments caseFormatterAddDocuments;
    private final GetPetitionIssueFee getPetitionIssueFee;
    private final ResetAosLinkingFields resetAosLinkingFields;

    @Autowired
    @SuppressWarnings("squid:S00107") // Can never have enough collaborators
    public IssueEventWorkflow(ValidateCaseData validateCaseData,
                              SetIssueDate setIssueDate,
                              PetitionGenerator petitionGenerator,
                              RespondentPinGenerator respondentPinGenerator,
                              CoRespondentPinGenerator coRespondentPinGenerator,
                              RespondentLetterGenerator respondentLetterGenerator,
                              GetPetitionIssueFee getPetitionIssueFee,
                              CoRespondentLetterGenerator coRespondentLetterGenerator,
                              CaseFormatterAddDocuments caseFormatterAddDocuments,
                              ResetAosLinkingFields resetAosLinkingFields) {
        this.validateCaseData = validateCaseData;
        this.setIssueDate = setIssueDate;
        this.petitionGenerator = petitionGenerator;
        this.respondentPinGenerator = respondentPinGenerator;
        this.coRespondentPinGenerator = coRespondentPinGenerator;
        this.respondentLetterGenerator = respondentLetterGenerator;
        this.getPetitionIssueFee = getPetitionIssueFee;
        this.coRespondentLetterGenerator = coRespondentLetterGenerator;
        this.caseFormatterAddDocuments = caseFormatterAddDocuments;
        this.resetAosLinkingFields = resetAosLinkingFields;
    }

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken, boolean generateAosInvitation) throws WorkflowException {

        List<Task> tasks = new ArrayList<>();

        tasks.add(validateCaseData);
        tasks.add(setIssueDate);
        tasks.add(petitionGenerator);

        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        final Map<String, Object> caseData = caseDetails.getCaseData();

        if (generateAosInvitation && isServiceCentreDivorceUnit(caseData)) {
            tasks.add(respondentPinGenerator);
            tasks.add(respondentLetterGenerator);

            if (isAdulteryCaseWithCoRespondent(caseData)) {
                log.info("Adultery case with co-respondent: {}. Calculating current petition fee and generating"
                    + " co-respondent letter", caseDetails.getCaseId());

                tasks.add(getPetitionIssueFee);
                tasks.add(coRespondentPinGenerator);
                tasks.add(coRespondentLetterGenerator);
            }
        }

        tasks.add(caseFormatterAddDocuments);
        tasks.add(resetAosLinkingFields);

        return this.execute(
            tasks.toArray(new Task[tasks.size()]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }

    private boolean isServiceCentreDivorceUnit(Map<String, Object> caseData) {
        return DIVORCE_UNIT_SERVICE_CENTRE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_UNIT_JSON_KEY)));
    }

    @SuppressWarnings("Duplicates")
    private boolean isAdulteryCaseWithCoRespondent(final Map<String, Object> caseData) {
        final String divorceReason = String.valueOf(caseData.getOrDefault(D_8_REASON_FOR_DIVORCE, EMPTY));
        final String coRespondentNamed = String.valueOf(caseData.getOrDefault(D_8_CO_RESPONDENT_NAMED, EMPTY));

        return ADULTERY.equals(divorceReason) && "YES".equals(coRespondentNamed);
    }
}
