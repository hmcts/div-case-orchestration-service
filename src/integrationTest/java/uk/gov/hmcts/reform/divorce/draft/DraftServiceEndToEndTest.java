package uk.gov.hmcts.reform.divorce.draft;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil;
import uk.gov.hmcts.reform.divorce.support.cms.CmsClientSupport;
import uk.gov.hmcts.reform.divorce.support.cos.DraftsSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.DateUtil;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DraftServiceEndToEndTest extends IntegrationTest {

    private static final String SAVE_DRAFT_FILE = "draft/save-draft.json";
    private static final String DRAFT_PART_1_FILE = "draft/draft-part1.json";
    private static final String DRAFT_PART_1_RESPONSE_FILE = "draft/draft-part1-response.json";
    private static final String DRAFT_PART_2_FILE = "draft/draft-part2.json";
    private static final String DRAFT_PART_2_RESPONSE_FILE = "draft/draft-part2-response.json";
    private static final String DRAFT__WITH_DIVORCE_FORMAT_FILE = "draft/draft-with-divorce-format.json";

    private static final String BASE_CASE_TO_SUBMIT = "draft/basic-case.json";
    private static final String BASE_CASE_RESPONSE = "draft/complete-case-response.json";

    private static final String NO_VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwib"
            + "mFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    private static final String PETITIONER_EMAIL_KEY = "D8PetitionerEmail";
    private static final String CREATED_DATE__KEY = "createdDate";

    private static final String CMS_DATA_KEY = "data";

    @Autowired
    private DraftsSubmissionSupport draftsSubmissionSupport;

    @Autowired
    private CmsClientSupport cmsClientSupport;

    private UserDetails user;

    @Before
    public void setUp() {
        user = createCitizenUser();
    }

    @After
    public void tearDown() {
        draftsSubmissionSupport.deleteDraft(user);
        user = null;
    }

    @Test
    public void givenNotAuthenticated_whenGetDraft_thenNotAuthenticatedError() {
        try {
            draftsSubmissionSupport.getUserDraft(UserDetails.builder()
                    .authToken(NO_VALID_TOKEN)
                    .build());
            fail("Not authenticated error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        }
    }

    @Test
    public void givenNotAuthenticated_whenSafeDraft_thenNotAuthenticatedError() {
        try {
            draftsSubmissionSupport.saveDraft(UserDetails.builder()
                    .authToken(NO_VALID_TOKEN)
                    .build(), SAVE_DRAFT_FILE);
            fail("Not authenticated error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        }
    }

    @Test
    public void givenNotAuthenticated_whenDeleteDraft_thenNotAuthenticatedError() {
        try {
            draftsSubmissionSupport.deleteDraft(UserDetails.builder()
                    .authToken(NO_VALID_TOKEN)
                    .build());
            fail("Not authenticated error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.FORBIDDEN.value(), error.status());
        }
    }

    @Test
    public void givenUserWithoutDraft_whenRetrieveDraft_thenReturn404Status() {
        try {
            draftsSubmissionSupport.getUserDraft(user);
            fail("Resource not found error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        }
    }

    @Test
    public void givenUser_whenSaveDraft_thenCaseIsSavedInDraftStore() {
        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserPetition(DRAFT__WITH_DIVORCE_FORMAT_FILE, user);
    }

    @Test
    public void givenUserWithDraft_whenDeleteDraft_thenDraftIsDeleted() {
        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserPetition(DRAFT__WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.deleteDraft(user);

        try {
            draftsSubmissionSupport.getUserDraft(user);
            fail("Resource not found error expected");
        } catch (FeignException error) {
            assertEquals(HttpStatus.NOT_FOUND.value(), error.status());
        }
    }

    @Test
    public void givenUserWithDraft_whenSubmitCase_thenDraftIsDeletedAndCaseExist() {
        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserPetition(DRAFT__WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.submitCase(user, BASE_CASE_TO_SUBMIT);

        Map<String, Object> draftFromCMS = cmsClientSupport.getDrafts(user);
        List response = (List) draftFromCMS.get(CMS_DATA_KEY);
        assertTrue(response.isEmpty());

        assertUserPetition(BASE_CASE_RESPONSE, user);
    }

    @Test
    public void givenUserWithDraftAfterSubmittedCase_whenGetDraft_thenCaseIsReturned() {
        draftsSubmissionSupport.saveDraft(user, SAVE_DRAFT_FILE);

        assertUserPetition(DRAFT__WITH_DIVORCE_FORMAT_FILE, user);

        draftsSubmissionSupport.submitCase(user, BASE_CASE_TO_SUBMIT);
        Map<String, Object> draftFromCMS = cmsClientSupport.getDrafts(user);
        List response = (List) draftFromCMS.get(CMS_DATA_KEY);
        assertTrue(response.isEmpty());

        cmsClientSupport.saveDrafts(SAVE_DRAFT_FILE, user);

        draftFromCMS = cmsClientSupport.getDrafts(user);
        response = (List) draftFromCMS.get(CMS_DATA_KEY);
        assertEquals(1, response.size());

        assertUserPetition(BASE_CASE_RESPONSE, user);
    }

    @Test
    public void givenUserWithDraft_whenUpdateDraft_thenDraftIsUpdated() {
        draftsSubmissionSupport.saveDraft(user, DRAFT_PART_1_FILE);
        assertUserPetition(DRAFT_PART_1_RESPONSE_FILE, user);

        draftsSubmissionSupport.saveDraft(user, DRAFT_PART_2_FILE);
        assertUserPetition(DRAFT_PART_2_RESPONSE_FILE, user);
    }

    private void assertUserPetition(String draftFile, UserDetails user) {
        final Map<String, Object> expectedDraft = getDraftResponseResource(draftFile, user);
        Map<String, Object> userDraft = draftsSubmissionSupport.getUserDraft(user);
        assertEquals(expectedDraft, userDraft);
    }

    private Map<String, Object> getDraftResponseResource(String file, UserDetails user) {
        Map<String, Object> expectedDraft = ResourceLoader.loadJsonToObject(file,
                Map.class);
        expectedDraft.put(CREATED_DATE__KEY, DateUtil.parseCurrentDate());
        expectedDraft.put(PETITIONER_EMAIL_KEY, user.getEmailAddress());
        return expectedDraft;
    }

}
