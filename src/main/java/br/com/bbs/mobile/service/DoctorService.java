package br.com.bbs.mobile.service;

import br.com.bbs.crypto.exception.CipherException;
import br.com.bbs.crypto.exception.KeyParseException;
import br.com.bbs.crypto.model.dto.KeyPairDTO;
import br.com.bbs.crypto.service.CryptographyService;
import br.com.bbs.crypto.service.SignatureService;
import br.com.bbs.crypto.service.serviceImpl.ECDSAService;
import br.com.bbs.mobile.exception.DoctorException;
import br.com.bbs.mobile.model.dto.BlockDTO;
import br.com.bbs.mobile.model.PrescriptionRecord;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.time.LocalDate;

@Slf4j
@Service
public class DoctorService {
    private final KeyService keyService;
    private final String keyPath;
    public DoctorService(KeyService keyService, @Value("${application.key-path.doctor}") String keyPath) {
        this.keyService = keyService;
        this.keyPath = keyPath;
    }

    public BlockDTO signBlock(PrescriptionRecord prescription, String patientKey, String cipherMedicine, SignatureService ecc) {

        log.info("Signing block ....");
        String doctorPrivateKey = keyService.getPrivate(keyPath);
        String doctorPublicKey = keyService.getPublic(keyPath);

        log.info("Signing block ....");
        String doctorPrivateKeyString = getKeyString(doctorPrivateKey);

        try {
            String signature = ecc.sign(doctorPrivateKeyString, cipherMedicine);
            return new BlockDTO(cipherMedicine,
                    patientKey,
                    doctorPublicKey,
                    signature,
                    LocalDate.now(),
                    LocalDate.now().plusDays(prescription.expirationInDays())
            );

        } catch (KeyParseException | CipherException e) {
            throw new DoctorException(e);
        }
    }

    public ResponseEntity<BlockDTO> createBlock(PrescriptionRecord prescription, String patientKey, CryptographyService crypto) {
        log.info("Creating block ....");
        try {
            String patientPrivateKey = getKeyString(patientKey);
            String medicineCipher = crypto.encrypt(patientPrivateKey, prescription.medicine());
            BlockDTO signedBlock = signBlock(prescription, patientKey, medicineCipher, new ECDSAService());
            log.info("Successfully created block .... {}", signedBlock.toString());
            return ResponseEntity.ok(signedBlock);
        } catch (InvalidApplicationException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getKeyString(String doctorPrivateKey) {
        byte[] privateKeyBytes = KeyPairDTO.getUrlDecoded(doctorPrivateKey);
        return Base64.toBase64String(privateKeyBytes);
    }
}

