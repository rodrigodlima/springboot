package br.com.getnet.plataformadigital.becharge.adapter;

import static br.com.getnet.plataformadigital.becharge.adapter.outbound.AdapterChargePortOutbound.CHARGE_OPERATION_ERROR_EVENT_NAME;
import static br.com.getnet.plataformadigital.becharge.adapter.outbound.AdapterChargePortOutbound.CHARGE_PROCESSING_REQUESTED_EVENT_NAME;
import static br.com.getnet.plataformadigital.becharge.adapter.outbound.AdapterChargePortOutbound.PAYMENT_DATA_ENRICHMENT_REQUESTED_EVENT_NAME;
import static br.com.getnet.plataformadigital.becharge.domain.Status.CANCELED;
import static br.com.getnet.plataformadigital.becharge.domain.Status.PROCESSING;
import static br.com.getnet.plataformadigital.becharge.domain.Status.SCHEDULED;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_APP_ID;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_DELAY;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_EVENT_NAME;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_SELLER_ID;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_TTL;
import static br.com.getnet.plataformadigital.commons.headers.DefaultHeader.HEADER_TYPE;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.getnet.plataformadigital.becharge.adapter.inbound.AdapterChargeProcessingRequestedBroker;
import br.com.getnet.plataformadigital.becharge.config.broker.BrokerInput;
import br.com.getnet.plataformadigital.becharge.config.broker.BrokerOutput;
import br.com.getnet.plataformadigital.becharge.core.ChargeCore;
import br.com.getnet.plataformadigital.becharge.domain.ChargeData;
import br.com.getnet.plataformadigital.becharge.domain.ChargeProcessingRequested;
import br.com.getnet.plataformadigital.becharge.domain.PaymentDataEnrichmentRequested;
import br.com.getnet.plataformadigital.becharge.port.outbound.PortOutbound;
import br.com.getnet.plataformadigital.becharge.repository.ChargeDataRepository;
import br.com.getnet.plataformadigital.commons.exceptions.DefaultError;
import br.com.getnet.plataformadigital.commons.headers.DefaultHeader;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AdapterChargeProcessingRequestedBrokerFunctionalTest {
	
	@Value("${app.rabbitmq.ttlInMilliseconds}")
	private int ttlInMilliseconds;
	
	@Value("${app.rabbitmq.delayInMilliseconds}")
	private int delayInMilliseconds;
	
	@Value("${app.rabbitmq.maximumNumberOfRetries}")
	private int maximumNumberOfRetries;
	
	@Autowired
	private ChargeDataRepository chargeDataRepository;
	
	@Autowired
	private PortOutbound outBound;
	
	@Autowired
	BrokerInput input;
	
	@Autowired
	BrokerOutput output;
	
	@Autowired
	ObjectMapper mapper;
	
	@Autowired
	private MessageCollector collector;
	
	private ChargeData chargeData;

	private ChargeProcessingRequested chargeProcessingRequested;
	
	@Before
	public void setup() {
		chargeData = new ChargeData();
		chargeData.setChargeId("any_charge_id");
		chargeData.setSellerId("any_seller_id");
		chargeData.setSubscriptionId("any_subscription_id");
		chargeData.setStatus(SCHEDULED);
		chargeData.setCreateDate(Instant.now());
		
		chargeProcessingRequested = new ChargeProcessingRequested();
		chargeProcessingRequested.setChargeId("any_charge_id");
	}
	
	@Test
	public void receiveAChargeRecordForPayment() throws IOException {
		chargeDataRepository.save(chargeData);
		
		// message from charge-batch
		input.subscribeChargeProcessingRequested().send(MessageBuilder
				.withPayload(chargeProcessingRequested)
				.setHeader(HEADER_TYPE, DefaultHeader.HeaderType.REC_PROCESSING.getValue())
				.setHeader(HEADER_SELLER_ID, "any_seller_id")
				.setHeader(HEADER_APP_ID, "charge-batch")
				.build());
		
		Message<?> poll = collector.forChannel(output.publishPaymentDataEnrichmentRequested()).poll();
		Object payload = poll.getPayload();
		MessageHeaders headers = poll.getHeaders();
		
		PaymentDataEnrichmentRequested publishDataEnrichmentRequested = mapper.readValue(payload.toString(), PaymentDataEnrichmentRequested.class);
		
		//Verificar se os dados foram enviados para fila de enriquecimento para pagamento;
		assertEquals("any_charge_id", publishDataEnrichmentRequested.getChargeId());
		assertEquals("any_seller_id", publishDataEnrichmentRequested.getSellerId());
		assertEquals("any_subscription_id", publishDataEnrichmentRequested.getSubscriptionId());
		
		// Verificar se o status foi alterado para 'PROCESSING' no banco
		assertEquals(PROCESSING, chargeDataRepository.findOne("any_charge_id").getStatus());
		
		//Verificar se as definições do Header foram enviados
		assertEquals("be-charge", headers.get(HEADER_APP_ID));
		assertEquals("rec-processing", headers.get(HEADER_TYPE));
		assertEquals(PAYMENT_DATA_ENRICHMENT_REQUESTED_EVENT_NAME, headers.get(HEADER_EVENT_NAME));
		assertEquals("any_seller_id", headers.get(HEADER_SELLER_ID));
		assertEquals(ttlInMilliseconds, headers.get(HEADER_TTL));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void receiveAInvalidChargeRecordForPayment() throws IOException {
		chargeData.setStatus(CANCELED);
		
		chargeDataRepository.save(chargeData);
		
		// message from charge-batch
		input.subscribeChargeProcessingRequested().send(MessageBuilder
				.withPayload(chargeProcessingRequested)
				.setHeader(HEADER_TYPE, DefaultHeader.HeaderType.REC_PROCESSING.getValue())
				.setHeader(HEADER_SELLER_ID, "any_seller_id")
				.setHeader(HEADER_APP_ID, "charge-batch")
				.build());
		
		Message<?> poll = collector.forChannel(output.publishChargeOperationError()).poll();
		Object payload = poll.getPayload();
		MessageHeaders headers = poll.getHeaders();
		
		DefaultError error = mapper.readValue(payload.toString(), DefaultError.class);
		
		//Verificar se os dados foram enviados para fila de ERROR;
		Map<String, Object> originalMessage = (Map<String, Object>) error.getOriginalMessage();
		assertEquals(chargeProcessingRequested.getChargeId(), originalMessage.get("charge_id"));
		assertEquals(SC_CONFLICT, error.getErrors().get(0).getCode());
		
		// Verificar se o status NÃO foi alterado para 'PROCESSING' no banco
		assertEquals(CANCELED, chargeDataRepository.findOne("any_charge_id").getStatus());
		
		// Verificar se as definições do Header foram enviados
		assertEquals("be-charge", headers.get(HEADER_APP_ID));
		assertEquals("rec-processing", headers.get(HEADER_TYPE));
		assertEquals(CHARGE_OPERATION_ERROR_EVENT_NAME, headers.get(HEADER_EVENT_NAME));
		assertEquals("any_seller_id", headers.get(HEADER_SELLER_ID));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testChargeProcessingRequestedWithWait() throws Exception {
		ChargeDataRepository chargeDataRepository = Mockito.mock(ChargeDataRepository.class);
		when(chargeDataRepository.findOne(Mockito.anyString())).thenThrow(Exception.class);
		
		ChargeCore chargeCore = new ChargeCore(chargeDataRepository, outBound);
		FieldUtils.writeDeclaredField(chargeCore, "maximumNumberOfRetries", maximumNumberOfRetries, true);
		
		AdapterChargeProcessingRequestedBroker adapter = new AdapterChargeProcessingRequestedBroker(mapper, chargeCore);
		
		// message from charge-batch
		DefaultHeader defaultHeaders = new DefaultHeader()
			.type(DefaultHeader.HeaderType.REC_PROCESSING)
			.sellerId("any_seller_id")
			.appId("charge-batch");

		// 1º Tentativa
		adapter.subscribeExchangeChargeProcessingRequested(chargeProcessingRequested, defaultHeaders.toMap());
		
		Message<?> poll = collector.forChannel(output.publishChargeProcessingRequestedWithWait()).poll();
		Object payload = poll.getPayload();
		MessageHeaders messageHeaders = poll.getHeaders();
		
		ChargeProcessingRequested chargeProcessingRequested = mapper.readValue(payload.toString(), ChargeProcessingRequested.class);
		
		assertEquals("any_charge_id", chargeProcessingRequested.getChargeId());
		
		assertEquals("be-charge", messageHeaders.get(HEADER_APP_ID));
		assertEquals("rec-processing", messageHeaders.get(HEADER_TYPE));
		assertEquals(CHARGE_PROCESSING_REQUESTED_EVENT_NAME, messageHeaders.get(HEADER_EVENT_NAME));
		assertEquals("any_seller_id", messageHeaders.get(HEADER_SELLER_ID));
		assertEquals(delayInMilliseconds, messageHeaders.get(HEADER_DELAY));
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testMaxRetriesLimit() throws Exception {
		ChargeDataRepository chargeDataRepository = Mockito.mock(ChargeDataRepository.class);
		when(chargeDataRepository.findOne(Mockito.anyString())).thenThrow(Exception.class);
		
		ChargeCore chargeCore = new ChargeCore(chargeDataRepository, outBound);
		FieldUtils.writeDeclaredField(chargeCore, "maximumNumberOfRetries", maximumNumberOfRetries, true);
		
		AdapterChargeProcessingRequestedBroker adapter = new AdapterChargeProcessingRequestedBroker(mapper, chargeCore);
		
		// message from charge-batch
		DefaultHeader defaultHeaders = new DefaultHeader()
			.type(DefaultHeader.HeaderType.REC_PROCESSING)
			.sellerId("any_seller_id")
			.retryCount(3) //Force limit
			.appId("charge-batch");

		adapter.subscribeExchangeChargeProcessingRequested(chargeProcessingRequested, defaultHeaders.toMap());
		
		Message<?> poll = collector.forChannel(output.publishChargeOperationError()).poll();
		Object payload = poll.getPayload();
		MessageHeaders messageHeaders = poll.getHeaders();
		
		DefaultError error = mapper.readValue(payload.toString(), DefaultError.class);
		
		Map<String, Object> originalMessage = (Map<String, Object>) error.getOriginalMessage();
		assertEquals(chargeProcessingRequested.getChargeId(), originalMessage.get("charge_id"));
		assertEquals(SC_INTERNAL_SERVER_ERROR, error.getErrors().get(0).getCode());
		
		assertEquals("be-charge", messageHeaders.get(HEADER_APP_ID));
		assertEquals("rec-processing", messageHeaders.get(HEADER_TYPE));
		assertEquals(CHARGE_OPERATION_ERROR_EVENT_NAME, messageHeaders.get(HEADER_EVENT_NAME));
		assertEquals("any_seller_id", messageHeaders.get(HEADER_SELLER_ID));
	}

}
