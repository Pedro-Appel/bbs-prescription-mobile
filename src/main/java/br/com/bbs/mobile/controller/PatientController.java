package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.crypto.service.ECCKeysService;
import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.mobile.model.Response;
import br.com.bbs.mobile.model.KeyResponse;
import br.com.bbs.mobile.model.VerifyResponse;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidApplicationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
@RestController
@RequestMapping("/patient")
public class PatientController {


    private static final boolean VALID_CIPHER = true;
    private static final boolean INVALID_CIPHER = false;
    public static final String PATIENT_DIRECTORY = System.getProperty("user.home") + "/patient";
    public static final String PUBLIC_KEY_PEM = "public-key.pem";
    public static final String PRIVATE_KEY_PEM = "private-key.pem";

    @GetMapping("/verify")
    public ResponseEntity<Response> getPrescriptionData(@RequestParam(name = "cipher") String medicineCipher) {

        log.info("Validating cipher ....");
        CryptographyService crypto = new ECCService();
        Path filePath = Paths.get(PATIENT_DIRECTORY + "/" + PRIVATE_KEY_PEM);
        try {
            String privateKey = Files.readString(filePath);
            byte[] privateKeyBytes = KeyPairDTO.getUrlDecoded(privateKey);
            String base64PrivateKey = Base64.toBase64String(privateKeyBytes);
            String plainText = crypto.decrypt(base64PrivateKey, medicineCipher);
            log.info("Valid cipher");
            return ResponseEntity.ok(new VerifyResponse(VALID_CIPHER, plainText));
        } catch (IOException e) {
            log.error("Failed to find key // {}", e.getMessage());
            return ResponseEntity.ok(new VerifyResponse(INVALID_CIPHER, "No key to decipher"));
        } catch (InvalidApplicationException e) {
            log.error("Failed to verify cipher with exception: {}", e.getMessage());
            return ResponseEntity.ok(new VerifyResponse(INVALID_CIPHER, medicineCipher));
        }
    }

    @GetMapping()
    public ResponseEntity<Response> getPublicKey() throws IOException {

        log.info("Retrieving public key ....");

        Path filePath = Paths.get(PATIENT_DIRECTORY + "/" + PUBLIC_KEY_PEM);
        if (!Files.exists(filePath)) {
            log.warn("No keys found");
            return ResponseEntity.ok(new KeyResponse(generateKeyPair()));
        }

        System.out.println("File already exists at: " + filePath);
        return ResponseEntity.ok(new KeyResponse(Files.readString(filePath)));
    }

    public String generateKeyPair() {
        try {
            log.info("Starting to generate KeyPair ....");

            ECCKeysService ecc = new ECCService();
            KeyPairDTO keyPairDTO = ecc.generateKeyPair();

            createFile(PUBLIC_KEY_PEM, keyPairDTO.getPublicKey());

            createFile(PRIVATE_KEY_PEM, keyPairDTO.getPrivateKey());

            log.info("Successfully generated KeyPair");

            return keyPairDTO.getPublicKey();

        } catch (IOException | InvalidApplicationException e) {
            log.error("Failed to generate key pair with exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void createFile(String fileName, String key) throws IOException {

        String fullPath = PATIENT_DIRECTORY + "/" + fileName;

        Path filePath = Paths.get(fullPath);

        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
        Files.write(filePath, key.getBytes());

        System.out.println("File created at: " + filePath);

    }
}
