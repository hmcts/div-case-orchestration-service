package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.getSolicitorOrganisationPolicy;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@RequiredArgsConstructor
@Slf4j
public abstract class SolicitorOrganisationPolicyReferenceTask implements Task<Map<String, Object>> {

    protected abstract String getSolicitorReferenceCaseField();

    protected abstract String getOrganisationPolicyCaseField();

    protected abstract String getSolicitorReference(Map<String, Object> caseData, String caseField);

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String caseId = getCaseId(context);
        String solicitorReferenceCaseField = getSolicitorReferenceCaseField();
        String solicitorReference = getSolicitorReference(caseData, solicitorReferenceCaseField);

        log.info("CaseID: {} About to update solicitor organisation policy reference for {}", caseId, solicitorReferenceCaseField);

        if (solicitorReference == null) {
            log.info("CaseID: {} Solicitor Reference {} not provided, returning case data", caseId, solicitorReferenceCaseField);
            return caseData;
        }

        OrganisationPolicy updatedOrganisationPolicy = getUpdatedOrganisationPolicy(caseData, solicitorReference);

        if (updatedOrganisationPolicy == null) {
            log.info("CaseID: {} Organisation Policy detail is non-existing. No data updated.", caseId);
        } else {
            String organisationPolicyCaseField = getOrganisationPolicyCaseField();
            log.info("CaseID: {} Adding Solicitor Reference to Organisation Policy detail for {}", caseId, organisationPolicyCaseField);
            caseData.put(organisationPolicyCaseField, updatedOrganisationPolicy);
            populateLegacyFieldsWithValuesFromOrganisationPolicy(caseId, caseData,updatedOrganisationPolicy);
        }

        log.info("CaseID: {} Updated solicitor organisation policy reference for {}", caseId, solicitorReferenceCaseField);
        return caseData;
    }

    protected void populateLegacyFieldsWithValuesFromOrganisationPolicy(
        String caseId,
        Map<String, Object> caseData,
        OrganisationPolicy organisationPolicy) {
        log.info("CaseId: {}, by default there is nothing to populate", caseId);
    }

    private OrganisationPolicy getUpdatedOrganisationPolicy(Map<String, Object> caseData, String solicitorReference) {
        OrganisationPolicy organisationPolicy = Optional.ofNullable(getSolicitorOrganisationPolicy(caseData, getOrganisationPolicyCaseField() ))
            .orElse(null);

        if (organisationPolicy != null) {
            organisationPolicy.setOrgPolicyReference(solicitorReference);
        }

        return organisationPolicy;
    }
}
