package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.in.ExceptionRecord;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.validation.in.OcrDataField;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformations.D8FormToCaseTransformer;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class D8FormToCaseTransformerTest {

    private final D8FormToCaseTransformer classUnderTest = new D8FormToCaseTransformer();

    @Test
    public void shouldReplaceCcdFieldForD8PaymentMethodIfPaymentMethodIsDebitCreditCard() {

        ExceptionRecord exceptionRecord = new ExceptionRecord(
                "",
                "",
                "",
                Collections.singletonList(new OcrDataField("D8PaymentMethod", "debit/credit card")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(exceptionRecord);

        assertEquals(transformedCaseData.get("D8PaymentMethod"), "card");
    }

    @Test
    public void shouldNotReplaceCcdFieldForD8PaymentMethodIfMethodIsCheque() {

        ExceptionRecord incomingExceptionRecord = new ExceptionRecord(
                "",
                "",
                "",
                Collections.singletonList(new OcrDataField("D8PaymentMethod", "cheque")));

        Map<String, Object> transformedCaseData = classUnderTest.transformIntoCaseData(incomingExceptionRecord);

        assertEquals(transformedCaseData.get("D8PaymentMethod"), "cheque");
    }
}