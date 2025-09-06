package com.project.financeapi.controller;

import com.project.financeapi.dto.user.LoginRequestDTO;
import com.project.financeapi.dto.user.SignupRequestDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.dto.user.TokenUser;
import com.project.financeapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService){ this.userService = userService; }

    @PostMapping("/signup")
    public ResponseEntity<TokenUser> signup(@Valid @RequestBody SignupRequestDTO dto){
        TokenUser saved = userService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenUser> login(@Valid @RequestBody LoginRequestDTO dto) {
        TokenUser tokenUser = userService.login(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(tokenUser);
    }

    @PatchMapping("/update-password")
    public ResponseEntity<TokenUser> updatePassword(@RequestHeader("X-Auth-Token") String authToken,
                                                    @RequestBody String newPassword){
        TokenUser tokenUser = userService.updatePassword(authToken, newPassword);
        return ResponseEntity.status(HttpStatus.OK).body(tokenUser);
    }
}
