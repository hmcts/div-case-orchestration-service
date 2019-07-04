package uk.gov.hmcts.reform.divorce.support.cms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;

import java.util.Map;

@Component
public class CmsClientSupport {

    @Autowired
    CaseMaintenanceClient cmsClient;

    public Map<String, Object> getDrafts(UserDetails userDetails) {
        return cmsClient.getDrafts(userDetails.getAuthToken());
    }
}
