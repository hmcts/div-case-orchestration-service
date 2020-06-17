package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_EXPECTED_DUE_DATE_FORMATTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.createCollectionMemberDocument;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskTestHelper.createGeneratedDocument;

public class CcdUtilUTest {

    private static final String CURRENT_DATE = "2018-01-01";
    private static final String PAYMENT_DATE = "01012018";
    private static final String EXPECTED_DATE_WITH_CUSTOMER_FORMAT = "1 January 2018";
    private static final LocalDateTime FIXED_DATE_TIME = LocalDate.parse(CURRENT_DATE).atStartOfDay();
    private static final String D8_DOCUMENTS_GENERATED_CCD_FIELD = "D8DocumentsGenerated";

    private Clock clock;

    private CcdUtil ccdUtil;

    @Before
    public void before() {
        clock = mock(Clock.class);
        when(clock.instant()).thenReturn(FIXED_DATE_TIME.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(UTC);

        ccdUtil = new CcdUtil(clock, getObjectMapperInstance());
    }

    @Test
    public void whenGetCurrentDate_thenReturnExpectedDate() {
        assertEquals(CURRENT_DATE, ccdUtil.getCurrentDateCcdFormat());
    }

    @Test
    public void whenCurrentDatePaymentPattern_thenReturnExpectedDate() {
        assertEquals(PAYMENT_DATE, ccdUtil.getCurrentDatePaymentFormat());
    }

    @Test
    public void whenGiveDateAsYyyyMmDd_thenReturnFormattedDate() throws TaskException {

        Map<String, Object> testCaseData = new HashMap<>();
        testCaseData.put(CO_RESPONDENT_DUE_DATE, TEST_EXPECTED_DUE_DATE);

        assertEquals(TEST_EXPECTED_DUE_DATE_FORMATTED, ccdUtil.getFormattedDueDate(testCaseData, CO_RESPONDENT_DUE_DATE));
    }

    @Test
    public void whenGetDisplayCurrentDate_thenReturnExpectedDate() {
        assertThat(ccdUtil.getCurrentDateWithCustomerFacingFormat(), is(EXPECTED_DATE_WITH_CUSTOMER_FORMAT));
    }

    @Test
    public void givenDateStringInPast_whenIsCcdDateTimeInThePast_thenReturnTrue() {
        String pastDate = LocalDateTime.now(clock).minusMonths(1).toString();
        assertTrue(ccdUtil.isCcdDateTimeInThePast(pastDate));
    }

    @Test
    public void givenDateStringIsToday_whenIsCcdDateTimeInThePast_thenReturnTrue() {
        String now = LocalDateTime.now(clock).toString();
        assertTrue(ccdUtil.isCcdDateTimeInThePast(now));
    }

    @Test
    public void givenDateStringInFuture_whenIsCcdDateTimeInThePast_thenReturnFalse() {
        String futureDate = LocalDateTime.now(clock).plusMonths(1).toString();
        assertFalse(ccdUtil.isCcdDateTimeInThePast(futureDate));
    }

    @Test
    public void givenLocalDate_whenParseDecreeAbsoluteEligibleDate_thenReturnParsedDateString() {
        LocalDate hearingDate = LocalDate.of(2000, 1, 1);
        // 6 weeks and 1 day
        String expectedDate = "2000-02-13";

        assertEquals(expectedDate, ccdUtil.parseDecreeAbsoluteEligibleDate(hearingDate));
    }

    @Test
    public void testParsingDateForCCD() {
        LocalDate localDate = CcdUtil.parseDateUsingCcdFormat("2019-05-13");

        assertThat(localDate.getDayOfMonth(), equalTo(13));
        assertThat(localDate.getMonth(), equalTo(Month.MAY));
        assertThat(localDate.getYear(), equalTo(2019));
    }

    @Test
    public void testFormattingDateForCCD() {
        LocalDate date = LocalDate.of(2019, Month.MAY, 13);

        String formattedDate = CcdUtil.formatDateForCCD(date);

        assertThat(formattedDate, equalTo("2019-05-13"));
    }

    @Test
    public void shouldConvertCCDDateTimeToLocalDateTime() {
        LocalDateTime localDateTime = CcdUtil.mapCCDDateTimeToLocalDateTime("2018-06-24T16:49:00.015");
        assertThat(localDateTime.getDayOfMonth(), is(24));
        assertThat(localDateTime.getMonth(), is(Month.JUNE));
        assertThat(localDateTime.getYear(), is(2018));
        assertThat(localDateTime.getHour(), is(16));
        assertThat(localDateTime.getMinute(), is(49));
        assertThat(localDateTime.getSecond(), is(0));
    }

    @Test
    public void shouldReturnCurrentLocalDateTime() {
        assertEquals(FIXED_DATE_TIME, ccdUtil.getCurrentLocalDateTime());
    }

    @Test
    public void givenNoGeneratedDocumentsInCaseData_whenAddNewDocuments_thenDocumentsAreAddedToCaseData() {
        final String url1 = "url1";
        final String documentType1 = "petition";
        final String fileName1 = "fileName1";
        final String url2 = "url1";
        final String documentType2 = "aos";
        final String fileName2 = "fileName2";
        final GeneratedDocumentInfo generatedDocumentInfo1 = createGeneratedDocument(url1, documentType1, fileName1);
        final GeneratedDocumentInfo generatedDocumentInfo2 = createGeneratedDocument(url2, documentType2, fileName2);

        Map<String, Object> actual = ccdUtil.addNewDocumentsToCaseData(new HashMap<>(), asList(generatedDocumentInfo1, generatedDocumentInfo2));

        assertThat((List<CollectionMember<Document>>) actual.get(D8_DOCUMENTS_GENERATED_CCD_FIELD), hasItems(
            createCollectionMemberDocument(url1, documentType1, fileName1),
            createCollectionMemberDocument(url2, documentType2, fileName2)
        ));
    }

    @Test
    public void shouldConvertDateFromCCDFormat() {
        String formattedDate = CcdUtil.formatFromCCDFormatToHumanReadableFormat("2017-08-15");
        Assert.assertThat(formattedDate, Is.is("15/08/2017"));
    }

    @Test
    public void givenConflictingD8DocumentsExistsInCaseData_whenAddDocuments_thenAddDocuments() {
        final String url1 = "url1";
        final String documentType1 = "petition";
        final String fileName1 = "fileName1";
        final String url2 = "url2";
        final String documentType2 = "aos";
        final String fileName2 = "fileName2";
        final String url3 = "url3";
        final String documentType3 = "aos1";
        final String fileName3 = "fileName3";
        final String url4 = "url4";
        final String documentType4 = "aos";
        final String fileName4 = "fileName4";
        final GeneratedDocumentInfo generatedDocumentInfo1 = createGeneratedDocument(url1, documentType1, fileName1);
        final GeneratedDocumentInfo generatedDocumentInfo2 = createGeneratedDocument(url2, documentType2, fileName2);

        final CollectionMember<Document> document1 = createCollectionMemberDocument(url1, documentType1, fileName1);
        final CollectionMember<Document> document2 = createCollectionMemberDocument(url2, documentType2, fileName2);
        final CollectionMember<Document> document3 = createCollectionMemberDocument(url3, documentType3, fileName3);
        final CollectionMember<Document> document4 = createCollectionMemberDocument(url4, documentType4, fileName4);

        final Map<String, Object> input = new HashMap<>();
        input.put(D8_DOCUMENTS_GENERATED_CCD_FIELD, asList(document3, document4));

        Map<String, Object> actual = ccdUtil.addNewDocumentsToCaseData(input, asList(generatedDocumentInfo1, generatedDocumentInfo2));

        assertThat((List<CollectionMember<Document>>) actual.get(D8_DOCUMENTS_GENERATED_CCD_FIELD), hasItems(document3, document1, document2));
    }

    @Test
    public void givenMultipleGenericD8DocumentsExistsInCaseData_whenAddDocuments_thenAddDocuments() {
        final String url1 = "url1";
        final String documentType1 = "petition";
        final String fileName1 = "fileName1";
        final String url2 = "url2";
        final String documentType2 = "aos";
        final String fileName2 = "fileName2";
        final String url3 = "url3";
        final String documentType3 = "other";
        final String fileName3 = "fileName3";
        final String url4 = "url4";
        final String documentType4 = "aos";
        final String fileName4 = "fileName4";
        final String url5 = "url5";
        final String documentType5 = "other";
        final String fileName5 = "fileName5";

        final GeneratedDocumentInfo generatedDocumentInfo1 = createGeneratedDocument(url1, documentType1, fileName1);
        final GeneratedDocumentInfo generatedDocumentInfo2 = createGeneratedDocument(url2, documentType2, fileName2);
        final GeneratedDocumentInfo generatedDocumentInfo3 = createGeneratedDocument(url3, documentType3, fileName3);

        final CollectionMember<Document> document1 = createCollectionMemberDocument(url1, documentType1, fileName1);
        final CollectionMember<Document> document2 = createCollectionMemberDocument(url2, documentType2, fileName2);
        final CollectionMember<Document> document3 = createCollectionMemberDocument(url3, documentType3, fileName3);
        final CollectionMember<Document> document4 = createCollectionMemberDocument(url4, documentType4, fileName4);
        final CollectionMember<Document> document5 = createCollectionMemberDocument(url5, documentType5, fileName5);

        final Map<String, Object> input = new HashMap<>();
        input.put(D8_DOCUMENTS_GENERATED_CCD_FIELD, asList(document4, document5));

        Map<String, Object> actual =
            ccdUtil.addNewDocumentsToCaseData(input, asList(generatedDocumentInfo1, generatedDocumentInfo2, generatedDocumentInfo3));

        List<CollectionMember<Document>> generatedDocuments = (List<CollectionMember<Document>>) actual.get(D8_DOCUMENTS_GENERATED_CCD_FIELD);
        assertThat(generatedDocuments, hasItems(document5, document1, document2, document3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void givenCoreCaseDataIsNull_whenAddDocuments_thenReturnThrowException() {
        ccdUtil.addNewDocumentsToCaseData(null, singletonList(GeneratedDocumentInfo.builder().build()));
    }

    @Test
    public void givenGeneratedDocumentInfoIsNull_whenAddDocuments_thenReturnSameCaseData() {
        final Map<String, Object> input = Collections.emptyMap();

        Map<String, Object> actual = ccdUtil.addNewDocumentsToCaseData(input, null);

        assertEquals(input, actual);
    }

    @Test
    public void givenGeneratedDocumentInfoIsEmpty_whenAddDocuments_thenReturnSameCaseData() {
        final Map<String, Object> input = Collections.emptyMap();

        Map<String, Object> actual = ccdUtil.addNewDocumentsToCaseData(input, Collections.emptyList());

        assertEquals(input, actual);
    }

}