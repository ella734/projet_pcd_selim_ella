package com.medical.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDTO {
    private Integer identifiantS;
    private String libelleS;
    private HopitalDTO hopital;
}
