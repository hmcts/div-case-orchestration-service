package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.document.Addressee;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.buildFullName;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

/*
 * data for template: FL-FRM-APP-ENG-00009.docx
 */
@Component
public class PrepareDataForDaGrantedLetterGenerationTask extends PrepareDataForDocumentGenerationTask {

    public static class CaseDataKeys {
        public static final String DA_GRANTED_DATE = OrchestrationConstants.DECREE_ABSOLUTE_GRANTED_DATE_CCD_FIELD;
        public static final String PETITIONER_FIRST_NAME = OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
        public static final String PETITIONER_LAST_NAME = OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
        public static final String RESPONDENT_FIRST_NAME = OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
        public static final String RESPONDENT_LAST_NAME = OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;
        public static final String ADDRESS_LINE1 = "AddressLine1";
        public static final String ADDRESS_LINE2 = "AddressLine2";
        public static final String TOWN = "PostTown";
        public static final String COUNTY = "County";
        public static final String POSTCODE = "PostCode";
    }

    @Override
    public void addPreparedDataToContext(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DaGrantedLetter daGrantedLetterData = DaGrantedLetter.builder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(getCtscContactDetails())
            .addressee(getAddressee(caseData))
            .letterDate(getDaGrantedDate(caseData))
            .petitionerFullName(getPetitionerFullName(caseData))
            .respondentFullName(getRespondentFullName(caseData))
            .build();

        context.setTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION,
            daGrantedLetterData
        );
    }

    private String getDaGrantedDate(Map<String, Object> caseData) throws TaskException {
        return getMandatoryPropertyValueAsString(caseData, CaseDataKeys.DA_GRANTED_DATE);
    }

    // to do - populate
    private CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails.builder().build();
    }

    private Addressee getAddressee(Map<String, Object> caseData) throws TaskException {
        return Addressee.builder()
            .name(getRespondentFullName(caseData))
            .formattedAddress(formatAddressForLetterPrinting(caseData))
            .build();
    }

    private String getRespondentFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.RESPONDENT_FIRST_NAME, CaseDataKeys.RESPONDENT_LAST_NAME);
    }

    private String getPetitionerFullName(Map<String, Object> caseData) {
        return buildFullName(caseData, CaseDataKeys.PETITIONER_FIRST_NAME, CaseDataKeys.PETITIONER_LAST_NAME);
    }

    private String formatAddressForLetterPrinting(Map<String, Object> address) throws TaskException {
        List<String> addressLines = new ArrayList<>();

        addressLines.add(getMandatoryPropertyValueAsString(address, CaseDataKeys.ADDRESS_LINE1));
        addressLines.add(getMandatoryPropertyValueAsString(address, CaseDataKeys.ADDRESS_LINE2));
        addressLines.add(getMandatoryPropertyValueAsString(address, CaseDataKeys.TOWN));
        addressLines.add(getMandatoryPropertyValueAsString(address, CaseDataKeys.COUNTY));
        addressLines.add(getMandatoryPropertyValueAsString(address, CaseDataKeys.POSTCODE));
        addressLines.removeAll(Arrays.asList("", null, "null"));

        return String.join("\n", addressLines);
    }
}
