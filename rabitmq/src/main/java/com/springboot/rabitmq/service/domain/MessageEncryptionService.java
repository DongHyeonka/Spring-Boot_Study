package com.springboot.rabitmq.service.domain;

public class MessageEncryptionService {
    public String encrypt(String message) {
        return message;
    }

    // private final String secretKey = "your-secret-key"; // 환경 변수에서 로드하는 것이 좋습니다
    
    // public String encrypt(String content) throws Exception {
    //     Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
    //     Cipher cipher = Cipher.getInstance("AES");
    //     cipher.init(Cipher.ENCRYPT_MODE, key);
        
    //     byte[] encrypted = cipher.doFinal(content.getBytes());
    //     return Base64.getEncoder().encodeToString(encrypted);
    // }
    
    // public String decrypt(String encryptedContent) throws Exception {
    //     Key key = new SecretKeySpec(secretKey.getBytes(), "AES");
    //     Cipher cipher = Cipher.getInstance("AES");
    //     cipher.init(Cipher.DECRYPT_MODE, key);
        
    //     byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedContent));
    //     return new String(decrypted);
    // }
}
