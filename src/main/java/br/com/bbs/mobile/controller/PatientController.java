package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.mobile.model.KeyResponse;
import br.com.bbs.mobile.model.VerifyResponse;
import br.com.bbs.mobile.service.PatientService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/verify")
    public ResponseEntity<VerifyResponse> getPrescriptionData(@RequestParam(name = "cipher") String medicineCipher){
        return patientService.getPrescriptionData(medicineCipher, new ECCService());
    }

    @GetMapping()
    public ResponseEntity<KeyResponse> getPublicKey() {
        return patientService.getPublicKey();
    }


}
