package br.com.bbs.mobile.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize()
public class BlockDTO {

    private String medicine;
    private String patientKey;
    private String doctorKey;
    private String signature;
    private LocalDate creationDate;
    private LocalDate expirationDate;

    @Override
    public String toString() {
        return "[" +
                "medicine='" + getSubstring(medicine) + '\'' +
                ", patientKey='" + getSubstring(patientKey) + '\'' +
                ", doctorKey='" + getSubstring(doctorKey)+ '\'' +
                ", creationDate=" + creationDate +
                ", expirationDate=" + expirationDate +
                ", signature='" + getSubstring(signature) + '\'' +
                ']';
    }

    private String getSubstring(String text) {
        return text.substring(text.length() - 10);
    }
}
