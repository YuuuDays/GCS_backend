package com.example.GCS.validation;

import lombok.EqualsAndHashCode;
import lombok.Getter;

// 概要:バリデーションチェック結果を返す汎用クラス
@EqualsAndHashCode
public class ValidationResult {
    private final boolean isValid;
    @Getter
    private final String field;        // どのフィールドのエラーか
    @Getter
    private final String errorMessage;

    private ValidationResult(boolean isValid, String field, String errorMessage) {
        this.isValid = isValid;
        this.field = field;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult error(String field, String message) {
        return new ValidationResult(false, field, message);
    }
}
/*
* 使用例
*    public ValidationResult validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return ValidationResult.error("email", "メールアドレスは必須です");
* */