package com.medical.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Integer userId;
    private String username;
    private String role;
    private Integer medecinId;
    private List<ServiceDTO> services;
}
