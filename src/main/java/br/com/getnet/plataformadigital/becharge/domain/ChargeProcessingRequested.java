package br.com.getnet.plataformadigital.becharge.domain;

import java.io.Serializable;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Validated
public class ChargeProcessingRequested implements Serializable {

	private static final long serialVersionUID = 4735509639958850475L;

	private String chargeId;

	public String getChargeId() {
		return chargeId;
	}

	public void setChargeId(String chargeId) {
		this.chargeId = chargeId;
	}

}
