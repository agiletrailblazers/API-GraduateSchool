package com.gs.api.controller.payment;

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

@Configuration
@RestController
@RequestMapping("/payment")
public class PaymentController {
    final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/reverse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void reversePayment(@RequestBody Payment payment) throws Exception  {

        logger.debug("Reversing payment for " + payment.getAuthorizationId() + " and amount " + payment.getAmount());

        return ;
    }
}
