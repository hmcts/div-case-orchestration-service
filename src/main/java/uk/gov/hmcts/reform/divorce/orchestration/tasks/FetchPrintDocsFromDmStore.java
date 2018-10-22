package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

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
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = extractGeneratedDocumentList(caseData);
        populateDocumentBytes(generatedDocumentInfoList);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocumentInfoList);
        return caseData;
    }

    private void populateDocumentBytes(Map<String, GeneratedDocumentInfo> generatedDocumentInfos) {
        for (GeneratedDocumentInfo generatedDocumentInfo : generatedDocumentInfos.values()) {

            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
            byte[] bytes = null;
            try {
                HttpGet request = new HttpGet(generatedDocumentInfo.getUrl());
                /** Enable this if you are running tests locally */
                //request.setConfig(getProxyConfig());
                request.setHeader(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
                request.setHeader(USER_ROLES, CASEWORKER_DIVORCE);
                CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(request);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                closeableHttpResponse.getEntity().writeTo(byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();

            } catch (IOException e) {

                log.error("Failed to get bytes from document store for document {}", generatedDocumentInfo.getUrl());
            }
            generatedDocumentInfo.setBytes(bytes);
        }
    }

    private RequestConfig getProxyConfig() {
        return RequestConfig.custom().setProxy(new HttpHost("proxyout.reform.hmcts.net",
            8080)).build();
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
            String documentType = getStringValue(value.entrySet(), DOCUMENT_TYPE);
            Map<String, Object> documentLink =
                ofNullable(getValue(value.entrySet(), DOCUMENT_LINK)).map(obj -> (Map) obj).orElse(null);

            if (ofNullable(documentLink).isPresent()) {
                GeneratedDocumentInfo gdi = GeneratedDocumentInfo.builder()
                    .documentType(getStringValue(value.entrySet(), DOCUMENT_TYPE))
                    .url(getStringValue(documentLink.entrySet(), DOCUMENT_URL))
                    .documentType(documentType)
                    .fileName(getStringValue(documentLink.entrySet(), DOCUMENT_FILENAME))
                    .build();
                generatedDocumentInfoList.put(documentType, gdi);
                log.info(gdi.toString());
            }
        }

        return generatedDocumentInfoList;
    }

    private Object getValue(Collection<Map.Entry<String, Object>> list, String key) {
        Iterator<Map.Entry<String, Object>> iterator = list.iterator();
        Object result = null;
        while (iterator.hasNext()) {
            Map.Entry map = iterator.next();
            if (map.getKey().equals(key)) {
                result = map.getValue();
            }
        }
        return result;
    }

    private String getStringValue(Collection<Map.Entry<String, Object>> list, String key) {
        return ofNullable(getValue(list, key)).map(Object::toString).orElse(StringUtils.EMPTY);
    }

}
