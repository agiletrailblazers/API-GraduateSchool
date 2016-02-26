package com.gs.api.service.payment;

import com.cybersource.ws.client.Client;
import com.cybersource.ws.client.ClientException;
import com.cybersource.ws.client.FaultException;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.PaymentException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    static final String REQUEST_ID = "requestID";
    static final String FAILED_TO_COMPLETE_SALE_MSG = "Failed to complete sale with CyberSource";
    static final String MERCHANT_ID = "merchantID";
    static final String CC_CAPTURE_SERVICE_RUN = "ccCaptureService_run";
    static final String MERCHANT_REFERENCE_CODE = "merchantReferenceCode";
    static final String CC_CAPTURE_SERVICE_AUTH_REQUEST_ID = "ccCaptureService_authRequestID";
    static final String PURCHASE_TOTALS_CURRENCY = "purchaseTotals_currency";
    static final String PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT = "purchaseTotals_grandTotalAmount";
    static final String USD = "USD";

    final Logger logger = LoggerFactory.getLogger(CyberSourcePaymentServiceImpl.class);

    // TODO inject cybersource properties from config

    @Override
    public PaymentConfirmation processPayment(final Payment payment) throws PaymentException {

        // create the request parameters for the CyberSource request
        Map<String, String> request = createRequestParameters(payment);

        // create the configuration properties for the CyberSource client
        Properties props = createClientProperties();

        try {
            // use the CyberSource client to execute the capture (sale) transaction
            Map<String, String> response = Client.runTransaction(request, props);

            // TODO check the response for actual success vs failure, etc.

            return new PaymentConfirmation(payment, response.get(REQUEST_ID));
        }
        catch (ClientException e) {

            if (e.isCritical()) {

                handleCriticalException(e, request);

                // TODO throw a FatalPaymentException?
            }

            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }
        catch (FaultException e) {

            if (e.isCritical()) {

                handleCriticalException(e, request);

                // TODO throw a FatalPaymentException?
            }

            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }
    }

    private Properties createClientProperties() {

        Properties props = new Properties();

        props.getProperty(MERCHANT_ID, "evalgraduateschool");

        // TODO add the properties from the config

        return props;
    }

    private Map<String, String> createRequestParameters(Payment payment) {

        Map<String, String> requestParameters = new HashMap<>();

        // execute the capture (sale)
        requestParameters.put(CC_CAPTURE_SERVICE_RUN, Boolean.TRUE.toString());

        // so that you can efficiently track the order in the CyberSource
        // reports and transaction search screens, you should use the same
        // merchantReferenceCode for the auth and subsequent captures and
        // credits.
        requestParameters.put(MERCHANT_REFERENCE_CODE, payment.getMerchantReferenceId());

        // reference the authorization ID returned by the hosted CyberSource payment page
        requestParameters.put(CC_CAPTURE_SERVICE_AUTH_REQUEST_ID, payment.getAuthorizationId());

        // currency is US dollar
        requestParameters.put(PURCHASE_TOTALS_CURRENCY, USD);

        // set the sale amount
        requestParameters.put(PURCHASE_TOTALS_GRAND_TOTAL_AMOUNT, new DecimalFormat("#.00").format(payment.getAmount()));

        return requestParameters;
    }

    private void handleCriticalException(Exception e, Map<String, String> request) {

        logger.error("CyberSource transaction failure", e);

        // TODO future story - send an email to Graduate School operations including the error and the request details?
    }


}
