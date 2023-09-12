package br.com.bbs.mobile.model;

import lombok.Getter;

@Getter
public class KeyResponse {

    private String publicKey;

    private KeyResponse() {}

    public static KeyResponse KeyResponseBuilder(){
        return new KeyResponse();
    }

    public KeyResponse withPublicKey(String publicKey){
        this.publicKey = publicKey;
        return this;
    }
}
