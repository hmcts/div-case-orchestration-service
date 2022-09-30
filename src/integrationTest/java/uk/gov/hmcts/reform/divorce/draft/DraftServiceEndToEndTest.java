package uk.gov.hmcts.reform.divorce.draft;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.FeignException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.DraftsSubmissionSupport;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public class DraftServiceEndToEndTest extends CcdSubmissionSupport {

    private static final String SAVE_DRAFT_FILE = "draft/save-draft.json";
    private static final String DRAFT_PART_1_FILE = "draft/draft-part1.json";
    private static final String DRAFT_PART_1_RESPONSE_FILE = "draft/draft-part1-response.json";
    private static final String DRAFT_PART_2_FILE = "draft/draft-part2.json";
    private static final String DRAFT_PART_2_RESPONSE_FILE = "draft/draft-part2-response.json";
    private static final String DRAFT_WITH_DIVORCE_FORMAT_FILE = "draft/draft-with-divorce-format.json";
    private static final String BASE_CASE_TO_SUBMIT = "draft/basic-case.json";

    @Autowired
    private DraftsSubmissionSupport draftsSubmissionSupport;

    @Autowired
    private CmsClientSupport cmsClientSupport;

    private UserDetails user;
    private Map<String, Object> draftResource;

    @Before
    public void setUp() {
        draftResource = loadJsonToObject(SAVE_DRAFT_FILE, Map.class);
        user = createCitizenUser();
    }

    @After
    public void tearDown() {
        if (user != null) {
            draftsSubmissionSupport.deleteDraft(user);
        }
        user = null;
    }

    @Test
    public void givenUserWithoutDraft_whenRetrieveDraft_thenReturn404Status() {
        FeignException expectedException = assertThrows("Resource not found error expected", FeignException.class,
            () -> draftsSubmissionSupport.getUserDraft(user));
        assertEquals(HttpStatus.NOT_FOUND.value(), expectedException.status());
    }

    @Test
    public void givenUser_whenSaveDraft_thenCaseIsSavedInDraftStore() {
        draftsSubmissionSupport.saveDraft(user, draftResource);

        assertUserDraft(draftsSubmissionSupport.getUserDraft(user), loadJsonToObject(DRAFT_WITH_DIVORCE_FORMAT_FILE, Map.class));
    }

    @Test
    public void givenUserWithDraft_whenDeleteDraft_thenDraftIsDeleted() {
        draftsSubmissionSupport.saveDraft(user, draftResource);

        assertUserDraft(draftsSubmissionSupport.getUserDraft(user), loadJsonToObject(DRAFT_WITH_DIVORCE_FORMAT_FILE, Map.class));

        draftsSubmissionSupport.deleteDraft(user);

        FeignException expectedException = assertThrows("Resource not found error expected", FeignException.class,
            () -> draftsSubmissionSupport.getUserDraft(user));
        assertEquals(HttpStatus.NOT_FOUND.value(), expectedException.status());
    }

    @Test
    public void givenUserWithDraft_whenUpdateDraft_thenDraftIsUpdated() {
        draftsSubmissionSupport.saveDraft(user, loadJsonToObject(DRAFT_PART_1_FILE, Map.class));
        assertUserDraft(draftsSubmissionSupport.getUserDraft(user), loadJsonToObject(DRAFT_PART_1_RESPONSE_FILE, Map.class));

        draftsSubmissionSupport.saveDraft(user, loadJsonToObject(DRAFT_PART_2_FILE, Map.class));
        assertUserDraft(draftsSubmissionSupport.getUserDraft(user), loadJsonToObject(DRAFT_PART_2_RESPONSE_FILE, Map.class));
    }

    private void assertUserDraft(Map<String, Object> actualUserDraft, Map<String, Object> expectedDraft) {
        //Assert transformation fields
        assertThat(actualUserDraft.get("court"), is(notNullValue()));
        JSONAssert.assertEquals(convertObjectToJsonString(expectedDraft), convertObjectToJsonString(actualUserDraft), false);
    }

    private String loadExpectedResponseWithoutExpiresAttribute() throws IOException {
        Map<String, Object> expectedCaseData = getJsonFromResourceFile("/fixtures/retrieve-case/divorce-session.json", new TypeReference<>() {
        });
        expectedCaseData.remove("expires");

        return convertObjectToJsonString(expectedCaseData).replace(USER_DEFAULT_EMAIL, user.getEmailAddress());
    }

}