package br.com.getnet.plataformadigital.becharge.domain;

import static br.com.getnet.plataformadigital.becharge.domain.Status.DENIED;
import static br.com.getnet.plataformadigital.becharge.domain.Status.PAYMENT_PENDING;
import static br.com.getnet.plataformadigital.becharge.domain.Status.SCHEDULED;

import java.io.Serializable;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import br.com.getnet.plataformadigital.commons.deserializer.DefaultInstantDeserializer;

@Validated
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Document(collection = "charge-data")
public class ChargeData implements Serializable {

	private static final long serialVersionUID = -5814155859734328495L;

	@Id
	private String chargeId;
	private String sellerId;
	private String subscriptionId;
	private String customerId;
	private String planId;
	private String paymentId;
	private String amount;
	private Status status;
	private String paymentType;
	private String terminalNsu;
	private String authorizationCode;
	private String acquirerTransactionId;

	private int retryNumber;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = DefaultInstantDeserializer.class)
	private Instant scheduledDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = DefaultInstantDeserializer.class)
	private Instant createDate;

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = DefaultInstantDeserializer.class)
	private Instant paymentDate;

	private Plan plan;

	@JsonIgnore
	public boolean sendToPaymentDataEnrichment() {
		return SCHEDULED.equals(status)
				|| PAYMENT_PENDING.equals(status)
				|| DENIED.equals(status);
	}

	public String getChargeId() {
		return chargeId;
	}

	public void setChargeId(String chargeId) {
		this.chargeId = chargeId;
	}

	public String getSellerId() {
		return sellerId;
	}

	public void setSellerId(String sellerId) {
		this.sellerId = sellerId;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getTerminalNsu() {
		return terminalNsu;
	}

	public void setTerminalNsu(String terminalNsu) {
		this.terminalNsu = terminalNsu;
	}

	public String getAuthorizationCode() {
		return authorizationCode;
	}

	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}

	public String getAcquirerTransactionId() {
		return acquirerTransactionId;
	}

	public void setAcquirerTransactionId(String acquirerTransactionId) {
		this.acquirerTransactionId = acquirerTransactionId;
	}

	public int getRetryNumber() {
		return retryNumber;
	}

	public void setRetryNumber(int retryNumber) {
		this.retryNumber = retryNumber;
	}

	public Instant getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(Instant scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Instant getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Instant createDate) {
		this.createDate = createDate;
	}

	public Instant getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Instant paymentDate) {
		this.paymentDate = paymentDate;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}
}