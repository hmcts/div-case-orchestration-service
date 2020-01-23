package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;
import uk.gov.hmcts.reform.bsp.common.model.shared.CaseDetails;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.OcrValidationResult;
import uk.gov.hmcts.reform.bsp.common.model.validation.out.ValidationStatus;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation.BulkScanFormTransformer;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.transformation.BulkScanFormTransformerFactory;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.BulkScanFormValidator;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.scan.validation.BulkScanFormValidatorFactory;

import java.util.List;
import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private BulkScanFormValidatorFactory bulkScanFormValidatorFactory;

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public OcrValidationResult validateBulkScanForm(String formType, List<OcrDataField> ocrDataFields) throws UnsupportedFormTypeException {
        BulkScanFormValidator formValidator = bulkScanFormValidatorFactory.getValidator(formType);
        return formValidator.validateBulkScanForm(ocrDataFields);
    }

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) throws UnsupportedFormTypeException {
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }

    public CaseDetails transformNewFormAndUpdateExistingCase(ExceptionRecord exceptionRecord, CaseDetails existingCase)
        throws UnsupportedFormTypeException, InvalidDataException {

        OcrValidationResult validationResult =  validateBulkScanForm(exceptionRecord.getFormType(), exceptionRecord.getOcrDataFields());

        if (!validationResult.getStatus().equals(ValidationStatus.SUCCESS)) {
            throw new InvalidDataException(
                String.format("Validation of exception record %s finished with status %s", exceptionRecord.getId(), validationResult.getStatus()),
                validationResult.getWarnings(),
                validationResult.getErrors()
            );
        }

        Map<String, Object> transformedCaseData = transformBulkScanForm(exceptionRecord);
        existingCase.getCaseData().putAll(transformedCaseData);

        // we need to update state here as well

        return existingCase;
    }

}