package uk.gov.hmcts.reform.divorce.orchestration.util.payment;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
@Getter
public class PaymentContactDetail {
    @Value("${pba.contact.phoneNumber}")
    private String pbaContactPhoneNumber;

    @Value("${pba.contact.email}")
    private String pbaContactEmail;

}
