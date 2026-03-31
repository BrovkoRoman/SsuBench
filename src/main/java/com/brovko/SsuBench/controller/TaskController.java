package com.brovko.SsuBench.controller;

import com.brovko.SsuBench.dto.TaskRequestDto;
import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.entity.Task;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.BlockedUserException;
import com.brovko.SsuBench.exception.BusinessException;
import com.brovko.SsuBench.service.BidService;
import com.brovko.SsuBench.service.JwtService;
import com.brovko.SsuBench.service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private BidService bidService;

    @GetMapping
    public PagedModel<Task> getTasks(Pageable pageable) {
        log.info("getTasks (requestId={})", MDC.get("requestId"));
        return new PagedModel<>(taskService.getTasks(pageable));
    }

    @PostMapping
    public Task createTask(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                           @RequestBody TaskRequestDto taskRequestDto) {
        log.info("createTask (requestId={})", MDC.get("requestId"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = jwtService.find(jwt);

            if (user != null && user.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            if (user != null && user.getRole() == User.Role.CUSTOMER) {
                Task task = taskService.createTask(taskRequestDto);
                task.setCustomerUser(user);
                return taskService.save(task);
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @PutMapping("/{taskId}/confirmBid/{bidId}")
    public Task confirmBid(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                           @PathVariable Long taskId, @PathVariable Long bidId) {
        log.info("confirmBid (requestId={})", MDC.get("requestId"));
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = jwtService.find(jwt);
            Bid bid = bidService.findById(bidId);
            Task task = taskService.findById(taskId);

            if (user != null && user.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            if (task.getConfirmedBid() != null) {
                throw new BusinessException("The task already has a confirmed bid", HttpServletResponse.SC_CONFLICT);
            }

            if (user != null && user.getId().equals(task.getCustomerUser().getId())
                    && bid.getTask().getId().equals(taskId)) {
                return taskService.confirmBid(bid);
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }

    @DeleteMapping("/{taskId}")
    public void deleteTask(@RequestHeader(name = "Authorization", required = false) String authorizationHeader,
                           @PathVariable Long taskId,
                           HttpServletResponse response) {
        log.info("deleteTask (requestId={})", MDC.get("requestId"));

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = jwtService.find(jwt);
            Task task = taskService.findById(taskId);

            if (user != null && user.isBlocked()) {
                throw new BlockedUserException("You are blocked");
            }

            if (task == null) {
                throw new BusinessException("Not Found", HttpServletResponse.SC_NOT_FOUND);
            }

            if (user != null && user.getRole() == User.Role.ADMIN) {
                if (task.getConfirmedBid() != null) {
                    log.info("ADMIN OPERATION: deleting the task which has a confirmed bid (requestId={})",
                            MDC.get("requestId"));
                }

                taskService.deleteById(taskId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            if (task.getConfirmedBid() != null) {
                throw new BusinessException("The task already has a confirmed bid", HttpServletResponse.SC_FORBIDDEN);
            }

            if (user != null && user.getId().equals(task.getCustomerUser().getId())) {
                taskService.deleteById(taskId);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
