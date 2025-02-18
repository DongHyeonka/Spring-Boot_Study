package com.springboot.rabitmq.service.domain;

public class MessageSanitizer {
    public String sanitize(String message) {
        return message;
    }

    // private final Whitelist whitelist = Whitelist.basic();
    
    // public String sanitize(String content) {
    //     // HTML 태그 제거 및 이스케이프 처리
    //     String sanitized = Jsoup.clean(content, whitelist);
        
    //     // 추가적인 특수 문자 이스케이프
    //     sanitized = escapeSpecialCharacters(sanitized);
        
    //     return sanitized;
    // }
    
    // private String escapeSpecialCharacters(String content) {
    //     return content
    //         .replace("<", "&lt;")
    //         .replace(">", "&gt;")
    //         .replace("\"", "&quot;")
    //         .replace("'", "&#x27;")
    //         .replace("&", "&amp;");
    // }
}
