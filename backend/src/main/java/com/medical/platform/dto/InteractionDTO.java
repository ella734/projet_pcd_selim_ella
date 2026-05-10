package com.medical.platform.dto;

public class InteractionDTO {
    private String medicament1;
    private String medicament2;

    public InteractionDTO(String medicament1, String medicament2) {
        this.medicament1 = medicament1;
        this.medicament2 = medicament2;
    }

    public String getMedicament1() { return medicament1; }
    public String getMedicament2() { return medicament2; }
}