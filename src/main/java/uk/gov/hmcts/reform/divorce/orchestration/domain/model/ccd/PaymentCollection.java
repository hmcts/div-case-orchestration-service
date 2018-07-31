package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.divorceapplicationdata.Payment;

@Data
@Builder
public class PaymentCollection {

    private String id;

    private Payment value;
}