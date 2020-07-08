package uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSummary {

    private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance();

    @JsonProperty("PaymentReference")
    private String paymentReference;

    @JsonProperty("PaymentTotal")
    private String paymentTotal;

    @JsonProperty("Fees")
    private List<FeeItem> fees;

    public void add(FeeResponse... fees) {
        List<FeeItem> feesItems = new ArrayList<>();
        FeeValue value = new FeeValue();
        FeeItem feeItem = new FeeItem();
        for (FeeResponse fee : fees) {
            if (fee != null) {
                value.setFeeAmount(getValueInPence(fee.getAmount()));
                value.setFeeCode(fee.getFeeCode());
                value.setFeeDescription(fee.getDescription());
                value.setFeeVersion(String.valueOf(fee.getVersion()));
                feeItem.setValue(value);
                feesItems.add(feeItem);
            }
        }
        this.setFees(feesItems);
        double totalAmount = Arrays.stream(fees).mapToDouble(FeeResponse::getAmount).sum();
        this.setPaymentTotal(getValueInPence(totalAmount));
    }

    @JsonIgnore
    public String getPaymentTotalInPounds() {
        return NUMBER_FORMATTER.format(new BigDecimal(paymentTotal).movePointLeft(2));
    }

    private static String getValueInPence(double value) {
        return BigDecimal.valueOf(value).movePointRight(2).toPlainString();
    }

}