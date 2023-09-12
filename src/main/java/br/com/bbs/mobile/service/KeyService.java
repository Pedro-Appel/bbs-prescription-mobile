package br.com.bbs.mobile.service;

import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.ECCKeysService;
import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.mobile.exception.ClientKeyException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;

import javax.management.InvalidApplicationException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class KeyService {

    public String getPrivate(String keyPath){

        try {
            String privateKey = Files.readString(Paths.get(keyPath,"private-key.pem"));
            byte[] privateKeyBytes = KeyPairDTO.getUrlDecoded(privateKey);
            return Base64.toBase64String(privateKeyBytes);
        } catch (IOException e) {
            throw new ClientKeyException(e);
        }
    }

    public String getPublic(String keyPath) {
        Path path = Paths.get(keyPath + "public-key.pem").toAbsolutePath();
        try {
            return Files.readString(path);
        } catch (IOException e) {
            log.warn("No keys found");
            log.info("Starting to generate KeyPair ....");
            return generateKeyPair(new ECCService(), keyPath);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new ClientKeyException(e);
        }
    }

    public String generateKeyPair(ECCKeysService ecc, String keyPath) {

        try {
            KeyPairDTO keyPairDTO = ecc.generateKeyPair();
            log.info("Successfully generated KeyPair");

            saveKey("public-key.pem", keyPairDTO.getPublicKey().getBytes(StandardCharsets.UTF_8), keyPath);
            saveKey("private-key.pem", keyPairDTO.getPrivateKey().getBytes(StandardCharsets.UTF_8), keyPath);
            log.info("Successfully saved KeyPair in : {}", keyPath);

            return keyPairDTO.getPublicKey();

        } catch (IOException | InvalidApplicationException e) {
            log.error("Failed to generate key pair with exception: {}", e.getMessage());
            throw new ClientKeyException(e);
        }
    }

    private void saveKey(String fileName, byte[] keyBytes, String keyPath){
        try (FileOutputStream privateOutputStream = new FileOutputStream(keyPath + fileName)) {
            privateOutputStream.write(keyBytes);
        } catch (IOException e) {
            throw new ClientKeyException(e);
        }
    }
}
