package uk.gov.hmcts.reform.divorce.draft;

import feign.FeignException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.DraftsSubmissionSupport;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public class DraftServiceEndToEndTest extends IntegrationTest {

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
    private Map draftResource;

    @Before
    public void setUp() {
        draftResource = loadJsonToObject(SAVE_DRAFT_FILE, Map.class);
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
        user = createCitizenUser();

        FeignException expectedException = assertThrows("Resource not found error expected", FeignException.class,
            () -> draftsSubmissionSupport.getUserDraft(user));
        assertEquals(HttpStatus.NOT_FOUND.value(), expectedException.status());
    }

    @Test
    public void givenUser_whenSaveDraft_thenCaseIsSavedInDraftStore() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, draftResource);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);
    }

    @Test
    public void givenUserWithDraft_whenDeleteDraft_thenDraftIsDeleted() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, draftResource);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.deleteDraft(user);

        FeignException expectedException = assertThrows("Resource not found error expected", FeignException.class,
            () -> draftsSubmissionSupport.getUserDraft(user));
        assertEquals(HttpStatus.NOT_FOUND.value(), expectedException.status());
    }

    @Test
    public void givenUserWithDraft_whenSubmitCase_thenDraftIsDeleted() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, draftResource);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.submitCase(user, BASE_CASE_TO_SUBMIT).get(CASE_ID_JSON_KEY);

        Map<String, Object> draftFromCMS = cmsClientSupport.getDrafts(user);
        List response = (List) draftFromCMS.get(DATA);
        assertEquals(0, response.size());
    }

    @Test
    public void givenUserWithDraft_whenUpdateDraft_thenDraftIsUpdated() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, loadJsonToObject(DRAFT_PART_1_FILE, Map.class));
        assertUserDraft(DRAFT_PART_1_RESPONSE_FILE, user);

        draftsSubmissionSupport.saveDraft(user, loadJsonToObject(DRAFT_PART_2_FILE, Map.class));
        assertUserDraft(DRAFT_PART_2_RESPONSE_FILE, user);
    }

    private void assertUserDraft(String draftFile, UserDetails user) {
        final Map<String, Object> expectedDraft = loadJsonToObject(draftFile, Map.class);
        final Map<String, Object> actualUserDraft = draftsSubmissionSupport.getUserDraft(user);

        //Assert transformation fields
        assertThat(actualUserDraft.get("court"), is(notNullValue()));
        JSONAssert.assertEquals(convertObjectToJsonString(expectedDraft), convertObjectToJsonString(actualUserDraft), false);
    }

}