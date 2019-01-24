package br.com.getnet.plataformadigital.becharge.domain;

import java.io.Serializable;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Period implements Serializable {

	private static final long serialVersionUID = -9154595289254251985L;

	private String type;
	private String billingCycle;
	private int specificCycleInDays;
	private int chargeBillingNumber;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBillingCycle() {
		return billingCycle;
	}

	public void setBillingCycle(String billingCycle) {
		this.billingCycle = billingCycle;
	}

	public int getSpecificCycleInDays() {
		return specificCycleInDays;
	}

	public void setSpecificCycleInDays(int specificCycleInDays) {
		this.specificCycleInDays = specificCycleInDays;
	}

	public int getChargeBillingNumber() {
		return chargeBillingNumber;
	}

	public void setChargeBillingNumber(int chargeBillingNumber) {
		this.chargeBillingNumber = chargeBillingNumber;
	}
}