package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.crypto.service.ECCKeysService;
import br.com.bbs.crypto.service.serviceImpl.ECCService;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;

import javax.management.InvalidApplicationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @GetMapping("/verify")
    public String getPrescriptionData(@RequestParam(name = "cipher") String medicineCipher){

        CryptographyService crypto = new ECCService();
        Path path = Paths.get("./src/main/resources/patient/private-key.pem");
        try {
            String privateKey = Files.readString(path);
            byte[] privateKeyBytes = KeyPairDTO.getPublicKeyDecoded(privateKey);
            String base64PrivateKey = Base64.toBase64String(privateKeyBytes);
            return crypto.decrypt(base64PrivateKey, medicineCipher);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidApplicationException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping()
    public ResponseEntity<String> getPublicKey() throws InvalidApplicationException {

        String publicKey = null;

        try {

            Path path = Paths.get("./src/main/resources/patient/public-key.pem");
            publicKey = Files.readString(path);

        } catch (Exception e1) {
            try {

                ECCKeysService ecc = new ECCService();
                KeyPairDTO keyPairDTO = ecc.generateKeyPair();

                publicKey = keyPairDTO.getPublicKey();

                FileOutputStream publicOutputStream = new FileOutputStream("./src/main/resources/patient/public-key.pem");
                publicOutputStream.write(keyPairDTO.getPublicKey().getBytes());
                publicOutputStream.close();

                FileOutputStream privateOutputStream = new FileOutputStream("./src/main/resources/patient/private-key.pem");
                privateOutputStream.write(keyPairDTO.getPrivateKey().getBytes());
                privateOutputStream.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok(publicKey);
    }
}
