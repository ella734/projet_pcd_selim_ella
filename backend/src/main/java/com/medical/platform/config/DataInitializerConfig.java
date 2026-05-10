package com.medical.platform.config;

import com.medical.platform.entities.HopitalStructureSoin;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.User;
import com.medical.platform.repositories.HopitalStructureSoinRepository;
import com.medical.platform.repositories.ServiceRepository;
import com.medical.platform.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class DataInitializerConfig {

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap-admin.username:}")
    private String adminLogin;

    @Value("${app.bootstrap-admin.password:}")
    private String adminPassword;

    @Value("${app.bootstrap-admin.role:ADMIN}")
    private String adminRole;

    @Bean
    public CommandLineRunner bootstrapData(
        UserRepository userRepository,
        HopitalStructureSoinRepository hopitalRepository,
        ServiceRepository serviceRepository,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!bootstrapAdminEnabled) {
                return;
            }

            if (!StringUtils.hasText(adminLogin) || !StringUtils.hasText(adminPassword)) {
                throw new IllegalStateException(
                    "Admin bootstrap is enabled but credentials are missing. Set BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD."
                );
            }

            ServiceMedical defaultService = serviceRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> createDefaultService(hopitalRepository, serviceRepository));

            User admin = userRepository.findByLoginU(adminLogin).orElseGet(User::new);
            if (admin.getIdentifiantU() == null) {
                admin.setIdentifiantU(nextUserId(userRepository));
            }
            admin.setLoginU(adminLogin);
            admin.setMotPasseU(passwordEncoder.encode(adminPassword));
            admin.setRole(adminRole);
            if (admin.getService() == null) {
                admin.setService(defaultService);
            }
            userRepository.save(admin);
            System.out.println("Admin synchronized: " + adminLogin + " (" + adminRole + ")");

            bootstrapDemoUser(userRepository, passwordEncoder, "investigateur", "Invest1234!", "MEDECIN_INVESTIGATEUR", defaultService);
            bootstrapDemoUser(userRepository, passwordEncoder, "suivi", "Suivi1234!", "MEDECIN_SUIVI", defaultService);
            bootstrapDemoUser(userRepository, passwordEncoder, "labo", "Labo1234!", "AGENT_LABORATOIRE", defaultService);
            bootstrapDemoUser(userRepository, passwordEncoder, "immuno", "Immuno1234!", "AGENT_IMMUNO", defaultService);
        };
    }

    private void bootstrapDemoUser(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        String login,
        String password,
        String role,
        ServiceMedical service
    ) {
        User user = userRepository.findByLoginU(login).orElseGet(User::new);
        if (user.getIdentifiantU() == null) {
            user.setIdentifiantU(nextUserId(userRepository));
        }
        user.setLoginU(login);
        user.setMotPasseU(passwordEncoder.encode(password));
        user.setRole(role);
        if (user.getService() == null) {
            user.setService(service);
        }
        userRepository.save(user);
        System.out.println("Demo user synchronized: " + login + " (" + role + ")");
    }

    private ServiceMedical createDefaultService(
        HopitalStructureSoinRepository hopitalRepository,
        ServiceRepository serviceRepository
    ) {
        HopitalStructureSoin hopital = new HopitalStructureSoin();
        hopital.setIdentifiantH(nextHopitalId(hopitalRepository));
        hopital.setLibelleH("Hopital par defaut");
        hopital.setDescriptionH("Structure creee automatiquement pour le compte admin");
        HopitalStructureSoin savedHopital = hopitalRepository.save(hopital);

        ServiceMedical service = new ServiceMedical();
        service.setIdentifiantS(nextServiceId(serviceRepository));
        service.setLibelleS("Service par defaut");
        service.setHopital(savedHopital);
        service.setNbLitsS(0);
        service.setNbChambresS(0);
        service.setNbMedecinsS(0);
        return serviceRepository.save(service);
    }

    private Integer nextHopitalId(HopitalStructureSoinRepository hopitalRepository) {
        return hopitalRepository.findAll().stream()
            .map(HopitalStructureSoin::getIdentifiantH)
            .filter(java.util.Objects::nonNull)
            .max(Integer::compareTo)
            .map(id -> id + 1)
            .orElse(1);
    }

    private Integer nextServiceId(ServiceRepository serviceRepository) {
        return serviceRepository.findAll().stream()
            .map(ServiceMedical::getIdentifiantS)
            .filter(java.util.Objects::nonNull)
            .max(Integer::compareTo)
            .map(id -> id + 1)
            .orElse(1);
    }

    private Integer nextUserId(UserRepository userRepository) {
        return userRepository.findAll().stream()
            .map(User::getIdentifiantU)
            .filter(java.util.Objects::nonNull)
            .max(Integer::compareTo)
            .map(id -> id + 1)
            .orElse(1);
    }
}
