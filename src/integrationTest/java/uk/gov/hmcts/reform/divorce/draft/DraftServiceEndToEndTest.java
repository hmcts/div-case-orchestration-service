package uk.gov.hmcts.reform.divorce.draft;

import feign.FeignException;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.DraftsSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

public class DraftServiceEndToEndTest extends IntegrationTest {

    private static final String SAVE_DRAFT_FILE = "draft/save-draft.json";
    private static final String DRAFT_PART_1_FILE = "draft/draft-part1.json";
    private static final String DRAFT_PART_1_RESPONSE_FILE = "draft/draft-part1-response.json";
    private static final String DRAFT_PART_2_FILE = "draft/draft-part2.json";
    private static final String DRAFT_PART_2_RESPONSE_FILE = "draft/draft-part2-response.json";
    private static final String DRAFT_WITH_DIVORCE_FORMAT_FILE = "draft/draft-with-divorce-format.json";

    private static final String BASE_CASE_TO_SUBMIT = "draft/basic-case.json";

    private static final String CMS_DATA_KEY = "data";

    @Autowired
    private DraftsSubmissionSupport draftsSubmissionSupport;

    @Autowired
    private CmsClientSupport cmsClientSupport;

    private UserDetails user;

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
        try {
            draftsSubmissionSupport.getUserDraft(user);
            fail("Resource not found error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        }
    }

    @Test
    public void givenUser_whenSaveDraft_thenCaseIsSavedInDraftStore() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);
    }

    @Test
    public void givenUserWithDraft_whenDeleteDraft_thenDraftIsDeleted() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.deleteDraft(user);

        try {
            draftsSubmissionSupport.getUserDraft(user);
            fail("Resource not found error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        }
    }

    @Test
    public void givenUserWithDraft_whenSubmitCase_thenDraftIsDeleted() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserDraft(DRAFT_WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.submitCase(user, BASE_CASE_TO_SUBMIT).get(CASE_ID_JSON_KEY);

        Map<String, Object> draftFromCMS = cmsClientSupport.getDrafts(user);
        List response = (List) draftFromCMS.get(CMS_DATA_KEY);
        assertEquals(0, response.size());
    }

    @Test
    public void givenUserWithDraft_whenUpdateDraft_thenDraftIsUpdated() {
        user = createCitizenUser();

        draftsSubmissionSupport.saveDraft(user, DRAFT_PART_1_FILE);
        assertUserDraft(DRAFT_PART_1_RESPONSE_FILE, user);

        draftsSubmissionSupport.saveDraft(user, DRAFT_PART_2_FILE);
        assertUserDraft(DRAFT_PART_2_RESPONSE_FILE, user);
    }

    private void assertUserDraft(String draftFile, UserDetails user) {
        final Map<String, Object> expectedDraft = getDraftResponseResource(draftFile);
        Map<String, Object> userDraft = draftsSubmissionSupport.getUserDraft(user);

        assertEquals(expectedDraft, userDraft);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDraftResponseResource(String file) {
        return ResourceLoader.loadJsonToObject(file, Map.class);
    }
}
