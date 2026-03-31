package com.brovko.SsuBench.service;

import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.entity.Payment;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.MoneyException;
import com.brovko.SsuBench.repository.BidRepository;
import com.brovko.SsuBench.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BidRepository bidRepository;

    @Transactional
    public Bid transferMoneyAndAcceptBid(User fromUser, User toUser, Bid bid, Long amount) {
        if (fromUser.getMoney() < amount) {
            throw new MoneyException("Not enough money");
        }

        fromUser.setMoney(fromUser.getMoney() - amount);
        toUser.setMoney(toUser.getMoney() + amount);

        Payment payment = new Payment();
        payment.setBid(bid);
        payment.setAmount(amount);
        payment.setCustomerUser(fromUser);
        payment.setExecutorUser(toUser);

        paymentRepository.save(payment);
        bid.setStatus(Bid.BidStatus.DONE);
        return bidRepository.save(bid);
    }

    public Page<Payment> getPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }
}
