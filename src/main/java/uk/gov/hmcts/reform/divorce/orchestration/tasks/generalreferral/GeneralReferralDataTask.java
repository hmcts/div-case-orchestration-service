package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalreferral;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralReferral;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralReferralDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
@AllArgsConstructor
public class GeneralReferralDataTask implements Task<Map<String, Object>> {

    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String caseId = getCaseId(context);
        log.info("CaseId: {} moving temp general referral data to collection.", caseId);

        DivorceGeneralReferral generalReferral = build(caseData);

        return addNewServiceApplicationToCaseData(caseData, generalReferral);
    }

    private Map<String, Object> addNewServiceApplicationToCaseData(
        Map<String, Object> caseData, DivorceGeneralReferral serviceApplication) {

        List<CollectionMember<DivorceGeneralReferral>> collection = ccdUtil.getListOfGeneralReferrals(caseData);
        CollectionMember<DivorceGeneralReferral> collectionMember = new CollectionMember<>();
        collectionMember.setValue(serviceApplication);

        collection.add(collectionMember);

        caseData.put(CcdFields.GENERAL_REFERRALS, collection);

        return caseData;
    }

    private DivorceGeneralReferral build(Map<String, Object> caseData) {
        return DivorceGeneralReferral.builder()
            .generalReferralReason(GeneralReferralDataExtractor.getReason(caseData))
            .generalApplicationFrom(GeneralReferralDataExtractor.getApplicationFrom(caseData))
            .generalApplicationReferralDate(GeneralReferralDataExtractor.getApplicationReferralDateUnformatted(caseData))
            .generalApplicationAddedDate(GeneralReferralDataExtractor.getApplicationAddedDateUnformatted(caseData))
            .generalReferralType(GeneralReferralDataExtractor.getType(caseData))
            .generalReferralDetails(GeneralReferralDataExtractor.getDetails(caseData))
            .alternativeServiceMedium(GeneralReferralDataExtractor.getAlternativeMedium(caseData))
            .generalReferralFee(GeneralReferralDataExtractor.getFee(caseData))
            .generalReferralDecision(GeneralReferralDataExtractor.getDecision(caseData))
            .generalReferralPaymentType(GeneralReferralDataExtractor.getPaymentType(caseData))
            .generalReferralDecisionDate(GeneralReferralDataExtractor.getDecisionDateUnformatted(caseData))
            .generalReferralDecisionReason(GeneralReferralDataExtractor.getDecisionReason(caseData))
            .build();
    }
}
