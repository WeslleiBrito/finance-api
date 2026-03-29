package com.project.financeapi.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() {
        try {
            if(FirebaseApp.getApps().isEmpty()){
                InputStream serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream("finance-api-2a3f5-firebase-adminsdk-fbsvc-2b51b95afd.json");

                if(serviceAccount == null) {
                    throw new RuntimeException("Arquivo de configuração do Firebase não encontrado.");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK inicializado com sucesso!");
            }

        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inicializar o Firebase" + e.getMessage());
        }
    }


}
