package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.JudgeTypesConfig;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;

import java.util.Map;
import java.util.Optional;

@Component
public class JudgeTypesLookupService {

    private Map<String, String> judgeTypes;

    @Autowired
    public JudgeTypesLookupService(JudgeTypesConfig judgeTypesConfig) {
        this.judgeTypes = judgeTypesConfig.getTypes();
    }

    public String getJudgeTypeByCode(String judgeTypeCode) throws JudgeTypeNotFoundException {
        return Optional.ofNullable(judgeTypes.get(judgeTypeCode))
            .orElseThrow(() -> new JudgeTypeNotFoundException(judgeTypeCode));
    }
}
