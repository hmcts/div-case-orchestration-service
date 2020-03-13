package uk.gov.hmcts.reform.divorce.orchestration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import javax.validation.Valid;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties("welsh")
public class WelshMonthTranslation {
    @Valid
    private Map<Integer, String> months;
}