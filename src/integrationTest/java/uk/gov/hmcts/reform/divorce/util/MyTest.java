package uk.gov.hmcts.reform.divorce.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.*;
import java.util.prefs.Preferences;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.*;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;

public class MyTest extends CcdSubmissionSupport {

    private UserDetails user;

    @Autowired
    CcdClientSupport ccdClientSupport;

    @Test
    public void test() {
        user = createCaseWorkerUser();
        Map<String, Object> bulkCase = new HashMap<>();
        List<Map<String, Object>> caselist = new ArrayList<>();
        Map<String, Object> caseObject =  new HashMap<>();
        Map<String, Object> link=  new HashMap<>();
        link.put("FieldId", ImmutableMap.of("CaseReference", "1234123412341234"));

        caseObject.put("CaseReference", ImmutableMap.of("CaseReference", "1556194967898930"));
        caseObject.put("CaseParties", "Petitioner");
        caseObject.put("CaseCreationDate", "2001-01-01");
        caseObject.put("IssuePronouncement", "YES");
        caselist.add(ImmutableMap.of("value", caseObject));
        caselist.add(ImmutableMap.of("value", caseObject));
        caselist.add(ImmutableMap.of("value", caseObject));
        bulkCase.put("CaseTitle", "Title");
        bulkCase.put("CaseList", caselist);
        ccdClientSupport.submitCase(bulkCase,user);
    }



}
