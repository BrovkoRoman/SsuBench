package com.brovko.SsuBench.controller;

import com.brovko.SsuBench.dto.BidRequestDto;
import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.entity.Task;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.BlockedUserException;
import com.brovko.SsuBench.exception.BusinessException;
import com.brovko.SsuBench.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import static com.brovko.SsuBench.entity.User.Role.ADMIN;

@Slf4j
@RestController
@RequestMapping("/bid")
public class BidController {
    @Autowired
    private BidService bidService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public PagedModel<Bid> getBids(Pageable pageable) {
        log.info("getBids (requestId={})", MDC.get("requestId"));
        return new PagedModel<>(bidService.getBids(pageable));
    }

    @PostMapping
    public Bid createBid(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                         @RequestBody BidRequestDto bidRequestDto) {
        log.info("createBid (requestId={})", MDC.get("requestId"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = jwtService.find(jwt);

            if (user != null && user.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            if (user != null && user.getRole() == User.Role.EXECUTOR) {
                Bid bid = bidService.createBid(bidRequestDto);
                bid.setExecutorUser(user);
                return bidService.save(bid);
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @PutMapping("/{bidId}/submitToReview")
    public Bid submitBidToReview(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                                 @PathVariable Long bidId) {
        log.info("submitToReview (requestId={})", MDC.get("requestId"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = jwtService.find(jwt);
            Bid bid = bidService.findById(bidId);

            if (user != null && user.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            if (bid.getStatus() != Bid.BidStatus.IN_PROGRESS) {
                throw new BusinessException("This bid is not in progress", HttpServletResponse.SC_CONFLICT);
            }

            if (user != null && user.getId().equals(bid.getExecutorUser().getId())) {
                bid.setStatus(Bid.BidStatus.IN_REVIEW_BY_CUSTOMER);
                return bidService.save(bid);
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @PutMapping("/{bidId}/review")
    public Bid reviewBid(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                         @PathVariable Long bidId,
                         @RequestParam boolean accept) {
        log.info("reviewBid (requestId={})", MDC.get("requestId"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User customerUser = jwtService.find(jwt);

            if (customerUser != null && customerUser.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            Bid bid = bidService.findById(bidId);
            Task task = bid.getTask();
            User executorUser = bid.getExecutorUser();

            if (bid.getStatus() != Bid.BidStatus.IN_REVIEW_BY_CUSTOMER) {
                throw new BusinessException("This bid is not in review", HttpServletResponse.SC_CONFLICT);
            }

            if (customerUser != null && customerUser.getId().equals(task.getCustomerUser().getId())) {
                if (accept) {
                    paymentService.transferMoney(customerUser, executorUser, bid, task.getRewardMoney());
                    bid.setStatus(Bid.BidStatus.DONE);
                } else {
                    bid.setStatus(Bid.BidStatus.IN_PROGRESS);
                }

                return bidService.save(bid);
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @DeleteMapping("/{bidId}")
    public void deleteBid(@PathVariable Long bidId,
                          @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        log.info("ADMIN OPERATION: deleteBid (requestId={})", MDC.get("requestId"));

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User adminUser = jwtService.find(jwt);

            if (adminUser == null || adminUser.getRole() != ADMIN) {
                throw new BusinessException("You should have admin rights to do this operation",
                        HttpServletResponse.SC_FORBIDDEN);
            }

            Bid bid = bidService.findById(bidId);

            if (bid == null) {
                throw new BusinessException("Not Found", HttpServletResponse.SC_NOT_FOUND);
            }

            bidService.deleteById(bidId);
            return;
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
