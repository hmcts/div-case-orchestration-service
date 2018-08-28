package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {
    @Override
    public Map<String, Object> sendEmail(String authToken) {
        //TODO - should send email using notification service
        return new HashMap<String, Object>();
    }
}
