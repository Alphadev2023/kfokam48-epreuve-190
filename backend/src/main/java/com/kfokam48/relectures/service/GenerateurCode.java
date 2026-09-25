package com.kfokam48.relectures.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * H8 / RG19 : code de 6 caracteres, sans caracteres ambigus a l'ecran d'un telephone
 * (ni 0, ni O, ni 1, ni I).
 */
@Component
public class GenerateurCode {

    public static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int LONGUEUR = 6;

    private final SecureRandom aleatoire = new SecureRandom();

    public String generer() {
        StringBuilder code = new StringBuilder(LONGUEUR);
        for (int i = 0; i < LONGUEUR; i++) {
            code.append(ALPHABET.charAt(aleatoire.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}