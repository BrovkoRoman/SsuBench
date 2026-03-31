package com.brovko.SsuBench;

import com.brovko.SsuBench.dto.BidRequestDto;
import com.brovko.SsuBench.dto.JwtResponseDto;
import com.brovko.SsuBench.dto.TaskRequestDto;
import com.brovko.SsuBench.dto.UserRegisterRequestDto;
import com.brovko.SsuBench.entity.Bid;
import com.brovko.SsuBench.entity.Task;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.service.BidService;
import com.brovko.SsuBench.service.JwtService;
import com.brovko.SsuBench.service.TaskService;
import com.brovko.SsuBench.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class IntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ssubench_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BidService bidService;

    @Test
    void testRegister() throws Exception {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto();
        userRegisterRequestDto.setLogin("testRegisterCustomer");
        userRegisterRequestDto.setPassword("12345");
        userRegisterRequestDto.setRole(User.Role.CUSTOMER);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        JwtResponseDto jwtResponseDto = objectMapper.readValue(contentAsString, JwtResponseDto.class);

        User user = jwtService.find(jwtResponseDto.getJwt());
        Assertions.assertEquals(user, userService.findByLogin("testRegisterCustomer"));
    }

    private String registerAndGetJwt(User.Role role) {
        return registerAndGetJwt(role, String.valueOf(role).toLowerCase(Locale.ROOT));
    }

    private String registerAndGetJwt(User.Role role, String login) {
        UserRegisterRequestDto userRegisterRequestDto = new UserRegisterRequestDto();
        userRegisterRequestDto.setLogin(login);
        userRegisterRequestDto.setPassword("12345");
        userRegisterRequestDto.setRole(role);

        User user = userService.createUser(userRegisterRequestDto);

        if (userService.findByLogin(user.getLogin()) != null) {
            user = userService.findByLogin(user.getLogin());
        } else {
            user = userService.save(user);
        }

        return jwtService.create(user);
    }

    @Test
    void testCreateTask() throws Exception {
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setText("task text");
        taskRequestDto.setRewardMoney(100L);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customerJwt)
                        .content(objectMapper.writeValueAsString(taskRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Task task = objectMapper.readValue(contentAsString, Task.class);

        Assertions.assertEquals(task.getText(), taskRequestDto.getText());
        Assertions.assertEquals(task.getRewardMoney(), taskRequestDto.getRewardMoney());
        Assertions.assertNull(task.getConfirmedBid());
        Assertions.assertEquals(task.getCustomerUser().getId(), jwtService.find(customerJwt).getId());
    }

    @Test
    void testCreateTaskExecutor() throws Exception {
        String executorJwt = registerAndGetJwt(User.Role.EXECUTOR);
        TaskRequestDto taskRequestDto = new TaskRequestDto();
        taskRequestDto.setText("task text");
        taskRequestDto.setRewardMoney(100L);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + executorJwt)
                        .content(objectMapper.writeValueAsString(taskRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    private Task createTask(Long rewardMoney) {
        Task task = new Task();

        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        task.setText("task text");
        task.setCustomerUser(jwtService.find(customerJwt));
        task.setRewardMoney(rewardMoney);

        return taskService.save(task);
    }

    @Test
    void testCreateBid() throws Exception {
        String executorJwt = registerAndGetJwt(User.Role.EXECUTOR);
        Task task = createTask(100L);

        BidRequestDto bidRequestDto = new BidRequestDto();
        bidRequestDto.setTaskId(task.getId());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + executorJwt)
                        .content(objectMapper.writeValueAsString(bidRequestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Bid bid = objectMapper.readValue(contentAsString, Bid.class);

        Assertions.assertEquals(bid.getExecutorUser().getId(), jwtService.find(executorJwt).getId());
        Assertions.assertEquals(Bid.BidStatus.NEW, bid.getStatus());
    }

    @Test
    void testCreateBidCustomer() throws Exception {
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);
        Task task = createTask(100L);

        BidRequestDto bidRequestDto = new BidRequestDto();
        bidRequestDto.setTaskId(task.getId());

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + customerJwt)
                        .content(objectMapper.writeValueAsString(bidRequestDto)))
                .andExpect(status().isUnauthorized());
    }

    private Bid createBid(Long taskId, Bid.BidStatus status) {
        String executorJwt = registerAndGetJwt(User.Role.EXECUTOR);

        BidRequestDto bidRequestDto = new BidRequestDto();
        bidRequestDto.setTaskId(taskId);

        Bid bid = bidService.createBid(bidRequestDto);
        bid.setExecutorUser(jwtService.find(executorJwt));
        bid.setStatus(status);

        return bidService.save(bid);
    }

    @Test
    void testConfirmBid() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.NEW);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/task/{taskId}/confirmBid/{bidId}", task.getId(), bid.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Task task1 = objectMapper.readValue(contentAsString, Task.class);

        bid = bidService.findById(bid.getId());
        Assertions.assertEquals(task1.getConfirmedBid().getId(), bid.getId());
        Assertions.assertEquals(Bid.BidStatus.IN_PROGRESS, bid.getStatus());

        Bid bid2 = createBid(task.getId(), Bid.BidStatus.NEW);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/task/{taskId}/confirmBid/{bidId}", task.getId(), bid2.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isConflict());
    }

    @Test
    void testConfirmBidWrongCustomer() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.NEW);
        String wrongCustomerJwt = registerAndGetJwt(User.Role.CUSTOMER, "wrongCustomer");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/task/{taskId}/confirmBid/{bidId}", task.getId(), bid.getId())
                        .header("Authorization", "Bearer " + wrongCustomerJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testConfirmBidNotFound() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.NEW);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/task/{taskId}/confirmBid/{bidId}", -11111111, bid.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSubmitToReview() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_PROGRESS);
        String executorJwt = registerAndGetJwt(User.Role.EXECUTOR);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/bid/{bidId}/submitToReview", bid.getId())
                        .header("Authorization", "Bearer " + executorJwt))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Bid bid1 = objectMapper.readValue(contentAsString, Bid.class);

        Assertions.assertEquals(Bid.BidStatus.IN_REVIEW_BY_CUSTOMER, bid1.getStatus());
    }

    @Test
    void testSubmitToReviewWrongExecutor() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_PROGRESS);
        String wrongExecutorJwt = registerAndGetJwt(User.Role.EXECUTOR, "wrongExecutor");

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/bid/{bidId}/submitToReview", bid.getId())
                        .header("Authorization", "Bearer " + wrongExecutorJwt))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testReviewBidAcceptTrue() throws Exception {
        Long rewardMoney = 100L;
        Task task = createTask(rewardMoney);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_REVIEW_BY_CUSTOMER);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        User customerUser = task.getCustomerUser();
        User executorUser = bid.getExecutorUser();

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/bid/{bidId}/review?accept=true", bid.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Bid bid1 = objectMapper.readValue(contentAsString, Bid.class);

        User newCustomerUser = userService.findById(customerUser.getId());
        User newExecutorUser = userService.findById(executorUser.getId());

        Assertions.assertEquals(Bid.BidStatus.DONE, bid1.getStatus());
        Assertions.assertEquals(newCustomerUser.getMoney(), customerUser.getMoney() - rewardMoney);
        Assertions.assertEquals(newExecutorUser.getMoney(), executorUser.getMoney() + rewardMoney);
    }

    @Test
    void testReviewBidAcceptFalse() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_REVIEW_BY_CUSTOMER);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .put("/bid/{bidId}/review?accept=false", bid.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Bid bid1 = objectMapper.readValue(contentAsString, Bid.class);

        Assertions.assertEquals(Bid.BidStatus.IN_PROGRESS, bid1.getStatus());
    }

    @Test
    void testReviewBidNotEnoughMoney() throws Exception {
        Task task = createTask(1000000L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_REVIEW_BY_CUSTOMER);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/bid/{bidId}/review?accept=true", bid.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = createTask(100L);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/task/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isNoContent());

        Assertions.assertNull(taskService.findById(task.getId()));
    }

    @Test
    void testDeleteTaskWithConfirmedBid() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_PROGRESS);
        task = taskService.confirmBid(bid);
        String customerJwt = registerAndGetJwt(User.Role.CUSTOMER);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/task/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + customerJwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteTaskWithConfirmedBidAdmin() throws Exception {
        Task task = createTask(100L);
        Bid bid = createBid(task.getId(), Bid.BidStatus.IN_PROGRESS);
        task = taskService.confirmBid(bid);
        String adminJwt = registerAndGetJwt(User.Role.ADMIN);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/task/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isNoContent());

        Assertions.assertNull(taskService.findById(task.getId()));
    }
}
