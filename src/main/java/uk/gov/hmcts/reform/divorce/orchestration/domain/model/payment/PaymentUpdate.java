
package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "_links",
    "account_number",
    "amount",
    "case_reference",
    "ccd_case_number",
    "channel",
    "currency",
    "customer_reference",
    "date_created",
    "date_updated",
    "description",
    "external_provider",
    "external_reference",
    "fees",
    "giro_slip_no",
    "id",
    "method",
    "organisation_name",
    "payment_group_reference",
    "payment_reference",
    "reference",
    "reported_date_offline",
    "service_name",
    "site_id",
    "status",
    "status_histories"
})
public class PaymentUpdate {

    @JsonProperty("_links")
    private Links links;
    @JsonProperty("account_number")
    private String accountNumber;
    @JsonProperty("amount")
    private Integer amount;
    @JsonProperty("case_reference")
    private String caseReference;
    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;
    @JsonProperty("channel")
    private String channel;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("customer_reference")
    private String customerReference;
    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("date_updated")
    private String dateUpdated;
    @JsonProperty("description")
    private String description;
    @JsonProperty("external_provider")
    private String externalProvider;
    @JsonProperty("external_reference")
    private String externalReference;
    @JsonProperty("fees")
    private List<Fee> fees = null;
    @JsonProperty("giro_slip_no")
    private String giroSlipNo;
    @JsonProperty("id")
    private String id;
    @JsonProperty("method")
    private String method;
    @JsonProperty("organisation_name")
    private String organisationName;
    @JsonProperty("payment_group_reference")
    private String paymentGroupReference;
    @JsonProperty("payment_reference")
    private String paymentReference;
    @JsonProperty("reference")
    private String reference;
    @JsonProperty("reported_date_offline")
    private String reportedDateOffline;
    @JsonProperty("service_name")
    private String serviceName;
    @JsonProperty("site_id")
    private String siteId;
    @JsonProperty("status")
    private String status;
    @JsonProperty("status_histories")
    private List<StatusHistory> statusHistories = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("_links")
    public Links getLinks() {
        return links;
    }

    @JsonProperty("_links")
    public void setLinks(Links links) {
        this.links = links;
    }

    @JsonProperty("account_number")
    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonProperty("account_number")
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @JsonProperty("amount")
    public Integer getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @JsonProperty("case_reference")
    public String getCaseReference() {
        return caseReference;
    }

    @JsonProperty("case_reference")
    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }

    @JsonProperty("ccd_case_number")
    public String getCcdCaseNumber() {
        return ccdCaseNumber;
    }

    @JsonProperty("ccd_case_number")
    public void setCcdCaseNumber(String ccdCaseNumber) {
        this.ccdCaseNumber = ccdCaseNumber;
    }

    @JsonProperty("channel")
    public String getChannel() {
        return channel;
    }

    @JsonProperty("channel")
    public void setChannel(String channel) {
        this.channel = channel;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("customer_reference")
    public String getCustomerReference() {
        return customerReference;
    }

    @JsonProperty("customer_reference")
    public void setCustomerReference(String customerReference) {
        this.customerReference = customerReference;
    }

    @JsonProperty("date_created")
    public String getDateCreated() {
        return dateCreated;
    }

    @JsonProperty("date_created")
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @JsonProperty("date_updated")
    public String getDateUpdated() {
        return dateUpdated;
    }

    @JsonProperty("date_updated")
    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("external_provider")
    public String getExternalProvider() {
        return externalProvider;
    }

    @JsonProperty("external_provider")
    public void setExternalProvider(String externalProvider) {
        this.externalProvider = externalProvider;
    }

    @JsonProperty("external_reference")
    public String getExternalReference() {
        return externalReference;
    }

    @JsonProperty("external_reference")
    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    @JsonProperty("fees")
    public List<Fee> getFees() {
        return fees;
    }

    @JsonProperty("fees")
    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    @JsonProperty("giro_slip_no")
    public String getGiroSlipNo() {
        return giroSlipNo;
    }

    @JsonProperty("giro_slip_no")
    public void setGiroSlipNo(String giroSlipNo) {
        this.giroSlipNo = giroSlipNo;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("method")
    public String getMethod() {
        return method;
    }

    @JsonProperty("method")
    public void setMethod(String method) {
        this.method = method;
    }

    @JsonProperty("organisation_name")
    public String getOrganisationName() {
        return organisationName;
    }

    @JsonProperty("organisation_name")
    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    @JsonProperty("payment_group_reference")
    public String getPaymentGroupReference() {
        return paymentGroupReference;
    }

    @JsonProperty("payment_group_reference")
    public void setPaymentGroupReference(String paymentGroupReference) {
        this.paymentGroupReference = paymentGroupReference;
    }

    @JsonProperty("payment_reference")
    public String getPaymentReference() {
        return paymentReference;
    }

    @JsonProperty("payment_reference")
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    @JsonProperty("reference")
    public String getReference() {
        return reference;
    }

    @JsonProperty("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonProperty("reported_date_offline")
    public String getReportedDateOffline() {
        return reportedDateOffline;
    }

    @JsonProperty("reported_date_offline")
    public void setReportedDateOffline(String reportedDateOffline) {
        this.reportedDateOffline = reportedDateOffline;
    }

    @JsonProperty("service_name")
    public String getServiceName() {
        return serviceName;
    }

    @JsonProperty("service_name")
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @JsonProperty("site_id")
    public String getSiteId() {
        return siteId;
    }

    @JsonProperty("site_id")
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("status_histories")
    public List<StatusHistory> getStatusHistories() {
        return statusHistories;
    }

    @JsonProperty("status_histories")
    public void setStatusHistories(List<StatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
