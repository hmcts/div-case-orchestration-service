package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;

@Component
public class AuthenticateRespondent implements Task<Boolean> {
    @Autowired
    private IdamClient idamClient;

    @Override
    public Boolean execute(TaskContext context, Boolean payload, Object... params) {
        UserDetails userDetails = idamClient.retrieveUserDetails(AuthUtil.getBearToken(params[0].toString()));
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
            && !"letter-holder".equals(role)
            && !role.endsWith("loa1");
    }
}
