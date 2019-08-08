package uk.gov.hmcts.reform.divorce.support.cms;

import io.restassured.internal.MapCreator;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Component
@Slf4j
public class CmsClientSupport {

    private static final String CMS_URL_SEARCH = "/casemaintenance/version/1/search";

    @Autowired
    private CaseMaintenanceClient cmsClient;

    @Value("${case_maintenance.api.url}")
    private String cmsBaseUrl;


    public Map<String, Object> getDrafts(UserDetails userDetails) {
        return cmsClient.getDrafts(userDetails.getAuthToken());
    }

    @SuppressWarnings("unchecked")
    public void saveDrafts(String fileName, UserDetails userDetails) {
        Map<String, Object> draftResource = ResourceLoader.loadJsonToObject(fileName, Map.class);
        cmsClient.saveDraft(draftResource, userDetails.getAuthToken(), true);
    }

    /**
     * Search cases via CMS' search endpoint.
     * @param queryString a JSON-formatted query string
     * @param authToken An authentication token passed to the CMS
     * @return a collection of found cases or empty if none is found
     */
    public List<CaseDetails> searchCases(final String queryString, final String authToken) {

        log.debug("Search case with query [{}], auth [{}]",queryString, authToken);

        final String searchUrl = cmsBaseUrl + CMS_URL_SEARCH;
        log.debug("About to call [{}], auth token [{}]", searchUrl, authToken);

        // todo: cmsClient.searchCases is not used here the main reason is different domain objects CaseDetails and
        // SearchResult are defined in the COS and CMS (references ccd-store-client)
        Response response = RestUtil.postToRestService(searchUrl,
                buildCommonHeaders(HttpHeaders.AUTHORIZATION, authToken,
                        HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()),
                queryString,
                emptyMap());

        SearchResult searchResult = response.getBody().as(SearchResult.class);

        return searchResult.getCases();
    }

    private Map<String, Object> buildCommonHeaders(String firstHeaderName, Object firstHeaderValue,
                                                   Object ... headValuePairs) {
        return MapCreator.createMapFromParams(MapCreator.CollisionStrategy.MERGE,
                firstHeaderName, firstHeaderValue, headValuePairs);
    }
}
