package uk.gov.hmcts.reform.divorce.orchestration.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.DivorceGeneralOrder;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@SuppressWarnings("squid:S1118")
@AllArgsConstructor
@Component
public class CcdUtil {
    private static final String UK_HUMAN_READABLE_DATE_FORMAT = "dd/MM/yyyy";
    private static final String PAYMENT_DATE_PATTERN = "ddMMyyyy";

    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final LocalDateToWelshStringConverter localDateToWelshStringConverter;

    public String getCurrentDateCcdFormat() {
        return LocalDate.now(clock).format(DateUtils.Formatters.CCD_DATE);
    }

    public String getCurrentDatePaymentFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN, DateUtils.Settings.LOCALE));
    }

    public String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateUtils.Formatters.CCD_DATE)
            .format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN, DateUtils.Settings.LOCALE));
    }

    public static String mapDivorceDateTimeToCCDDateTime(LocalDateTime dateTime) {
        return DateUtils.formatDateTimeForCcd(dateTime);
    }

    public static LocalDateTime mapCCDDateTimeToLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }

    public String getCurrentDateWithCustomerFacingFormat() {
        return DateUtils.formatDateWithCustomerFacingFormat(LocalDate.now(clock));
    }

    public String getFormattedDueDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        LocalDate dueDate = getLocalDate(caseData, dateToFormat);
        return DateUtils.formatDateWithCustomerFacingFormat(dueDate);
    }

    public String getWelshFormattedDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        LocalDate localDate = getLocalDate(caseData, dateToFormat);
        return localDateToWelshStringConverter.convert(localDate);
    }

    private LocalDate getLocalDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        String dateAsString = getMandatoryPropertyValueAsString(caseData, dateToFormat);
        return LocalDate.parse(dateAsString);
    }

    public boolean isCcdDateTimeInThePast(String date) {
        return LocalDateTime.parse(date).toLocalDate().isBefore(LocalDate.now(clock).plusDays(1));
    }

    public String parseDecreeAbsoluteEligibleDate(LocalDate grantedDate) {
        return DateUtils.formatDateFromLocalDate(
            grantedDate.plusWeeks(6).plusDays(1)
        );
    }

    public static LocalDate parseDateUsingCcdFormat(String date) {
        return LocalDate.parse(date, DateUtils.Formatters.CCD_DATE);
    }

    public static String formatDateForCCD(LocalDate plus) {
        return plus.format(DateUtils.Formatters.CCD_DATE);
    }

    public static String formatFromCCDFormatToHumanReadableFormat(String inputDate) {
        LocalDate localDate = parseDateUsingCcdFormat(inputDate);
        return localDate.format(DateTimeFormatter.ofPattern(UK_HUMAN_READABLE_DATE_FORMAT, DateUtils.Settings.LOCALE));
    }

    public static String retrieveAndFormatCCDDateFieldIfPresent(String fieldName, Map<String, Object> caseData, String defaultValue) {
        return Optional.ofNullable(caseData.get(fieldName))
            .map((String.class::cast))
            .map(CcdUtil::formatFromCCDFormatToHumanReadableFormat)
            .orElse(defaultValue);
    }

    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(clock);
    }

    public Map<String, Object> addNewDocumentsToCaseData(Map<String, Object> existingCaseData, List<GeneratedDocumentInfo> newDocumentsToAdd) {
        if (existingCaseData == null) {
            throw new IllegalArgumentException("Existing case data must not be null.");
        }

        if (CollectionUtils.isNotEmpty(newDocumentsToAdd)) {
            Set<String> newDocumentsTypes = newDocumentsToAdd.stream()
                .map(GeneratedDocumentInfo::getDocumentType)
                .collect(Collectors.toSet());

            List<CollectionMember<Document>> documentsGenerated = getCollectionMembersOrEmptyList(
                objectMapper, existingCaseData, D8DOCUMENTS_GENERATED);

            List<CollectionMember<Document>> resultDocuments = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(documentsGenerated)) {
                List<CollectionMember<Document>> existingDocuments = documentsGenerated.stream()
                    .filter(documentCollectionMember ->
                        DOCUMENT_TYPE_OTHER.equals(documentCollectionMember.getValue().getDocumentType())
                            || !newDocumentsTypes.contains(documentCollectionMember.getValue().getDocumentType())
                    )
                    .collect(Collectors.toList());

                resultDocuments.addAll(existingDocuments);
            }

            List<CollectionMember<Document>> newDocuments =
                newDocumentsToAdd.stream()
                    .map(CcdMappers::mapDocumentInfoToCcdDocument)
                    .collect(Collectors.toList());
            resultDocuments.addAll(newDocuments);

            existingCaseData.put(D8DOCUMENTS_GENERATED, resultDocuments);
        }

        return existingCaseData;
    }

    public Map<String, Object> addNewDocumentToCollection(Map<String, Object> caseData, GeneratedDocumentInfo document, String field) {
        if (caseData == null || document == null || field == null) {
            throw new IllegalArgumentException("Invalid input. No nulls allowed.");
        }

        CollectionMember<Document> documentCollectionMemberToAdd = CcdMappers.mapDocumentInfoToCcdDocument(document);

        List<CollectionMember<Document>> allDocuments = getCollectionMembersOrEmptyList(objectMapper, caseData, field);
        allDocuments.add(documentCollectionMemberToAdd);

        Map<String, Object> copiedMap = new HashMap<>(caseData);
        copiedMap.put(field, allDocuments);

        return copiedMap;
    }

    public static List<CollectionMember<Document>> getCollectionMembersOrEmptyList(ObjectMapper objectMapper,
                                                                                   Map<String, Object> caseData,
                                                                                   String field) {
        return Optional.ofNullable(caseData.get(field))
            .map(i -> objectMapper.convertValue(i, new TypeReference<List<CollectionMember<Document>>>() {
            }))
            .orElse(new ArrayList<>());
    }

    public List<CollectionMember<DivorceGeneralOrder>> getListOfCollectionMembers(
        Map<String, Object> caseData, String field
    ) {
        return Optional.ofNullable(caseData.get(field))
            .map(i -> objectMapper.convertValue(i, new TypeReference<List<CollectionMember<DivorceGeneralOrder>>>() {
            }))
            .orElse(new ArrayList<>());
    }
}
