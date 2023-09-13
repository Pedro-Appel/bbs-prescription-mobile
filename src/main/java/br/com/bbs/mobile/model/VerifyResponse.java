package br.com.bbs.mobile.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class VerifyResponse {

    private final boolean valid;
    private final String data;
    private String reason;

    public VerifyResponse(boolean valid, String plainText) {
        this.valid = valid;
        this.data = plainText;
    }
    public VerifyResponse(boolean valid, String plainText, String reason) {
        this.valid = valid;
        this.data = plainText;
        this.reason = reason;
    }
}
