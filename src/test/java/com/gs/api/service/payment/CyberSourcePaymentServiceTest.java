package com.gs.api.service.payment;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.FatalPaymentException;
import com.gs.api.exception.PaymentException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Properties;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Client.class)
public class CyberSourcePaymentServiceTest {

    private static final double AMOUNT = 149.50;
    private static final String AUTHORIZATION_ID = "authId12345";
    private static final String MERCHANT_REFERENCE_ID = "merch12345";
    static final String SALE_ID = "sale12345";
    private static final String MERCHANT_ID = "merch12345";
    private static final String KEY_DIR = "/some/test/dir";
    private static final String TESTCERT = "testcert.p12";
    private static final String API_VERSION = "1.1.1";
    private static final boolean SEND_TO_PROD = false;
    private static final boolean FALSE = SEND_TO_PROD;
    private static final String SERVER_URL = "my.server.url";
    private static final boolean TRUE = true;
    private static final int TIMEOUT = 5000;
    private static final String REJECT = "Reject";
    private static final String TEST_REJECT_REASON_CODE = "testRejectReasonCode";
    private static final String LOG_DIR = "logDir";
    private static final int MAX_LOG_SIZE = 10;

    private CyberSourcePaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<HashMap<String, String>> requestCaptor;

    @Captor
    private ArgumentCaptor<Properties> propertiesCaptor;

    @Mock
    private ClientException clientException;

    @Mock
    private FaultException faultException;

    /*
        By default, no exceptions are expected to be thrown (i.e. tests will fail if an exception is thrown),
        but using this rule allows for verification of operations that are expected to throw specific exceptions
    */
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        paymentService = new CyberSourcePaymentServiceImpl();
        mockStatic(Client.class);

