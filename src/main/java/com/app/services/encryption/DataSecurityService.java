package com.app.services.encryption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@Service
public class DataSecurityService {

    private static final int GCM_TAG_LENGTH = 16;
    private EncryptionMetadata encryptionMetadata;

    public DataSecurityService() {
        InputStream is = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            is = EncryptionMetadata.class.getResourceAsStream("/encryptionConfig.json");
            this.encryptionMetadata = mapper.readValue(is, EncryptionMetadata.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public String encryptString(String plainStr) {
        InputStream is = null;
        try {
            byte[] ivBytes = Base64.getDecoder().decode(encryptionMetadata.getIv());
            Key key = getAESKey(encryptionMetadata.getKey().getBytes(StandardCharsets.UTF_8));
            Cipher encryptionCipher = getAESCipher(key, ivBytes, true);
            is = IOUtils.toInputStream(plainStr);
            return cryptInputStream(encryptionCipher, is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public String decryptString(String encryptedStr) {
        InputStream is = null;
        try {
            byte[] ivBytes = Base64.getDecoder().decode(encryptionMetadata.getIv());
            Key key = getAESKey(encryptionMetadata.getKey().getBytes(StandardCharsets.UTF_8));
            Cipher encryptionCipher = getAESCipher(key, ivBytes, false);
            is = new ByteArrayInputStream(Base64.getDecoder().decode(encryptedStr));
            String base64Str = cryptInputStream(encryptionCipher, is);
            return new String(Base64.getDecoder().decode(base64Str));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private static String cryptInputStream(Cipher cipher, InputStream inputStream) {
        OutputStream outputStream = null;
        File tempOutputFile = null;

        try {
            tempOutputFile = File.createTempFile("crypt-", ".tmp");
            outputStream = Files.newOutputStream(tempOutputFile.toPath());
            byte[] inputBuffer = new byte[1024];
            int len;
            while ((len = inputStream.read(inputBuffer)) != -1) {
                byte[] outputBuffer = cipher.update(inputBuffer, 0, len);
                if (outputBuffer != null) {
                    outputStream.write(outputBuffer);
                }
            }
            byte[] outputBuffer = cipher.doFinal();
            if (outputBuffer != null) {
                outputStream.write(outputBuffer);
            }
            String rv = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(tempOutputFile.getPath())));
            tempOutputFile.delete();
            return rv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private static Key getAESKey(byte[] key) {
        return new SecretKeySpec(key, "AES");
    }

    private static Cipher getAESCipher(Key key, byte[] ivBytes, boolean isEncryptionMode) {
        try {
            AlgorithmParameterSpec iv = new GCMParameterSpec(GCM_TAG_LENGTH * 8, ivBytes);

            int mode = isEncryptionMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, key, iv);

            return cipher;
        } catch (Throwable t) {
            throw new RuntimeException("Error while creating AES cipher: ", t);
        }
    }


    @VisibleForTesting
    private EncryptionMetadata getEncryptionMetadata() {
        return encryptionMetadata;
    }

    public static class EncryptionMetadata {
        private String iv;
        private String key;

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
