package com.gs.api.controller.payment;

import com.gs.api.controller.BaseController;
import com.gs.api.domain.payment.Payment;
import com.gs.api.service.payment.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Configuration
@RestController
@RequestMapping("/payment")
public class PaymentController extends BaseController {

    final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/reverse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void reversePayment(@RequestBody List<Payment> payments) throws Exception  {

        // TODO should we be passing user id in the URI and verifying user with token?

        logger.debug("Reversing {} payments", payments.size());

        for (Payment payment : payments) {

            logger.debug("Processing payment authorization reversal, payment reference number {}", payment.getMerchantReferenceId());

            paymentService.reversePayment(payment);

            logger.info("Successful payment authorization reversal, payment reference number {}", payment.getMerchantReferenceId());
        }
    }
}
