package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import org.joda.time.LocalDate;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;

import java.io.IOException;

import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

public class DataTransformationTestHelper {

    public static DivorceSession getTestDivorceSessionData() throws IOException {
        return getJsonFromResourceFile("/jsonExamples/payloads/transformations/divorce/case-data.json", DivorceSession.class);
    }

    public static CoreCaseData getExpectedTranslatedCoreCaseData() throws IOException {
        CoreCaseData coreCaseData = getJsonFromResourceFile("/jsonExamples/payloads/transformations/ccd/case-data.json", CoreCaseData.class);
        coreCaseData.setCreatedDate(LocalDate.now().toString());
        return coreCaseData;
    }

    public static CoreCaseData getExpectedTranslatedCoreCaseDataRepresentedRespondentJourneyEnabled() throws IOException {
        CoreCaseData coreCaseData = getJsonFromResourceFile("/jsonExamples/payloads/transformations/ccd/case-data-rep-resp.json", CoreCaseData.class);
        coreCaseData.setCreatedDate(LocalDate.now().toString());
        return coreCaseData;
    }
}