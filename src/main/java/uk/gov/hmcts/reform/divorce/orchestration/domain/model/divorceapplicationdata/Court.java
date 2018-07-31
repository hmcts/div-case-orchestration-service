package uk.gov.hmcts.reform.divorce.orchestration.domain.model.divorceapplicationdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
public @Data class Court {
	private String region;
	private String phone;
	private String divorceCentre;
	private String courtCity;
	private String poBox;
	private String postCode;
	private String openingHours;
	private String email;
	private String phoneNumber;
}