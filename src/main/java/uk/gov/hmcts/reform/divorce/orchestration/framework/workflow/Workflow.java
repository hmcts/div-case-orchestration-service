package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.Map;
import java.util.Optional;

public interface Workflow<T> {

    T execute(Task[] tasks, T payload, Pair... pairs) throws WorkflowException;

    T execute(Task[] tasks, DefaultTaskContext context, T payload, Pair... pairs) throws WorkflowException;

    Map<String, Object> errors();

    default String getTemplateId(DocumentTemplateService documentTemplateService,
                                 DocumentType documentType, Object languagePreferenceFlag) {
        Optional<LanguagePreference> languagePreference = CaseDataUtils.getLanguagePreference(languagePreferenceFlag);
        return documentTemplateService.getTemplateId(languagePreference, documentType);
    }
}
