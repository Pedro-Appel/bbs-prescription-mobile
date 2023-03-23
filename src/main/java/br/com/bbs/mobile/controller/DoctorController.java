package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.crypto.service.SignatureService;
import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.crypto.service.serviceImpl.ECDSAService;

import br.com.bbs.mobile.model.dto.BlockDTO;
import br.com.bbs.mobile.model.dto.PrescriptionDTO;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @PostMapping()
    public ResponseEntity createBlock(@RequestBody PrescriptionDTO prescription, @RequestParam(name = "patientKey") String patientKey){
        byte[] publicKeyBytes = KeyPairDTO.getPublicKeyDecoded(patientKey);
        String base64PublicKey = Base64.toBase64String(publicKeyBytes);
        CryptographyService crypto = new ECCService();
        try {
            String cipherMedicine = crypto.encrypt(base64PublicKey, prescription.getMedicine());
            return ResponseEntity.ok(signBlock(prescription,patientKey, cipherMedicine));
        } catch (InvalidApplicationException e) {
            throw new RuntimeException(e);
        }
    }
    public BlockDTO signBlock(PrescriptionDTO prescription,String patientKey, String cipherMedicine) throws InvalidApplicationException {

        SignatureService ecc = new ECDSAService();
        String privateKey = null;
        String publicKey = null;
        try {

            Path pathToPrivate = Paths.get("./src/main/resources/doctor/private-key.pem");
            privateKey = Files.readString(pathToPrivate);

            Path pathToPublic = Paths.get("./src/main/resources/doctor/public-key.pem");
            publicKey = Files.readString(pathToPublic);

        } catch (Exception e) {
            try {

                KeyPairDTO keyPairDTO = ecc.generateKeyPair();
                privateKey = keyPairDTO.getPrivateKey();

                FileOutputStream publicOutputStream = new FileOutputStream("./src/main/resources/doctor/public-key.pem");
                publicOutputStream.write(keyPairDTO.getPublicKey().getBytes());
                publicOutputStream.close();

                FileOutputStream privateOutputStream = new FileOutputStream("./src/main/resources/doctor/private-key.pem");
                privateOutputStream.write(keyPairDTO.getPrivateKey().getBytes());
                privateOutputStream.close();

            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }

        byte[] privateKeyBytes = KeyPairDTO.getPublicKeyDecoded(privateKey);
        String base64PublicKey = Base64.toBase64String(privateKeyBytes);
        String signature = ecc.sign(base64PublicKey, cipherMedicine);

        return new BlockDTO(cipherMedicine,
                patientKey,
                publicKey,
                LocalDateTime.now().format(FORMATTER),
                prescription.getExpiration().format(FORMATTER),
                signature
        );

    }
}
