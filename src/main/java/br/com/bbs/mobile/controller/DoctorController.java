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
import java.io.FileOutputStream;
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

    @PostMapping()
    public ResponseEntity createBlock(@RequestBody PrescriptionDTO prescription, @RequestParam(name = "patientKey") String patientKey){
        log.info("Creating block ....");
        byte[] patientKeyDecoded = KeyPairDTO.getUrlDecoded(patientKey);
        String patientKeyBase64 = Base64.toBase64String(patientKeyDecoded);
        CryptographyService crypto = new ECCService();
        try {
            String cipherMedicine = crypto.encrypt(patientKeyBase64, prescription.getMedicine());
            BlockDTO signedBlock = signBlock(prescription, patientKey, cipherMedicine);
            log.info("Successfully created block .... {}", signedBlock.toString());
            return ResponseEntity.ok(signedBlock);
        } catch (InvalidApplicationException | KeyParseException | CipherException e) {
            throw new RuntimeException(e);
        }
    }
    public BlockDTO signBlock(PrescriptionDTO prescription,String patientKey, String cipherMedicine) throws InvalidApplicationException, KeyParseException, CipherException {

        log.info("Signing block ....");
        SignatureService ecc = new ECDSAService();
        String doctorPrivateKey = null;
        String doctorPublicKey = null;
        try {

            log.info("Retrieving keys ....");
            Path pathToPrivate = Paths.get("doctor/private-key.pem");
            doctorPrivateKey = Files.readString(pathToPrivate);

            Path pathToPublic = Paths.get("doctor/public-key.pem");
            doctorPublicKey = Files.readString(pathToPublic);

        } catch (Exception e) {
            try {
                log.warn("Failed to retrieve keys!!");
                log.info("Generating KeyPair ....");

                KeyPairDTO keyPairDTO = ecc.generateKeyPair();
                doctorPrivateKey = keyPairDTO.getPrivateKey();
                doctorPublicKey = keyPairDTO.getPublicKey();

                FileOutputStream publicOutputStream = new FileOutputStream("doctor/public-key.pem");
                publicOutputStream.write(doctorPublicKey.getBytes());
                publicOutputStream.close();

                FileOutputStream privateOutputStream = new FileOutputStream("doctor/private-key.pem");
                privateOutputStream.write(doctorPrivateKey.getBytes());
                privateOutputStream.close();
                log.info("Successfully generated KeyPair");

            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

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
}
