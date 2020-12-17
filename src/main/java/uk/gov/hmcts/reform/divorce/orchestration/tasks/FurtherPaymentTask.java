package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.ReferenceNumber;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember.buildCollectionMember;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;

@Slf4j
public abstract class FurtherPaymentTask implements Task<Map<String, Object>> {

    protected abstract String getFurtherPaymentReferenceNumbersField();

    protected abstract String getPaymentReferenceNumberField();

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        String referenceNumber = getPaymentReferenceNumber(caseData);

        if (referenceNumber == null) {
            log.info("CaseID: {}. No payment reference number found.", getCaseId(context));
            return caseData;
        }

        log.info("CaseID: {}. Payment reference number '{}' found. Updating case data", getCaseId(context), referenceNumber);

        List<CollectionMember<ReferenceNumber>> furtherReferenceNumbers = getFurtherPaymentReferenceCollection(caseData);
        if (furtherReferenceNumbers == null) {
            caseData.put(getFurtherPaymentReferenceNumbersField(), buildNewCollectionMembers(referenceNumber));
        } else {
            furtherReferenceNumbers.add(buildReferenceData(referenceNumber));
        }

        log.info("CaseID: {}. Resetting payment reference number value '{}' to empty in case data", getCaseId(context), referenceNumber);
        caseData.put(getPaymentReferenceNumberField(), null);

        return caseData;
    }

    private List<CollectionMember<ReferenceNumber>> getFurtherPaymentReferenceCollection(Map<String, Object> caseData) {
        return (List<CollectionMember<ReferenceNumber>>) caseData.get(getFurtherPaymentReferenceNumbersField());
    }

    private String getPaymentReferenceNumber(Map<String, Object> caseData) {
        return getOptionalPropertyValueAsString(caseData, getPaymentReferenceNumberField(), null);
    }

    private List<CollectionMember<ReferenceNumber>> buildNewCollectionMembers(String pbaReferenceNumber) {
        List<CollectionMember<ReferenceNumber>> referenceNumbers = new ArrayList<>();
        referenceNumbers.add(buildReferenceData(pbaReferenceNumber));
        return referenceNumbers;
    }

    private CollectionMember<ReferenceNumber> buildReferenceData(String pbaReferenceNumber) {
        ReferenceNumber referenceNumber = ReferenceNumber.builder()
            .reference(pbaReferenceNumber)
            .build();
        return buildCollectionMember(referenceNumber);
    }

}
