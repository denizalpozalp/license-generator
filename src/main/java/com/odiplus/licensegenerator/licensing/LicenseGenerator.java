package com.odiplus.licensegenerator.licensing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class LicenseGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseGenerator.class);

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final ObjectWriter canonicalWriter;

    public LicenseGenerator(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
        ObjectMapper canonicalMapper = objectMapper.copy()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.canonicalWriter = canonicalMapper.writer();
    }

    public LicenseGenerationResult generate(LicenseGenerationRequest request) {
        LOGGER.info("Generating license for '{}'", request.customerName());

        KeyPair keyPair = generateKeyPair(request.keySize());
        LicensePayload payload = buildPayload(request);

        ObjectNode payloadNode = objectMapper.valueToTree(payload);
        byte[] payloadBytes = serializePayload(payloadNode);
        String signature = signPayload(payloadBytes, keyPair.getPrivate());

        writeLicenseFile(request.licensePath(), payloadNode, signature);
        writePemFile(request.publicKeyPath(), "PUBLIC KEY", keyPair.getPublic().getEncoded());
        writePemFile(request.privateKeyPath(), "PRIVATE KEY", keyPair.getPrivate().getEncoded());

        LOGGER.info("License, public key and private key generated successfully.");
        return new LicenseGenerationResult(
                request.licensePath(),
                request.publicKeyPath(),
                request.privateKeyPath(),
                payload,
                signature);
    }

    private KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new LicenseGenerationException("Unable to generate RSA key pair", ex);
        }
    }

    private LicensePayload buildPayload(LicenseGenerationRequest request) {
        Instant issuedAt = request.issuedAt();
        if (issuedAt == null) {
            issuedAt = Instant.now(clock);
        }
        return new LicensePayload(
                request.customerName(),
                issuedAt,
                request.expiresAt(),
                request.hardwareId());
    }

    private byte[] serializePayload(JsonNode payloadNode) {
        try {
            return canonicalWriter.writeValueAsBytes(payloadNode);
        } catch (JsonProcessingException ex) {
            throw new LicenseGenerationException("Unable to serialize license payload", ex);
        }
    }

    private String signPayload(byte[] payloadBytes, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(payloadBytes);
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (GeneralSecurityException ex) {
            throw new LicenseGenerationException("Unable to sign license payload", ex);
        }
    }

    private void writeLicenseFile(Path licensePath, ObjectNode payloadNode, String signature) {
        ObjectNode root = objectMapper.createObjectNode();
        root.set("payload", payloadNode);
        root.put("signature", signature);

        createParentDirectory(licensePath);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(licensePath.toFile(), root);
        } catch (IOException ex) {
            throw new LicenseGenerationException("Unable to write license file", ex);
        }
    }

    private void writePemFile(Path path, String type, byte[] content) {
        createParentDirectory(path);
        String encoded = Base64.getEncoder().encodeToString(content);
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN ").append(type).append("-----\n");
        for (int i = 0; i < encoded.length(); i += 64) {
            int end = Math.min(i + 64, encoded.length());
            builder.append(encoded, i, end).append('\n');
        }
        builder.append("-----END ").append(type).append("-----\n");

        try {
            Files.writeString(path, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new LicenseGenerationException("Unable to write " + type.toLowerCase() + " file", ex);
        }
    }

    private void createParentDirectory(Path path) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException ex) {
                throw new LicenseGenerationException("Unable to create directory " + parent, ex);
            }
        }
    }
}
