package br.com.bbs.mobile.controller;

import br.com.bbs.crypto.service.serviceImpl.ECCService;
import br.com.bbs.mobile.model.dto.BlockDTO;
import br.com.bbs.mobile.model.PrescriptionRecord;
import br.com.bbs.mobile.service.DoctorService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping()
    public ResponseEntity<BlockDTO> createBlock(@RequestBody PrescriptionRecord prescription, @RequestParam(name = "patientKey") String patientKey){
        return doctorService.createBlock(prescription, patientKey, new ECCService());
    }
}
