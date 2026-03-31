package com.brovko.SsuBench.controller;

import com.brovko.SsuBench.entity.Payment;
import com.brovko.SsuBench.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @GetMapping("/")
    public PagedModel<Payment> getPayments(Pageable pageable) {
        log.info("getPayments (requestId={})", MDC.get("requestId"));
        return new PagedModel<>(paymentService.getPayments(pageable));
    }
}
