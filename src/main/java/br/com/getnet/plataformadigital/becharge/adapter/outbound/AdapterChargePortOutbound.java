package br.com.getnet.plataformadigital.becharge.adapter.outbound;

import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_APP_ID;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_DELAY;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_EVENT_NAME;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_RETRY_COUNT;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_TTL;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.getnet.plataformadigital.becharge.config.broker.BrokerOutput;
import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.becharge.domain.PaymentDataEnrichmentRequested;
import br.com.getnet.plataformadigital.becharge.port.outbound.PortOutbound;
import br.com.getnet.plataformadigital.commons.exceptions.DefaultError;
import br.com.getnet.plataformadigital.commons.headers.HeaderHelper;

@Service
@Transactional
@EnableBinding({BrokerOutput.class})
public class AdapterChargePortOutbound implements PortOutbound {
	
	public static final String CHARGE_OPERATION_ERROR_EVENT_NAME = "ChargeOperationError";
	public static final String PAYMENT_DATA_ENRICHMENT_REQUESTED_EVENT_NAME = "PaymentDataEnrichmentRequested";
	public static final String CHARGE_PROCESSING_REQUESTED_EVENT_NAME = "ChargeProcessingRequested";
	
	private Logger log = LoggerFactory.getLogger(AdapterChargePortOutbound.class);

	@Value("${spring.application.name}")
	private String appId;

	@Value("${app.rabbitmq.ttlInMilliseconds}")
	private int ttlInMilliseconds;
	
	@Value("${app.rabbitmq.delayInMilliseconds}")
	private int delayInMilliseconds;
	
	private final ObjectMapper mapper;
	private final BrokerOutput output;
	
	public AdapterChargePortOutbound(BrokerOutput output, ObjectMapper mapper) {
		this.output = output;
		this.mapper = mapper;
	}

	@Override
	public void sendMessageForPaymentDataEnrichment(PaymentDataEnrichmentRequested paymentDataEnrichmentRequested, Map<String, Object> headers) {
		try {
			String json = this.mapper.writeValueAsString(paymentDataEnrichmentRequested);
			this.output.publishPaymentDataEnrichmentRequested().send(MessageBuilder
					.withPayload(json)
					.copyHeaders(headers)
					.setHeader(HEADER_APP_ID, appId)
					.setHeader(HEADER_EVENT_NAME, PAYMENT_DATA_ENRICHMENT_REQUESTED_EVENT_NAME)
					.setHeader(HEADER_TTL, ttlInMilliseconds)
					.build());
		} catch (JsonProcessingException e) {
			log.error("Erro ao enviar PaymentDataEnrichmentRequested[{}] para fila sendMessageForPaymentDataEnrichment", paymentDataEnrichmentRequested.getChargeId());
		}
	}

	@Override
	public void sendMessageError(DefaultError error, Map<String, Object> headers) {
		try {
			String json = this.mapper.writeValueAsString(error);
			this.output.publishChargeOperationError().send(MessageBuilder
					.withPayload(json)
					.copyHeaders(headers)
					.setHeader(HEADER_APP_ID, appId)
					.setHeader(HEADER_EVENT_NAME, CHARGE_OPERATION_ERROR_EVENT_NAME)
					.build());
		} catch (JsonProcessingException e) {
			log.error("Erro ao enviar ChargeOperationError[{}] para fila sendMessageError", error.getOriginalMessage());
		}
	}

	@Override
	public void sendMessageForChargeProcessingRequestedWithWait(ChargeProcessingRequested chargeProcessingRequested,
			Map<String, Object> headers) {
		try {
			String json = this.mapper.writeValueAsString(chargeProcessingRequested);
			this.output.publishChargeProcessingRequestedWithWait().send(MessageBuilder
					.withPayload(json)
					.copyHeaders(headers)
					.setHeader(HEADER_APP_ID, appId)
					.setHeader(HEADER_EVENT_NAME, CHARGE_PROCESSING_REQUESTED_EVENT_NAME)
					.setHeader(HEADER_DELAY, delayInMilliseconds)
					.setHeader(HEADER_RETRY_COUNT, HeaderHelper.increaseRetryCount(headers))
					.build());
		} catch (JsonProcessingException e) {
			log.error("Erro ao enviar ChargeProcessingRequested[{}] para fila de espera", chargeProcessingRequested.getChargeId());
		}
	}
	
}