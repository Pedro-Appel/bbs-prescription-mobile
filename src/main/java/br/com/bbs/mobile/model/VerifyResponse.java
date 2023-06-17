package br.com.bbs.mobile.model;

import lombok.Getter;

@Getter
public class VerifyResponse implements Response {
    private final boolean valid;
    private final String data;

    public VerifyResponse(boolean valid, String plainText) {
        this.valid = valid;
        this.data = plainText;
    }
}
