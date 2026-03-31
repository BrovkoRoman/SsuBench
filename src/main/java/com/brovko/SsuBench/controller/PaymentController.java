package com.brovko.SsuBench.controller;

import com.brovko.SsuBench.entity.Payment;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.BusinessException;
import com.brovko.SsuBench.service.JwtService;
import com.brovko.SsuBench.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.brovko.SsuBench.entity.User.Role.ADMIN;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public PagedModel<Payment> getPayments(Pageable pageable,
                                           @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        log.info("ADMIN OPERATION: getPayments (requestId={})", MDC.get("requestId"));
        jwtService.checkAdminRights(authorizationHeader);
        return new PagedModel<>(paymentService.getPayments(pageable));
    }
}
