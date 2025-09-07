package com.project.financeapi.service;

import com.project.financeapi.dto.user.LoginRequestDTO;
import com.project.financeapi.dto.user.SignupRequestDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.UserStatus;
import com.project.financeapi.exception.*;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.dto.user.TokenUser;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public TokenUser signup(SignupRequestDTO dto){

        if(userRepository.findByEmail(dto.email()).isPresent()){
            throw new EmailAlreadyExistsException(HttpStatus.CONFLICT, dto.email());
        }

        User user = new User(dto.name(), dto.email(), dto.password());

        User userRepo = userRepository.save(user);

        String token = JwtUtil.generateToken(userRepo);

        return new TokenUser(token);
    }

    public TokenUser login(LoginRequestDTO dto){

        User userExists = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidEmailOrPasswordException("Email ou senha incorreta."));

        if(!passwordEncoder.matches(dto.password(), userExists.getPassword())){
            throw new InvalidEmailOrPasswordException("Email ou senha incorreta.");
        }

        if(!userExists.getUserStatus().equals(UserStatus.ACTIVATED)){
            throw new AccessBlockedException("O usuário não tem permissão para acessar o APP.");
        }

        String token = JwtUtil.generateToken(userExists);

        return new TokenUser(token);
    }

    public TokenUser updatePassword(String tokenUser, String newPassword){
        System.out.println(tokenUser);
        if(newPassword.length() < 4){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A senha deve ter pelo menos 4 caracteres");
        }

        JwtPayload payload = JwtUtil.extractPayload(tokenUser);

        User user = userRepository.findByEmail(payload.email())
                .orElseThrow(() -> new InvalidEmailOrPasswordException("Email ou senha incorreta."));

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
