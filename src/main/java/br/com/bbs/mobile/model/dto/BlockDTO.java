package br.com.bbs.mobile.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonSerialize()
public class BlockDTO {
    private String medicine;
    private String patientKey;
    private String doctorKey;
    private String creationDate;
    private String expirationDate;
    private String signature;

    public BlockDTO(String medicine, String patientKey, String doctorKey, String creationDate, String expirationDate, String signature) {
        this.medicine = medicine;
        this.patientKey = patientKey;
        this.doctorKey = doctorKey;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.signature = signature;
    }

}
