package com.project.financeapi.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.project.financeapi.entity.User;
import com.project.financeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Sincroniza apenas o ID do usuário com o banco local.
     * Nome e e-mail agora ficam exclusivamente no Firebase.
     */
    public void syncUser(String firebaseToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String firebaseUid = decodedToken.getUid();

            // Se o usuário já existe na tabela base, não faz nada
            if (userRepository.findById(firebaseUid).isPresent()) {
                return;
            }

            // A exclusividade do e-mail já foi garantida pelo Firebase,
            // então apenas criamos a âncora relacional no banco.
            User user = new User();
            user.setId(firebaseUid);

            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao sincronizar usuário no banco de dados: " + e.getMessage());
        }
    }
}