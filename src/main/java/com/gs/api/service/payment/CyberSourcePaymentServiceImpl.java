package com.gs.api.service.payment;

import com.cybersource.ws.client.Client;
import com.gs.api.domain.payment.Payment;
import com.gs.api.domain.payment.PaymentConfirmation;
import com.gs.api.exception.PaymentException;

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

    // TODO inject cybersource properties from config

    @Override
    public PaymentConfirmation processPayment(final Payment payment) throws PaymentException {

        // TODO add the parameters for the request
        Map<String, String> request = new HashMap<>();

        request.put( "ccCaptureService_run", "true" );

        // TODO We will let the Client get the merchantID from props and insert it
        // into the request Map.

        // so that you can efficiently track the order in the CyberSource
        // reports and transaction search screens, you should use the same
        // merchantReferenceCode for the auth and subsequent captures and
        // credits.
        request.put( "merchantReferenceCode", payment.getMerchantReferenceId() );

        // reference the transaction_id returned by the previous authorization.
        request.put( "ccCaptureService_authRequestID", payment.getAuthorizationId());

        // set the sale amount
        request.put( "purchaseTotals_currency", "USD" );

        // this sample assumes only the first item has been shipped.
        request.put( "purchaseTotals_grandTotalAmount", new DecimalFormat("#.00").format(payment.getAmount()));

        Properties props = new Properties();

        // TODO add the properties from the config

        // call CyberSource to complete the sale
        try {
            Map<String, String> response = Client.runTransaction(request, props);

            // TODO check the response for actual success vs failure, etc.

            return new PaymentConfirmation(payment, response.get(REQUEST_ID));
        }
        catch (Exception e) {
            throw new PaymentException(FAILED_TO_COMPLETE_SALE_MSG, e);
        }
    }
}
