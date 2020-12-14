package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.dataextractor.GeneralReferralDataExtractable;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isGeneralReferralPaymentRequired;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isReasonGeneralApplicationReferral;
import static uk.gov.hmcts.reform.divorce.orchestration.util.GeneralReferralHelper.isTypeOfAlternativeServiceApplication;

@Component
@Slf4j
@AllArgsConstructor
public class GeneralReferralDataTask implements Task<Map<String, Object>>, GeneralReferralDataExtractable {

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        log.info("CaseId: {} moving temp general referral data to collection.", caseId);

        DivorceGeneralReferral generalReferral = buildGeneralReferral(caseData);

        return addNewServiceApplicationToCaseData(caseData, generalReferral);
    }

    private DivorceGeneralReferral buildGeneralReferral(Map<String, Object> caseData) {
        DivorceGeneralReferral.DivorceGeneralReferralBuilder divorceGeneralReferralBuilder = DivorceGeneralReferral.builder()
            .generalReferralReason(getReason(caseData))
            .generalApplicationReferralDate(getApplicationReferralDateUnformatted(caseData))
            .generalApplicationAddedDate(getApplicationAddedDateUnformatted(caseData))
            .generalReferralType(getType(caseData))
            .generalReferralDetails(getDetails(caseData))
            .generalReferralFee(getIsFeeRequired(caseData))
            .generalReferralDecision(getDecision(caseData))
            .generalReferralDecisionDate(getDecisionDateUnformatted(caseData))
            .generalReferralDecisionReason(getDecisionReason(caseData));

        addConditionalFieldsFor(divorceGeneralReferralBuilder, caseData);

        return divorceGeneralReferralBuilder.build();
    }

    private Map<String, Object> addNewServiceApplicationToCaseData(
        Map<String, Object> caseData, DivorceGeneralReferral serviceApplication) {

        List<CollectionMember<DivorceGeneralReferral>> generalReferrals = getListOfGeneralReferrals(caseData);
        CollectionMember<DivorceGeneralReferral> collectionMember = buildCollectionMember(serviceApplication);

        generalReferrals.add(collectionMember);

        caseData.put(CcdFields.GENERAL_REFERRALS, generalReferrals);

        return caseData;
    }

    private void addConditionalFieldsFor(DivorceGeneralReferral.DivorceGeneralReferralBuilder generalReferral, Map<String, Object> caseData) {
        if (isGeneralReferralPaymentRequired(caseData)) {
            generalReferral.generalReferralPaymentType(getPaymentType(caseData));
        }

        if (isReasonGeneralApplicationReferral(caseData)) {
            generalReferral.generalApplicationFrom(getApplicationFrom(caseData));
        }

        if (isTypeOfAlternativeServiceApplication(caseData)) {
            generalReferral.alternativeServiceMedium(getAlternativeMedium(caseData));
        }
    }
}
