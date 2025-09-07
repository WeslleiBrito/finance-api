package com.project.financeapi.util;

public class CpfValidator {

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            long cpfNúmeros = Long.parseLong(cpf);
        } catch (NumberFormatException e) {
            return false;
        }

        int[] digit = new int[11];
        for (int i = 0; i < 11; i++) {
            digit[i] = Character.getNumericValue(cpf.charAt(i));
        }

        // Validação do primeiro dígito verificador
        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += digit[i] * (10 - i);
        }
        int firstCheckDigit = 11 - (sum1 % 11);
        if (firstCheckDigit >= 10) {
            firstCheckDigit = 0;
        }

        if (firstCheckDigit != digit[9]) {
            return false;
        }

        // Validação do segundo dígito verificador
        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += digit[i] * (11 - i);
        }
        int secondCheckDigit = 11 - (sum2 % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }

        return secondCheckDigit == digit[10];
    }
}