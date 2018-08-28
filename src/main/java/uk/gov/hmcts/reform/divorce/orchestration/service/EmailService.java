package uk.gov.hmcts.reform.divorce.orchestration.service;

import java.util.Map;

public interface EmailService {
    public Map<String, Object> sendEmail(String authToken);
}
