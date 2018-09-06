package uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSummary {

    @JsonProperty("PaymentReference")
    private String paymentReference;

    @JsonProperty("PaymentTotal")
    private String paymentTotal;

    @JsonProperty("Fees")
    private List<FeeItem> fees;

    public void add(FeeResponse... fees) {
        NumberFormat formatter = new DecimalFormat("#0");
        List<FeeItem> feesItems = new ArrayList<>();
        FeeValue value = new FeeValue();
        FeeItem feeItem = new FeeItem();
        for (FeeResponse fee : fees) {
            if (fee != null) {
                value.setFeeAmount(String.valueOf(formatter.format(fee.getAmount() * 100)));
                value.setFeeCode(fee.getFeeCode());
                value.setFeeDescription(fee.getDescription());
                value.setFeeVersion(String.valueOf(fee.getVersion()));
                feeItem.setValue(value);
                feesItems.add(feeItem);
            }
        }
        this.setFees(feesItems);
        double sum = Arrays.asList(fees).stream().mapToDouble(FeeResponse::getAmount).sum() * 100;
        this.setPaymentTotal(String.valueOf(formatter.format(sum)));
    }

}
