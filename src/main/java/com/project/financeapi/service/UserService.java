package com.project.financeapi.service;

import com.project.financeapi.entity.User;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.exception.EmailAlreadyExistsException;
import com.project.financeapi.exception.InvalidEmailOrPasswordException;
import com.project.financeapi.exception.InvalidJwtException;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.dto.TokenUser;
import com.project.financeapi.dto.JwtPayload;
import com.project.financeapi.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public TokenUser signup(User user){
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException(HttpStatus.CONFLICT, user.getEmail());
        }

        String id = UUID.randomUUID().toString();

        user.setId(id);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String token = JwtUtil.generateToken(user);
        userRepository.save(user);

        return new TokenUser(token);
    }

    public TokenUser login(User user){

        User userExists = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new InvalidEmailOrPasswordException(HttpStatus.NOT_FOUND, "Email ou senha incorreta."));

        if(!passwordEncoder.matches(user.getPassword(), userExists.getPassword())){
            throw new InvalidEmailOrPasswordException(HttpStatus.NOT_FOUND, "Email ou senha incorreta.");
        }

        String token = JwtUtil.generateToken(user);

        return new TokenUser(token);
    }

    public TokenUser updatePassword(String tokenUser, String newPassword){
        System.out.println(tokenUser);
        if(newPassword.length() < 4){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 4 caracteres");
        }

        JwtPayload payload = JwtUtil.extractPayload(tokenUser);

        User user = userRepository.findByEmail(payload.email())
                .orElseThrow(() -> new InvalidEmailOrPasswordException(HttpStatus.NOT_FOUND, "Email ou senha incorreta."));

        if(!user.getTokenVersion().equals(payload.tokenVersion())) {
            throw new InvalidJwtException(HttpStatus.FORBIDDEN, "É necessário refazer o login.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);

        String token = JwtUtil.generateToken(user);

        userRepository.save(user);

        return new TokenUser(token);
    }
}
