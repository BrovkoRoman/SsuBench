package com.brovko.SsuBench.controller;

import com.brovko.SsuBench.dto.JwtResponseDto;
import com.brovko.SsuBench.dto.UserLoginRequestDto;
import com.brovko.SsuBench.dto.UserRegisterRequestDto;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.BusinessException;
import com.brovko.SsuBench.service.JwtService;
import com.brovko.SsuBench.service.UserService;
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
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping
    public PagedModel<User> getUsers(Pageable pageable) {
        return new PagedModel<>(userService.getUsers(pageable));
    }

    @PostMapping("/register")
    public JwtResponseDto register(@RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        log.info("register (requestId={})", MDC.get("requestId"));
        User user = userService.createUser(userRegisterRequestDto);

        if (userService.findByLogin(user.getLogin()) != null) {
            throw new BusinessException("This user already exists", HttpServletResponse.SC_CONFLICT);
        }

        if (user.getRole() == ADMIN) {
            throw new BusinessException("You can't register as an admin", HttpServletResponse.SC_FORBIDDEN);
        }

        user = userService.save(user);
        return new JwtResponseDto(jwtService.create(user));
    }

    @PostMapping("/login")
    public JwtResponseDto login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        log.info("login (requestId={})", MDC.get("requestId"));
        userLoginRequestDto.validate();

        if (!userService.checkPassword(userLoginRequestDto.getLogin(), userLoginRequestDto.getPassword())) {
            throw new BusinessException("Incorrect login or password", HttpServletResponse.SC_UNAUTHORIZED);
        }

        User user = userService.findByLogin(userLoginRequestDto.getLogin());
        return new JwtResponseDto(jwtService.create(user));
    }

    @PutMapping("/{userId}/set-blocked")
    public User setBlocked(@PathVariable Long userId, @RequestParam boolean value,
                                     @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        log.info("ADMIN OPERATION: setBlocked (requestId={})", MDC.get("requestId"));

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User adminUser = jwtService.find(jwt);

            if (adminUser == null || adminUser.getRole() != ADMIN) {
                throw new BusinessException("You should have admin rights to do this operation",
                        HttpServletResponse.SC_FORBIDDEN);
            }

            User setBlockedUser = userService.findById(userId);

            if (setBlockedUser == null) {
                throw new BusinessException("Not Found", HttpServletResponse.SC_NOT_FOUND);
            }

            if (setBlockedUser.getRole() == ADMIN) {
                throw new BusinessException("Admins can't be blocked", HttpServletResponse.SC_BAD_REQUEST);
            }

            setBlockedUser.setBlocked(value);
            return userService.save(setBlockedUser);
        }

        throw new BusinessException("You don't have permission to do this operation", HttpServletResponse.SC_UNAUTHORIZED);
    }
}
