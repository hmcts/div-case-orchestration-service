package uk.gov.hmcts.reform.divorce.orchestration.service;

import java.util.Map;

public interface SolicitorService {
    Map<String, Object> issuePersonalServicePack(Map<String, Object> divorceSession, String authToken, String testCaseId);
}
