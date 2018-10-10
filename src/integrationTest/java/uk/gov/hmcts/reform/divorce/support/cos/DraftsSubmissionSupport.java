package uk.gov.hmcts.reform.divorce.support.cos;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;


@Slf4j
@Component
public class DraftsSubmissionSupport {

    @Autowired
    private CosApiClient cosApiClient;

    public Map<String, Object> getUserDraft(UserDetails userDetails, boolean checkCcd) {
        return cosApiClient.getDraft(userDetails.getAuthToken(), checkCcd);
    }

    public void saveDraft(UserDetails userDetails, String fileName) {
        JsonNode draftResource = ResourceLoader.loadJsonToObject(fileName, JsonNode.class);
        cosApiClient.saveDraft(userDetails.getAuthToken(), draftResource, userDetails.getEmailAddress());
    }

    public void deleteDraft(UserDetails userDetails) {
        cosApiClient.deleteDraft(userDetails.getAuthToken());
    }


    public Map<String, Object> submitCase(UserDetails userDetails, String fileName) {
        JsonNode draftResource = ResourceLoader.loadJsonToObject(fileName, JsonNode.class);
        return cosApiClient.submitCase(userDetails.getAuthToken(), draftResource);
    }
}
