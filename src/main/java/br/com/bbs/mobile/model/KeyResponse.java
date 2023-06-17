package br.com.bbs.mobile.model;

import br.com.bbs.mobile.model.Response;
import lombok.Getter;

@Getter
public class KeyResponse implements Response {
    private final String publicKey;
    public KeyResponse(String publicKey) {
        this.publicKey = publicKey;
    }
}
