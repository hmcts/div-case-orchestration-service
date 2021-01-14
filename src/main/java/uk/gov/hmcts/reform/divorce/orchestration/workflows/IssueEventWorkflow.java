package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtEnum;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentLetterGeneratorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CoRespondentPinGeneratorTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.GetPetitionIssueFeeTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PetitionGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetCoRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ResetRespondentLinkingFields;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentLetterGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.RespondentPinGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetIssueDateTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.ValidateCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_UNIT_JSON_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class IssueEventWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetIssueDateTask setIssueDateTask;
    private final ValidateCaseDataTask validateCaseDataTask;
    private final PetitionGenerator petitionGenerator;
    private final RespondentPinGenerator respondentPinGenerator;
    private final CoRespondentPinGeneratorTask coRespondentPinGeneratorTask;
    private final RespondentLetterGenerator respondentLetterGenerator;
    private final CoRespondentLetterGeneratorTask coRespondentLetterGeneratorTask;
    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;
    private final GetPetitionIssueFeeTask getPetitionIssueFeeTask;
    private final ResetRespondentLinkingFields resetRespondentLinkingFields;
    private final ResetCoRespondentLinkingFields resetCoRespondentLinkingFields;
    private final CaseDataUtils caseDataUtils;

    public Map<String, Object> run(CcdCallbackRequest ccdCallbackRequest,
                                   String authToken, boolean generateAosInvitation) throws WorkflowException {

        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(validateCaseDataTask);
        tasks.add(setIssueDateTask);
        tasks.add(petitionGenerator);

        final CaseDetails caseDetails = ccdCallbackRequest.getCaseDetails();
        final Map<String, Object> caseData = caseDetails.getCaseData();

        if (generateAosInvitation && isServiceCentreOrNottinghamDivorceUnit(caseData)) {
            tasks.add(respondentPinGenerator);
            tasks.add(respondentLetterGenerator);

            if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
                log.info("Adultery case with co-respondent: {}. Calculating current petition fee and generating"
                    + " co-respondent letter", caseDetails.getCaseId());

                tasks.add(getPetitionIssueFeeTask);
                tasks.add(coRespondentPinGeneratorTask);
                tasks.add(coRespondentLetterGeneratorTask);
            }
        }

        tasks.add(addNewDocumentsToCaseDataTask);
        tasks.add(resetRespondentLinkingFields);
        tasks.add(resetCoRespondentLinkingFields);

        return this.execute(
            tasks.toArray(new Task[0]),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails)
        );
    }

    private boolean isServiceCentreOrNottinghamDivorceUnit(Map<String, Object> caseData) {
        final String court = String.valueOf(caseData.get(DIVORCE_UNIT_JSON_KEY));

        return CourtEnum.EASTMIDLANDS.getId().equalsIgnoreCase(court)
            || CourtEnum.SERVICE_CENTER.getId().equalsIgnoreCase(court);
    }

}