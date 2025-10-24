package com.odiplus.licensegenerator.licensing;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class LicenseVerifier {

    private final ObjectMapper objectMapper;
    private final ObjectWriter canonicalWriter;

    public LicenseVerifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        ObjectMapper canonicalMapper = objectMapper.copy()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.canonicalWriter = canonicalMapper.writer();
    }

    public LicenseVerificationResult verify(String licenseContent, String primaryPublicKeyPem, String secondaryPublicKeyPem) {
        JsonNode root = readLicense(licenseContent);
        JsonNode payloadNode = readPayloadNode(root);
        String signatureText = readSignature(root);

        byte[] payloadBytes = serializePayload(payloadNode);
        byte[] signatureBytes = decodeSignature(signatureText);

        PublicKey primaryKey = parsePublicKey(primaryPublicKeyPem, "primary");
        PublicKey secondaryKey = parsePublicKey(secondaryPublicKeyPem, "secondary");

        boolean primaryMatches = verifySignature(payloadBytes, signatureBytes, primaryKey);
        boolean secondaryMatches = verifySignature(payloadBytes, signatureBytes, secondaryKey);

        LicensePayload payload = objectMapper.convertValue(payloadNode, LicensePayload.class);
        return new LicenseVerificationResult(payload, signatureText, primaryMatches, secondaryMatches);
    }

    private JsonNode readLicense(String licenseContent) {
        try {
            JsonNode node = objectMapper.readTree(licenseContent);
            if (node == null || !node.isObject()) {
                throw new LicenseVerificationException("License dosyası beklenen formatta değil.");
            }
            return node;
        } catch (JsonProcessingException ex) {
            throw new LicenseVerificationException("Lisans dosyası JSON olarak okunamadı.", ex);
        }
    }

    private JsonNode readPayloadNode(JsonNode root) {
        JsonNode payloadNode = root.get("payload");
        if (payloadNode == null || !payloadNode.isObject()) {
            throw new LicenseVerificationException("Lisans dosyasında 'payload' alanı bulunamadı.");
        }
        return payloadNode;
    }

    private String readSignature(JsonNode root) {
        JsonNode signatureNode = root.get("signature");
        if (signatureNode == null || !signatureNode.isTextual()) {
            throw new LicenseVerificationException("Lisans dosyasında imza bilgisi bulunamadı.");
        }
        return signatureNode.asText();
    }

    private byte[] serializePayload(JsonNode payloadNode) {
        try {
            return canonicalWriter.writeValueAsBytes(payloadNode);
        } catch (JsonProcessingException ex) {
            throw new LicenseVerificationException("Lisans içeriği imza doğrulaması için hazırlanamadı.", ex);
        }
    }

    private byte[] decodeSignature(String signature) {
        try {
            return Base64.getDecoder().decode(signature);
        } catch (IllegalArgumentException ex) {
            throw new LicenseVerificationException("Lisans imzası Base64 formatında değil.", ex);
        }
    }

    private PublicKey parsePublicKey(String pem, String description) {
        String sanitized = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(sanitized);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new LicenseVerificationException(description + " public key okunamadı.", ex);
        }
    }

    private boolean verifySignature(byte[] payloadBytes, byte[] signature, PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(payloadBytes);
            return verifier.verify(signature);
        } catch (GeneralSecurityException ex) {
            throw new LicenseVerificationException("RSA imza doğrulaması başarısız oldu.", ex);
        }
    }
}
