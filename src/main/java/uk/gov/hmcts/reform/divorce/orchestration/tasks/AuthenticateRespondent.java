package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
public class AuthenticateRespondent implements Task<Boolean> {
    @Autowired
    private IdamClient idamClient;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public Boolean execute(TaskContext context, Boolean payload) {
        UserDetails userDetails = idamClient.getUserDetails(authUtil.getBearerToken(
            context.getTransientObject(AUTH_TOKEN_JSON_KEY).toString()
        ));
        return isRespondentUser(userDetails);
    }

    private boolean isRespondentUser(UserDetails userDetails) {
        return userDetails != null
            && CollectionUtils.isNotEmpty(userDetails.getRoles())
            && userDetails.getRoles()
            .stream()
            .anyMatch(this::isLetterHolderRole);
    }

    private boolean isLetterHolderRole(String role) {
        return StringUtils.isNotBlank(role)
            && role.startsWith("letter")
            && !role.endsWith("loa1");
    }
}
