package br.com.bbs.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeyResponse implements Response {
    private final String publicKey;
}
