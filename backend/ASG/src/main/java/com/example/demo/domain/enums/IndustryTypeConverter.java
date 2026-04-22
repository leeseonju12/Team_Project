package com.example.demo.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IndustryTypeConverter implements AttributeConverter<IndustryType, String> {

    // 1. Java Enum -> DB 문자열로 저장할 때
    @Override
    public String convertToDatabaseColumn(IndustryType attribute) {
        if (attribute == null) {
            return null;
        }
        // 새로운 데이터는 무조건 깔끔한 영문 코드('CAFE')로 저장합니다.
        return attribute.name(); 
    }

    // 2. DB 문자열 -> Java Enum으로 읽어올 때 (에러 방지의 핵심!)
    @Override
    public IndustryType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            // 1) 먼저 영문 코드("CAFE")로 매칭을 시도해 봅니다.
            return IndustryType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // 2) 여기서 에러가 났다는 건, 과거에 저장된 한글 데이터("카페 / 베이커리")라는 뜻입니다.
            // 아까 Enum에 만들어둔 fromDescription 메서드를 써서 한글로 매칭해줍니다.
            return IndustryType.fromDescription(dbData);
        }
    }
}