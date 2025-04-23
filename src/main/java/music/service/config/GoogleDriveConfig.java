package music.service.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GoogleDriveConfig {

    @Bean
    public Drive driveService() throws GeneralSecurityException, IOException {
        InputStream keyFile = new ClassPathResource("service-account.json").getInputStream();
        GoogleCredentials credentials = GoogleCredentials.fromStream(keyFile)
                .createScoped(Arrays.asList(
                        DriveScopes.DRIVE,
                        DriveScopes.DRIVE_FILE,
                        DriveScopes.DRIVE_READONLY
                ));


        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        return new Drive.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName("Music-Service")
                .build();
    }
}
