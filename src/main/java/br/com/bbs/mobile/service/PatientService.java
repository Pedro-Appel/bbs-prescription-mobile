package br.com.bbs.mobile.service;

import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.mobile.exception.ClientKeyException;
import br.com.bbs.mobile.model.KeyResponse;
import br.com.bbs.mobile.model.VerifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;

import static br.com.bbs.mobile.model.KeyResponse.KeyResponseBuilder;

@Slf4j
@Service
public class PatientService {

    private final String keyPath;
    private final KeyService keyService;
    private static final boolean VALID_CIPHER = true;
    private static final boolean INVALID_CIPHER = false;
    public PatientService(KeyService keyService, @Value("${application.key-path.patient}") String keyPath) {
        this.keyService = keyService;
        this.keyPath = keyPath;
    }


    public ResponseEntity<VerifyResponse> getPrescriptionData(String medicineCipher, CryptographyService crypto) {
        log.info("Validating cipher ....");
        try {

            String plainText = crypto.decrypt(keyService.getPrivate(keyPath), medicineCipher);
            return ResponseEntity.ok(new VerifyResponse(VALID_CIPHER, plainText));

        }  catch (ClientKeyException | InvalidApplicationException e) {

            log.error("Failed to verify cipher with exception: {}", e.getMessage());
            return ResponseEntity.ok(new VerifyResponse(INVALID_CIPHER, medicineCipher, e.getMessage()));
        }
    }


    public ResponseEntity<KeyResponse> getPublicKey() {

        log.info("Retrieving public key ....");
        KeyResponse keyResponse = KeyResponseBuilder().withPublicKey(keyService.getPublic(keyPath));

        return ResponseEntity.ok(keyResponse);
    }
}
