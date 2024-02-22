package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.exception.CipherException;
import br.com.bbs.crypto.exception.KeyParseException;
import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.crypto.service.SignatureService;
import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.crypto.service.serviceImpl.ECDSAService;

import br.com.bbs.mobile.model.dto.BlockDTO;
import br.com.bbs.mobile.model.dto.PrescriptionDTO;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
@RestController
@RequestMapping("/doctor")
public class DoctorController {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static final String DOCTOR_DIRECTORY = System.getProperty("user.home") + "/doctor";
    public static final String PUBLIC_KEY_PEM = "public-key.pem";
    public static final String PRIVATE_KEY_PEM = "private-key.pem";

    @PostMapping()
    public ResponseEntity<BlockDTO> createBlock(@RequestBody PrescriptionDTO prescription, @RequestParam(name = "patientKey") String patientKey) {
        log.info("Creating block ....");
        byte[] patientKeyDecoded = KeyPairDTO.getUrlDecoded(patientKey);
        String patientKeyBase64 = Base64.toBase64String(patientKeyDecoded);
        CryptographyService crypto = new ECCService();
        try {
            String cipherMedicine = crypto.encrypt(patientKeyBase64, prescription.getMedicine());
            BlockDTO signedBlock = signBlock(prescription, patientKey, cipherMedicine);
            log.info("Successfully created block .... {}", signedBlock.toString());
            return ResponseEntity.ok(signedBlock);
        } catch (InvalidApplicationException | KeyParseException | CipherException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockDTO signBlock(PrescriptionDTO prescription, String patientKey, String cipherMedicine) throws InvalidApplicationException, KeyParseException, CipherException, IOException {

        log.info("Signing block ....");
        SignatureService ecc = new ECDSAService();

        Path publicKeyFilePath = Paths.get(DOCTOR_DIRECTORY + "/" + PUBLIC_KEY_PEM);
        Path privateKeyFilePath = Paths.get(DOCTOR_DIRECTORY + "/" + PRIVATE_KEY_PEM);

        log.info("Retrieving keys ....");
        if (!Files.exists(publicKeyFilePath) || !Files.exists(privateKeyFilePath)) {
            log.warn("No keys found");
            generateKeyPair(ecc);
        }

        System.out.println("File already exists at: " + publicKeyFilePath);
        String doctorPublicKey = Files.readString(publicKeyFilePath);

        System.out.println("File already exists at: " + privateKeyFilePath);
        String doctorPrivateKey = Files.readString(privateKeyFilePath);

        log.info("Signing block ....");
        byte[] privateKeyBytes = KeyPairDTO.getUrlDecoded(doctorPrivateKey);
        String base64PublicKey = Base64.toBase64String(privateKeyBytes);
        String signature = ecc.sign(base64PublicKey, cipherMedicine);

        return new BlockDTO(cipherMedicine,
                patientKey,
                doctorPublicKey,
                LocalDateTime.now().format(FORMATTER),
                prescription.getExpiration().format(FORMATTER),
                signature
        );
    }

    public void generateKeyPair(SignatureService ecc) {
        try {
            log.warn("Failed to retrieve keys!!");
            log.info("Generating KeyPair ....");

            KeyPairDTO keyPairDTO = ecc.generateKeyPair();

            createFile(PUBLIC_KEY_PEM, keyPairDTO.getPublicKey().getBytes());

            createFile(PRIVATE_KEY_PEM, keyPairDTO.getPrivateKey().getBytes());

            log.info("Successfully generated KeyPair");

        } catch (IOException | InvalidApplicationException e) {
            log.error("Failed to generate key pair with exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void createFile(String fileName, byte[] key) throws IOException {

        String fullPath = DOCTOR_DIRECTORY + "/" + fileName;

        Path filePath = Paths.get(fullPath);

        Files.createDirectories(filePath.getParent());
        Files.createFile(filePath);
        Files.write(filePath, key);

        System.out.println("File created at: " + filePath);

    }
}
