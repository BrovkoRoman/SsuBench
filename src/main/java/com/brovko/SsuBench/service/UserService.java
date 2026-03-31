package com.brovko.SsuBench.service;

import com.brovko.SsuBench.dto.UserRegisterRequestDto;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public boolean checkPassword(String login, String password) {
        User dbUser = findByLogin(login);
        return dbUser != null && BCrypt.checkpw(password, dbUser.getPassword());
    }

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User createUser(UserRegisterRequestDto userRegisterRequestDto) {
        userRegisterRequestDto.validate();
        User user = new User();
        user.setLogin(userRegisterRequestDto.getLogin());
        user.setPassword(BCrypt.hashpw(userRegisterRequestDto.getPassword(), BCrypt.gensalt(12)));
        user.setRole(userRegisterRequestDto.getRole());
        return user;
    }
}