        ReflectionTestUtils.setField(paymentService, "merchantId", MERCHANT_ID);
        ReflectionTestUtils.setField(paymentService, "keysDirectory", KEY_DIR);
        ReflectionTestUtils.setField(paymentService, "keyFilename", TESTCERT);
        ReflectionTestUtils.setField(paymentService, "targetAPIVersion", API_VERSION);
        ReflectionTestUtils.setField(paymentService, "sendToProduction", FALSE);
        ReflectionTestUtils.setField(paymentService, "serverURL", SERVER_URL);
        ReflectionTestUtils.setField(paymentService, "useHttpClient", TRUE);
        ReflectionTestUtils.setField(paymentService, "timeout", TIMEOUT);
        ReflectionTestUtils.setField(paymentService, "enableLog", FALSE);
        ReflectionTestUtils.setField(paymentService, "logDirectory", LOG_DIR);
        ReflectionTestUtils.setField(paymentService, "logMaximumSize", MAX_LOG_SIZE);
    }

    @Test
    public void testProcessPayment_Success() throws Exception {

        final HashMap<String, String> response = new HashMap<>();
        response.put(CyberSourcePaymentServiceImpl.REQUEST_ID, SALE_ID);
        response.put(CyberSourcePaymentServiceImpl.RESPONSE_DECISION, CyberSourcePaymentServiceImpl.ACCEPT);

        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenReturn(response);

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        PaymentConfirmation confirmation = paymentService.processPayment(payment);

        assertSame("wrong payment", payment, confirmation.getPayment());
        assertEquals("wrong sale transaction id", SALE_ID, confirmation.getSaleId());

        PowerMockito.verifyStatic();
        Client.runTransaction(requestCaptor.capture(), propertiesCaptor.capture());

        HashMap<String, String> capturedRequest = requestCaptor.getValue();
        assertEquals(5, capturedRequest.size());
        assertTrue(Boolean.parseBoolean(capturedRequest.get(CyberSourcePaymentServiceImpl.REQUEST_CC_CAPTURE_SERVICE_RUN)));
        assertEquals(MERCHANT_REFERENCE_ID, capturedRequest.get(CyberSourcePaymentServiceImpl.REQUEST_MERCHANT_REFERENCE_CODE));
        assertEquals(AUTHORIZATION_ID, capturedRequest.get(CyberSourcePaymentServiceImpl.REQUEST_CC_CAPTURE_SERVICE_AUTH_REQUEST_ID));
        assertEquals(CyberSourcePaymentServiceImpl.USD, capturedRequest.get(CyberSourcePaymentServiceImpl.REQUEST_PURCHASE_TOTALS_CURRENCY));
        assertEquals(new DecimalFormat("#.00").format(AMOUNT), capturedRequest.get(CyberSourcePaymentServiceImpl.REQUEST_PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT));

        Properties capturedProps = propertiesCaptor.getValue();
        assertEquals(13, capturedProps.size());
        assertEquals(MERCHANT_ID, capturedProps.get(CyberSourcePaymentServiceImpl.MERCHANT_ID_PROP));
        assertEquals(KEY_DIR, capturedProps.get(CyberSourcePaymentServiceImpl.KEYS_DIRECTORY_PROP));
        assertEquals(TESTCERT, capturedProps.get(CyberSourcePaymentServiceImpl.KEY_FILENAME_PROP));
        assertEquals(MERCHANT_ID, capturedProps.get(CyberSourcePaymentServiceImpl.KEY_ALIAS_PROP));
        assertEquals(MERCHANT_ID, capturedProps.get(CyberSourcePaymentServiceImpl.KEY_PASSWORD_PROP));
        assertEquals(API_VERSION, capturedProps.get(CyberSourcePaymentServiceImpl.TARGET_API_VERSION_PROP));
        assertFalse(Boolean.parseBoolean((String) capturedProps.get(CyberSourcePaymentServiceImpl.SEND_TO_PRODUCTION_PROP)));
        assertEquals(SERVER_URL, capturedProps.get(CyberSourcePaymentServiceImpl.SERVER_URL_PROP));
        assertTrue(Boolean.parseBoolean((String) capturedProps.get(CyberSourcePaymentServiceImpl.USE_HTTP_CLIENT_PROP)));
        assertEquals(Integer.toString(TIMEOUT), capturedProps.get(CyberSourcePaymentServiceImpl.TIMEOUT_PROP));
        assertFalse(Boolean.parseBoolean((String) capturedProps.get(CyberSourcePaymentServiceImpl.ENABLE_LOG_PROP)));
        assertEquals(LOG_DIR, capturedProps.get(CyberSourcePaymentServiceImpl.LOG_DIRECTORY_PROP));
        assertEquals(Integer.toString(MAX_LOG_SIZE), capturedProps.get(CyberSourcePaymentServiceImpl.LOG_MAXIMUM_SIZE_PROP));
    }

    @Test
    public void testProcessPayment_Decline() throws Exception {

        thrown.expect(PaymentException.class);
        thrown.expectMessage(Matchers.containsString(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG));
        thrown.expectMessage(Matchers.containsString(REJECT));
        thrown.expectMessage(Matchers.containsString(TEST_REJECT_REASON_CODE));

        final HashMap<String, String> response = new HashMap<>();
        response.put(CyberSourcePaymentServiceImpl.REQUEST_ID, SALE_ID);
        response.put(CyberSourcePaymentServiceImpl.RESPONSE_DECISION, REJECT);
        response.put(CyberSourcePaymentServiceImpl.RESPONSE_REASON_CODE, TEST_REJECT_REASON_CODE);

        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenReturn(response);

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        paymentService.processPayment(payment);
     }

    @Test
    public void testProcessPayment_Failure() throws Exception {

        // setup expected exception
        IllegalArgumentException cause = new IllegalArgumentException("I caused test to fail");

        thrown.expect(IllegalArgumentException.class);

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(cause);

        paymentService.processPayment(payment);
    }

    @Test
    public void testProcessPayment_CyberSourceClientException() throws Exception {

        thrown.expect(PaymentException.class);
        thrown.expectMessage(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG);
        thrown.expectCause(Matchers.is(clientException));

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(clientException.isCritical()).thenReturn(false);
        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(clientException);

        paymentService.processPayment(payment);
    }

    @Test
    public void testProcessPayment_CyberSourceCriticalClientException() throws Exception {

        thrown.expect(FatalPaymentException.class);
        thrown.expectMessage(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG);
        thrown.expectCause(Matchers.is(clientException));

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(clientException.isCritical()).thenReturn(true);
        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(clientException);

        paymentService.processPayment(payment);
    }

    @Test
    public void testProcessPayment_CyberSourceFaultException() throws Exception {

        thrown.expect(PaymentException.class);
        thrown.expectMessage(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG);
        thrown.expectCause(Matchers.is(faultException));

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(faultException.isCritical()).thenReturn(false);
        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(faultException);

        paymentService.processPayment(payment);
    }

    @Test
    public void testProcessPayment_CyberSourceCriticalFaultException() throws Exception {

        thrown.expect(FatalPaymentException.class);
        thrown.expectMessage(CyberSourcePaymentServiceImpl.FAILED_TO_COMPLETE_SALE_MSG);
        thrown.expectCause(Matchers.is(faultException));

        Payment payment = new Payment(AMOUNT, AUTHORIZATION_ID, MERCHANT_REFERENCE_ID);

        when(faultException.isCritical()).thenReturn(true);
        when(Client.runTransaction(isA(HashMap.class), isA(Properties.class))).thenThrow(faultException);

        paymentService.processPayment(payment);
    }

}
