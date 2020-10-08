package uk.gov.hmcts.reform.divorce.support.cos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

@Slf4j
@Component
public class DraftsSubmissionSupport {

    @Autowired
    private CosApiClient cosApiClient;

    public Map<String, Object> getUserDraft(UserDetails userDetails) {
        return cosApiClient.getDraft(userDetails.getAuthToken());
    }

    public void saveDraft(UserDetails userDetails, Map<String, Object> draftResource) {
        cosApiClient.saveDraft(userDetails.getAuthToken(), draftResource, Boolean.TRUE.toString());
    }

    public void deleteDraft(UserDetails userDetails) {
        cosApiClient.deleteDraft(userDetails.getAuthToken());
    }


    public Map<String, Object> submitCase(UserDetails userDetails, String fileName) {
        Map<String, Object> draftResource = ResourceLoader.loadJsonToObject(fileName, Map.class);
        return cosApiClient.submitCase(userDetails.getAuthToken(), draftResource);
    }

}