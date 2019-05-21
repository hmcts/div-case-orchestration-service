package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@Component
@Slf4j
public class FetchPrintDocsFromDmStore implements Task<Map<String, Object>> {

    private static final String DOCUMENT_LINK = "DocumentLink";

    private static final String VALUE = "value";

    private static final String DOCUMENT_URL = "document_binary_url";

    private static final String DOCUMENT_TYPE = "DocumentType";

    private static final String DOCUMENT_FILENAME = "document_filename";

    private static final String DOCUMENTS_GENERATED = "DocumentsGenerated";

    private static final String D8DOCUMENTS_GENERATED = "D8DocumentsGenerated";

    private static final String CASEWORKER_DIVORCE = "caseworker-divorce";

    private static final String USER_ROLES = "user-roles";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private AuthTokenGenerator authTokenGenerator;

    public FetchPrintDocsFromDmStore(AuthTokenGenerator authTokenGenerator) {
        this.authTokenGenerator = authTokenGenerator;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = extractGeneratedDocumentList(caseData);
        populateDocumentBytes(context, generatedDocumentInfoList);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocumentInfoList);
        return caseData;
    }

    private void populateDocumentBytes(TaskContext context, Map<String, GeneratedDocumentInfo> generatedDocumentInfos) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        for (GeneratedDocumentInfo generatedDocumentInfo : generatedDocumentInfos.values()) {

            byte[] bytes = null;
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build()) {
                HttpGet request = new HttpGet(generatedDocumentInfo.getUrl());
                request.setHeader(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
                request.setHeader(USER_ROLES, CASEWORKER_DIVORCE);
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(request);
                InputStream is = closeableHttpResponse.getEntity().getContent();

                byte[] byteChunk = new byte[4096];
                int readBytes;
                while ( (readBytes = is.read(byteChunk)) > 0 ) {
                    baos.write(byteChunk, 0, readBytes);
                }
                bytes = baos.toByteArray();

            } catch (IOException e) {

                log.error("Failed to get bytes from document store for document {} in case Id {}",
                    generatedDocumentInfo.getUrl(), caseDetails.getCaseId());
            }
            generatedDocumentInfo.setBytes(bytes);
        }
    }

    /**
     * I'm not using object mapper here to keep it consistent with rest of code, when we migrate the formatter
     * service to as module dependency this method could be simplified.
     */
    @SuppressWarnings("unchecked")
    private Map<String, GeneratedDocumentInfo> extractGeneratedDocumentList(Map<String, Object> caseData) {
        List<Map> documentList =
            ofNullable(caseData.get(D8DOCUMENTS_GENERATED)).map(i -> (List<Map>) i).orElse(new ArrayList<>());
        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = new HashMap<>();
        for (Map<String, Object> document : documentList) {
            Map<String, Object> value = ((Map) document.get(VALUE));
            String documentType = getStringValue(value, DOCUMENT_TYPE);
            Map<String, Object> documentLink =
                ofNullable(getValue(value, DOCUMENT_LINK)).map(obj -> (Map) obj).orElse(null);

            if (ofNullable(documentLink).isPresent()) {
                GeneratedDocumentInfo gdi = GeneratedDocumentInfo.builder()
                    .documentType(getStringValue(value, DOCUMENT_TYPE))
                    .url(getStringValue(documentLink, DOCUMENT_URL))
                    .documentType(documentType)
                    .fileName(getStringValue(documentLink, DOCUMENT_FILENAME))
                    .build();
                generatedDocumentInfoList.put(documentType, gdi);
            }
        }

        return generatedDocumentInfoList;
    }

    private Object getValue(Map<String, Object> objectMap, String key) {
        Iterator<Map.Entry<String, Object>> iterator = objectMap.entrySet().iterator();
        Object result = null;
        while (iterator.hasNext()) {
            Map.Entry map = iterator.next();
            if (map.getKey().equals(key)) {
                result = map.getValue();
            }
        }
        return result;
    }

    private String getStringValue(Map<String, Object> objectMap, String key) {
        return ofNullable(getValue(objectMap, key)).map(Object::toString).orElse(StringUtils.EMPTY);
    }

}
