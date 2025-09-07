package com.project.financeapi.util;

public class CnpjValidator {

    public static boolean isValid(String CNPJ) {
        if (CNPJ == null || CNPJ.length() != 14 || CNPJ.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            long cnpjNumeros = Long.parseLong(CNPJ);
        } catch (NumberFormatException e) {
            return false;
        }

        int[] digits = new int[14];
        for (int i = 0; i < 14; i++) {
            digits[i] = Character.getNumericValue(CNPJ.charAt(i));
        }

        // Validação do primeiro dígito verificador
        int sum1 = 0;
        int[] weight1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 12; i++) {
            sum1 += digits[i] * weight1[i];
        }
        int firstCheckDigit = 11 - (sum1 % 11);
        if (firstCheckDigit >= 10) {
            firstCheckDigit = 0;
        }

        if (firstCheckDigit != digits[12]) {
            return false;
        }

        // Validação do segundo dígito verificador
        int sum2 = 0;
        int[] weight2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int i = 0; i < 13; i++) {
            sum2 += digits[i] * weight2[i];
        }
        int secondCheckDigit = 11 - (sum2 % 11);
        if (secondCheckDigit >= 10) {
            secondCheckDigit = 0;
        }

        return secondCheckDigit == digits[13];
    }
}