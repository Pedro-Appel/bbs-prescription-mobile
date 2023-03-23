package br.com.bbs.mobile.model.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PrescriptionDTO {

    private String medicine;

    private LocalDateTime expiration;

    public PrescriptionDTO(String medicine, Integer expirationDays) {
        this.medicine = medicine;
        this.expiration = LocalDateTime.now().plusDays(expirationDays);
    }

    @Override
    public String toString() {
        return "PrescriptionDTO{" +
                "medicine='" + medicine + '\'' +
                ", expiration=" + expiration +
                '}';
    }
}
