package com.project.financeapi.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.project.financeapi.dto.user.SyncUserRequestDTO;
import com.project.financeapi.entity.User;
import com.project.financeapi.exception.*;
import com.project.financeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Sincroniza o usuário criado no Firebase com o banco de dados local.
     * Esta rota deve ser chamada pelo Aplicativo logo após o usuário se cadastrar no Firebase.
     */
    public void syncUser(SyncUserRequestDTO dto, String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();

            // Em vez de pegar do DTO, pegamos o email verdadeiro extraído do token do Google!
            String emailDoToken = decodedToken.getEmail();

            if (userRepository.findById(firebaseUid).isPresent()) {
                return;
            }

            if(userRepository.findByEmail(emailDoToken).isPresent()){
                throw new EmailAlreadyExistsException(HttpStatus.CONFLICT, emailDoToken);
            }

            User user = new User();
            user.setId(firebaseUid);
            user.setName(dto.name()); // O nome vem do DTO (enviado pelo App)
            user.setEmail(emailDoToken); // O email vem do Token do Google

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar: " + e.getMessage());
        }
    }
}
