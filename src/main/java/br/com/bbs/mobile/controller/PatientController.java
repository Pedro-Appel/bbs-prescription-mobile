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

    @GetMapping("/verify")
    public ResponseEntity<Response> getPrescriptionData(@RequestParam(name = "cipher") String medicineCipher){

        log.info("Validating cipher ....");
        CryptographyService crypto = new ECCService();
        Path path = Paths.get("patient/private-key.pem");
        try {
            String privateKey = Files.readString(path);
            byte[] privateKeyBytes = KeyPairDTO.getUrlDecoded(privateKey);
            String base64PrivateKey = Base64.toBase64String(privateKeyBytes);
            String plainText = crypto.decrypt(base64PrivateKey, medicineCipher);
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
    public ResponseEntity<Response> getPublicKey() throws InvalidApplicationException {

        log.info("Retrieving public key ....");
        String publicKey = null;

        try {

            Path path = Paths.get("patient/public-key.pem");
            publicKey = Files.readString(path);

        } catch (Exception e1) {
            try {
                log.warn("No keys found");
                log.info("Starting to generate KeyPair ....");

                ECCKeysService ecc = new ECCService();
                KeyPairDTO keyPairDTO = ecc.generateKeyPair();

                publicKey = keyPairDTO.getPublicKey();

                FileOutputStream publicOutputStream = new FileOutputStream("patient/public-key.pem");
                publicOutputStream.write(keyPairDTO.getPublicKey().getBytes());
                publicOutputStream.close();

                FileOutputStream privateOutputStream = new FileOutputStream("patient/private-key.pem");
                privateOutputStream.write(keyPairDTO.getPrivateKey().getBytes());
                privateOutputStream.close();
                log.info("Successfully generated KeyPair");

            } catch (IOException e) {
                log.error("Failed to generate key pair with exception: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(new KeyResponse(publicKey));
    }
}
