package tech.kp45.bids.bridge.common.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LocalPublicKeyResourceLoader {

    private static KeyPair KeyPair;

    public static void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate key pair", e);
            throw new RuntimeException("Failed to generate key pair", e);
        }
    }

    public static PublicKey getPublicKey() {
        return KeyPair.getPublic();
    }

    public static PrivateKey getPrivateKey() {
        return KeyPair.getPrivate();
    }
}
