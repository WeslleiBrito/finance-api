package com.project.financeapi.validation;

public class ValidateCPF {

    public static boolean isValidCPF(String cpf) {
        if (cpf == null) return false;

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11) return false;

        // Rejeita CPFs com todos os dígitos iguais
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }

            int firstDigit = 11 - (sum % 11);
            firstDigit = (firstDigit >= 10) ? 0 : firstDigit;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }

            int secondDigit = 11 - (sum % 11);
            secondDigit = (secondDigit >= 10) ? 0 : secondDigit;

            return firstDigit == Character.getNumericValue(cpf.charAt(9))
                    && secondDigit == Character.getNumericValue(cpf.charAt(10));

        } catch (Exception e) {
            return false;
        }
    }

}
