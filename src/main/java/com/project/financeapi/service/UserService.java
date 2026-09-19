package com.project.financeapi.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.project.financeapi.entity.User;
import com.project.financeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Sincroniza apenas o ID do usuário com o banco local.
     * Nome e e-mail agora ficam exclusivamente no Firebase.
     */
    public User syncUser(String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();

            // Se o usuário já existe na tabela base, não faz nada
            Optional<User> userExists = userRepository.findById(firebaseUid);

            if (userExists.isPresent()) {
                return userExists.get();
            }

            // A exclusividade do e-mail já foi garantida pelo Firebase,
            // então apenas criamos a âncora relacional no banco.
            User user = new User();
            user.setId(firebaseUid);

            return userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar usuário no banco de dados: " + e.getMessage());
        }
    }
}