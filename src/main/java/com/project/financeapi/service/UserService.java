package com.project.financeapi.service;

import com.project.financeapi.dto.user.LoginRequestDTO;
import com.project.financeapi.dto.user.SignupRequestDTO;
import com.project.financeapi.dto.user.UpdatePasswordRequestDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.enums.UserStatus;
import com.project.financeapi.exception.*;
import com.project.financeapi.repository.UserRepository;
import com.project.financeapi.dto.user.TokenUser;
import com.project.financeapi.dto.util.JwtPayload;
import com.project.financeapi.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public TokenUser signup(SignupRequestDTO dto){

        if(userRepository.findByEmail(dto.email()).isPresent()){
            throw new EmailAlreadyExistsException(HttpStatus.CONFLICT, dto.email());
        }

        User user = new User(dto.name(), dto.email(), passwordEncoder.encode(dto.password()));

        User userRepo = userRepository.save(user);

        String token = jwtUtil.generateToken(userRepo);

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

        String token = jwtUtil.generateToken(userExists);

        return new TokenUser(token);
    }

    public TokenUser updatePassword(String tokenUser, UpdatePasswordRequestDTO dto){

        JwtPayload payload = jwtUtil.extractPayload(tokenUser);

        User user = userRepository.findByEmail(payload.email())
                .orElseThrow(() -> new InvalidEmailOrPasswordException("Email ou senha incorreta."));


        if(!user.getTokenVersion().equals(payload.tokenVersion())) {
            throw new InvalidJwtException(HttpStatus.FORBIDDEN, "É necessário refazer o login.");
        }

        String passwordEncrypted = passwordEncoder.encode(dto.password());
        user.setPassword(passwordEncrypted);
        user.setTokenVersion(user.getTokenVersion() + 1);

        String token = jwtUtil.generateToken(user);

        userRepository.save(user);

        return new TokenUser(token);
    }
}
