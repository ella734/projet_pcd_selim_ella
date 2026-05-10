package com.medical.platform.service;

import com.medical.platform.dto.AuthResponse;
import com.medical.platform.dto.HopitalDTO;
import com.medical.platform.dto.ServiceDTO;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.User;
import com.medical.platform.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthSessionService {

    private final UserRepository userRepository;

    public AuthSessionService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse buildResponse(String username) {
        User user = userRepository.findByLoginU(username).orElseThrow();
        Integer medecinId = user.getMedecin() != null ? user.getMedecin().getIdentifiantM() : null;

        return new AuthResponse(
            user.getIdentifiantU(),
            user.getLoginU(),
            user.getRole(),
            medecinId,
            buildServiceDtos(user.getService())
        );
    }

    private List<ServiceDTO> buildServiceDtos(ServiceMedical service) {
        List<ServiceDTO> serviceDtos = new ArrayList<>();
        if (service == null) {
            return serviceDtos;
        }

        HopitalDTO hopitalDto = null;
        if (service.getHopital() != null) {
            hopitalDto = new HopitalDTO(service.getHopital().getIdentifiantH(), service.getHopital().getLibelleH());
        }

        serviceDtos.add(new ServiceDTO(service.getIdentifiantS(), service.getLibelleS(), hopitalDto));
        return serviceDtos;
    }
}
