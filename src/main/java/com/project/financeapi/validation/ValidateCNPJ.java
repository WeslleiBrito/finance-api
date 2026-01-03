package com.project.financeapi.validation;

public class ValidateCNPJ {

    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) return false;

        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() != 14) return false;

        // Rejeita CNPJs com todos os dígitos iguais
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
            }

            int firstDigit = 11 - (sum % 11);
            firstDigit = (firstDigit >= 10) ? 0 : firstDigit;

            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
            }

            int secondDigit = 11 - (sum % 11);
            secondDigit = (secondDigit >= 10) ? 0 : secondDigit;

            return firstDigit == Character.getNumericValue(cnpj.charAt(12))
                    && secondDigit == Character.getNumericValue(cnpj.charAt(13));

        } catch (Exception e) {
            return false;
        }
    }

}
