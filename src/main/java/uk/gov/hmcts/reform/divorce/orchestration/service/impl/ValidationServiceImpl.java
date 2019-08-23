package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.Result;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.ValidationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.ValidationService;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    @Qualifier("D8RuleBook")
    private RuleBook<List<String>> d8RuleBook;

    @Autowired
    @Qualifier("SessionRuleBook")
    private RuleBook<List<String>> divorceSessionRuleBook;

    @Override
    public ValidationResponse validate(final ValidationRequest validationRequest) {
        ObjectMapper mapper = new ObjectMapper();

        ValidationResponse validationResponse = ValidationResponse.builder()
            .validationStatus("success")
            .build();

        if (validationRequest.getFormId().contains("case")) {
            log.info("Validating CoreCaseData");
            NameValueReferableMap<CoreCaseData> facts = new FactMap<>();
            facts.setValue("coreCaseData", mapper.convertValue(validationRequest.getData(), CoreCaseData.class));
            d8RuleBook.setDefaultResult(new ArrayList<>());
            d8RuleBook.run(facts);
            d8RuleBook.getResult().map(Result::getValue).ifPresent(result -> errorResponse(validationResponse, result));
        }

        if (validationRequest.getFormId().contains("session")) {
            log.info("Validating DivorceSession");
            NameValueReferableMap<DivorceSession> facts = new FactMap<>();
            facts.setValue("divorceSession", mapper.convertValue(validationRequest.getData(), DivorceSession.class));
            divorceSessionRuleBook.setDefaultResult(new ArrayList<>());
            divorceSessionRuleBook.run(facts);
            divorceSessionRuleBook.getResult().map(Result::getValue)
                .ifPresent(result -> errorResponse(validationResponse, result));
        }

        return validationResponse;
    }

    private void errorResponse(ValidationResponse validationResponse, List<String> result) {
        if (!result.isEmpty()) {
            validationResponse.setErrors(result);
            validationResponse.setValidationStatus("failed");
        }
    }
}
