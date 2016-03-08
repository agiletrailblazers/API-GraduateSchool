package com.gs.api.service.payment;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.PaymentAcceptedException;
import com.gs.api.exception.PaymentDeclinedException;
import com.gs.api.exception.PaymentException;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Payment service implementation for the CyberSource payment system.
 */
@Service
public class CyberSourcePaymentServiceImpl implements PaymentService {

    static final String FAILED_PAYMENT_DECLINED_MSG = "Payment declined by CyberSource";
    static final String FAILED_TO_COMPLETE_SALE_MSG = "Failed to complete sale with CyberSource";
    static final String FAILED_TO_REVERSE_AUTHORIZATION_MSG = "Failed to reverse payment authorization with CyberSource";
    static final String REQUEST_ID = "requestID";
    static final String REQUEST_CC_CAPTURE_SERVICE_RUN = "ccCaptureService_run";
    static final String REQUEST_CC_AUTH_REVERSAL_SERVICE_RUN = "ccAuthReversalService_run";
    static final String REQUEST_MERCHANT_REFERENCE_CODE = "merchantReferenceCode";
    static final String REQUEST_CC_CAPTURE_SERVICE_AUTH_REQUEST_ID = "ccCaptureService_authRequestID";
    static final String REQUEST_PURCHASE_TOTALS_CURRENCY = "purchaseTotals_currency";
    static final String REQUEST_PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT = "purchaseTotals_grandTotalAmount";
    static final String USD = "USD";
    static final String MERCHANT_ID_PROP = "merchantID";
    static final String KEYS_DIRECTORY_PROP = "keysDirectory";
    static final String KEY_FILENAME_PROP = "keyFilename";
    static final String KEY_ALIAS_PROP = "keyAlias";
    static final String KEY_PASSWORD_PROP = "keyPassword";
    static final String TARGET_API_VERSION_PROP = "targetAPIVersion";
    static final String SEND_TO_PRODUCTION_PROP = "sendToProduction";
    static final String SERVER_URL_PROP = "serverURL";
    static final String USE_HTTP_CLIENT_PROP = "useHttpClient";
    static final String TIMEOUT_PROP = "timeout";
    static final String RESPONSE_REASON_CODE = "reasonCode";
    static final String RESPONSE_DECISION = "decision";
    static final String ACCEPT = "ACCEPT";
    static final String ENABLE_LOG_PROP = "enableLogProp";
    static final String LOG_MAXIMUM_SIZE_PROP = "logMaximumSizeProp";
    static final String LOG_DIRECTORY_PROP = "logDirectoryProp";

    final Logger logger = LoggerFactory.getLogger(CyberSourcePaymentServiceImpl.class);

    @Value("${cybersource.merchantID}")
    private String merchantId;

    @Value("${cybersource.keyFilename}")
    private String keyFilename;

    @Value("${cybersource.targetAPIVersion}")
    private String targetAPIVersion;

    @Value("${cybersource.sendToProduction}")
    private boolean sendToProduction;

    @Value("${cybersource.serverURL}")
    private String serverURL;

    @Value("${cybersource.useHttpClient}")
    private boolean useHttpClient;

    @Value("${cybersource.timeout}")
    private int timeout;

    @Value("${cybersource.enableLog}")
    private boolean enableLog;

    @Value("${cybersource.logMaximumSize}")
    private int logMaximumSize;

    @Value("#{systemProperties['cybersourceDir'] ?: ''}")
    private String cybersourceDir;

    @Value("${cybersource.declinedReasonCodes}")
    private String[] declinedReasonCodes;

    @Override
    public PaymentConfirmation processPayment(final Payment payment) throws PaymentException {

        logger.info("Processing payment, authorization id {}, amount {}", payment.getAuthorizationId(), payment.getAmount());

        // create the request parameters for the CyberSource request
        Map<String, String> cyberSourceRequest = createRequestParameters(REQUEST_CC_CAPTURE_SERVICE_RUN, payment);

        // create the configuration properties for the CyberSource client
        Properties clientProperties = createClientProperties();

        Map cyberSourceResponse = null;
        try {
            // use the CyberSource client to execute the capture (sale) transaction
            cyberSourceResponse = Client.runTransaction(cyberSourceRequest, clientProperties);
        }
        catch (ClientException e) {

            if (e.isCritical()) {
                // do not send auth reversal in this case because the transaction may have actually been processed
                handleNotifications(e, cyberSourceRequest, cyberSourceResponse);
                throw new PaymentAcceptedException(FAILED_TO_COMPLETE_SALE_MSG, e);
            }
            logger.error("Payment failed, reversing authorization", e);
            reversePaymentSilently(payment);
            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }
        catch (FaultException e) {

            if (e.isCritical()) {
                // do not send auth reversal in this case because the transaction may have actually been processed
                handleNotifications(e, cyberSourceRequest, cyberSourceResponse);
                throw new PaymentAcceptedException(FAILED_TO_COMPLETE_SALE_MSG, e);
            }
            logger.error("Payment failed, reversing authorization", e);
            reversePaymentSilently(payment);
            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }
        catch (Exception e) {
            logger.error("Payment failed, reversing authorization", e);
            reversePaymentSilently(payment);
            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }

        String decision = (String) cyberSourceResponse.get(RESPONSE_DECISION);
        if (!ACCEPT.equalsIgnoreCase(decision)) {
            reversePaymentSilently(payment);

            String errorMsg;
            // non-accept response, log and throw payment failure
            String reasonCode = (String) cyberSourceResponse.get(RESPONSE_REASON_CODE);
            if(ArrayUtils.contains(declinedReasonCodes, reasonCode)) {
                errorMsg = String.format(FAILED_PAYMENT_DECLINED_MSG + ", decision %s, reason code %s", decision, reasonCode);
                logger.debug(errorMsg);
                throw new PaymentDeclinedException(errorMsg);
            }
            else {
                errorMsg = String.format(FAILED_TO_COMPLETE_SALE_MSG + ", decision %s, reason code %s", decision, reasonCode);
                logger.debug(errorMsg);
                throw new PaymentException(errorMsg);
            }
        }

        logger.info("Successful payment, reference number {}, amount {}", payment.getMerchantReferenceId(), payment.getAmount());
        return new PaymentConfirmation(payment, (String) cyberSourceResponse.get(REQUEST_ID));
    }

