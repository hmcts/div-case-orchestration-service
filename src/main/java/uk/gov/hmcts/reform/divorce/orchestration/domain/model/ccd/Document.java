package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

public @Data class Document {
	
	@JsonProperty("DocumentType")
	private DocumentType documentType;

	@JsonProperty("DocumentLink")
	private DocumentLink documentLink;

	@JsonProperty("DocumentDateAdded")
	private String documentDateAdded;

	@JsonProperty("DocumentComment")
	private String documentComment;

	@JsonProperty("DocumentFileName")
	private String documentFileName;

	@JsonProperty("DocumentEmailContent")
	private String documentEmailContent;
}
