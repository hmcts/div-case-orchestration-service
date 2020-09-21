package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.FieldsRemovalTask;

import java.util.List;

import static java.util.Arrays.asList;

@Component
@Slf4j
public class GeneralOrderFieldsRemovalTask extends FieldsRemovalTask {

    @Override
    protected List<String> getFieldsToRemove() {
        return asList(
            CcdFields.GENERAL_ORDER_DRAFT,
            CcdFields.GENERAL_ORDER_RECITALS,
            CcdFields.GENERAL_ORDER_PARTIES,
            CcdFields.GENERAL_ORDER_DATE,
            CcdFields.GENERAL_ORDER_DETAILS,
            CcdFields.JUDGE_NAME,
            CcdFields.JUDGE_TYPE
        );
    }
}