    @Override
    public void reversePayment(final Payment payment) throws PaymentException {

        logger.info("Processing payment authorization reversal, authorization id {}, amount {}", payment.getAuthorizationId(), payment.getAmount());

        // create the request parameters for the CyberSource request
        Map<String, String> cyberSourceRequest = createRequestParameters(REQUEST_CC_AUTH_REVERSAL_SERVICE_RUN, payment);

        // create the configuration properties for the CyberSource client
        Properties clientProperties = createClientProperties();

        Map cyberSourceResponse = null;
        try {
            // use the CyberSource client to execute the reversal transaction
            cyberSourceResponse = Client.runTransaction(cyberSourceRequest, clientProperties);

            String decision = (String) cyberSourceResponse.get(RESPONSE_DECISION);
            if (!ACCEPT.equalsIgnoreCase(decision)) {
                // non-accept response, log and throw payment failure
                String reasonCode = (String) cyberSourceResponse.get(RESPONSE_REASON_CODE);
                String errorMsg = String.format(FAILED_TO_REVERSE_AUTHORIZATION_MSG + ", decision %s, reason code %s", decision, reasonCode);
                logger.error(errorMsg);
                throw new PaymentException(errorMsg);
            }

            logger.info("Successful payment authorization reversal, reference number {}, amount {}", payment.getMerchantReferenceId(), payment.getAmount());
        }
        catch (ClientException e) {
            throw new PaymentException(FAILED_TO_REVERSE_AUTHORIZATION_MSG, e);
        }
        catch (FaultException e) {
            throw new PaymentException(FAILED_TO_REVERSE_AUTHORIZATION_MSG, e);
        }
    }

    private Properties createClientProperties() {

        Properties clientProperties = new Properties();

        // GS CyberSource merchant ID
        clientProperties.setProperty(MERCHANT_ID_PROP, merchantId);

        // Directory containing the CyberSource security certificate
        clientProperties.setProperty(KEYS_DIRECTORY_PROP, cybersourceDir);

        // File name of the CyberSource security certificate
        clientProperties.setProperty(KEY_FILENAME_PROP, keyFilename);

        // Per CyberSource, use merchant ID for key alias
        clientProperties.setProperty(KEY_ALIAS_PROP, merchantId);

        // Per CyberSource, use merchant ID for key password
        clientProperties.setProperty(KEY_PASSWORD_PROP, merchantId);

        // Version of the CyberSource Simple Order API
        clientProperties.setProperty(TARGET_API_VERSION_PROP, targetAPIVersion);

        // Flag indicating whether or not targeting production system, false uses test environment
        clientProperties.setProperty(SEND_TO_PRODUCTION_PROP, Boolean.toString(sendToProduction));

        // URL of the CyberSource API end point
        clientProperties.setProperty(SERVER_URL_PROP, serverURL);

        // Flag indicating whether or not to use Apache Commons HttpClient
        clientProperties.setProperty(USE_HTTP_CLIENT_PROP, Boolean.toString(useHttpClient));

        // Http request timeout, only used if using the Apache Commons HttpClient
        clientProperties.setProperty(TIMEOUT_PROP, Integer.toString(timeout));

        // Flag indicating whether or not CyberSource logging is turned on, they recommend keeping it off except for debugging
        clientProperties.setProperty(ENABLE_LOG_PROP, Boolean.toString(enableLog));

        // Directory into which the CyberSource log file will be created
        clientProperties.setProperty(LOG_DIRECTORY_PROP, cybersourceDir);

        // Maximum file size of the CyberSource log file
        clientProperties.setProperty(LOG_MAXIMUM_SIZE_PROP, Integer.toString(logMaximumSize));

        return clientProperties;
    }

    private Map<String, String> createRequestParameters(String serviceName, Payment payment) {

        Map<String, String> requestParameters = new HashMap<>();

        // execute the capture (sale)
        requestParameters.put(serviceName, Boolean.TRUE.toString());

        // so that you can efficiently track the order in the CyberSource
        // reports and transaction search screens, you should use the same
        // merchantReferenceCode for the auth and subsequent captures and
        // credits.
        requestParameters.put(REQUEST_MERCHANT_REFERENCE_CODE, payment.getMerchantReferenceId());

        // reference the authorization ID returned by the hosted CyberSource payment page
        requestParameters.put(REQUEST_CC_CAPTURE_SERVICE_AUTH_REQUEST_ID, payment.getAuthorizationId());

        // currency is US dollar
        requestParameters.put(REQUEST_PURCHASE_TOTALS_CURRENCY, USD);

        // set the sale amount
        requestParameters.put(REQUEST_PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, new DecimalFormat("#.00").format(payment.getAmount()));

        return requestParameters;
    }

    private void reversePaymentSilently(final Payment payment) {
        try {
            reversePayment(payment);
        }
        catch (PaymentException e) {
            logger.debug("Logging authorization reversal failure but continuing to process");
        }
    }

    private void handleNotifications(Exception e, Map<String, String> cyberSourceRequest, Map<String, String> cyberSourceResponse) {

        logger.error("Sending notifications of CyberSource transaction failure", e);

        // TODO future story - send an email to Graduate School operations including the error, request and response details?
    }

}
