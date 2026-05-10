package com.medical.platform.services;

import com.medical.platform.entities.Medecin;
import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.repositories.MedecinRepository;
import com.medical.platform.repositories.PatientIdAdminRepository;
import com.medical.platform.repositories.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private PatientIdAdminRepository patientRepository;

    /**
     * Assigner un médecin à un service
     */
    @Transactional("transactionManager")
    public Medecin assignerMedecinAuService(Integer medecinId, Integer serviceId) {
        Optional<Medecin> medecin = medecinRepository.findById(medecinId);
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);

        if (medecin.isPresent() && service.isPresent()) {
            Medecin m = medecin.get();
            ServiceMedical s = service.get();
            validateServiceBelongsToMedecinHopital(m, s);
            m.setService(s);
            return medecinRepository.save(m);
        }
        throw new RuntimeException("Médecin ou Service non trouvé");
    }

    /**
     * Retirer un médecin d'un service
     */
    @Transactional("transactionManager")
    public Medecin retirerMedecinDuService(Integer medecinId, Integer serviceId) {
        Optional<Medecin> medecin = medecinRepository.findById(medecinId);
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);

        if (medecin.isPresent() && service.isPresent()) {
            Medecin m = medecin.get();
            m.setService(null);
            return medecinRepository.save(m);
        }
        throw new RuntimeException("Médecin ou Service non trouvé");
    }

    /**
     * Récupérer tous les médecins
     */
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    /**
     * Récupérer tous les médecins d'un service
     */
    public List<Medecin> getMedecinsByService(Integer serviceId) {
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);
        if (service.isPresent()) {
            return medecinRepository.findByServices(service.get());
        }
        return List.of();
    }

    public List<Medecin> getMedecinsByHopital(Integer hopitalId) {
        return medecinRepository.findByHopitalId(hopitalId);
    }

    /**
     * Récupérer un médecin par ID
     */
    public Optional<Medecin> getMedecinById(Integer medecinId) {
        return medecinRepository.findById(medecinId);
    }

    /**
     * Créer un nouveau médecin
     */
    @Transactional("transactionManager")
    public Medecin createMedecin(Medecin medecin) {
        if (medecin.getService() != null && medecin.getService().getIdentifiantS() != null) {
            ServiceMedical service = serviceRepository.findById(medecin.getService().getIdentifiantS())
                .orElseThrow(() -> new RuntimeException("Service non trouvÃ©"));
            validateServiceBelongsToMedecinHopital(medecin, service);
            medecin.setService(service);
        }
        return medecinRepository.save(medecin);
    }

    /**
     * Mettre à jour un médecin
     */
    @Transactional("transactionManager")
    public Medecin updateMedecin(Integer medecinId, Medecin medecinDetails) {
        Optional<Medecin> medecin = medecinRepository.findById(medecinId);
        if (medecin.isPresent()) {
            Medecin m = medecin.get();
            boolean hopitalUpdated = false;
            
            // Basic fields
            if (medecinDetails.getNomM() != null) m.setNomM(medecinDetails.getNomM());
            if (medecinDetails.getPrenomM() != null) m.setPrenomM(medecinDetails.getPrenomM());
            if (medecinDetails.getDateNaissM() != null) m.setDateNaissM(medecinDetails.getDateNaissM());
            if (medecinDetails.getSexeM() != null) m.setSexeM(medecinDetails.getSexeM());
            if (medecinDetails.getNumTelM() != null) m.setNumTelM(medecinDetails.getNumTelM());
            if (medecinDetails.getNumTelWhatsAppM() != null) m.setNumTelWhatsAppM(medecinDetails.getNumTelWhatsAppM());
            if (medecinDetails.getAdresseDomM() != null) m.setAdresseDomM(medecinDetails.getAdresseDomM());
            if (medecinDetails.getSpecialiteM() != null) m.setSpecialiteM(medecinDetails.getSpecialiteM());
            if (medecinDetails.getDateDernierDiplomeM() != null) m.setDateDernierDiplomeM(medecinDetails.getDateDernierDiplomeM());
            if (medecinDetails.getTypeMedecin() != null) m.setTypeMedecin(medecinDetails.getTypeMedecin());
            if (medecinDetails.getIndexHopitalM() != null) {
                m.setIndexHopitalM(medecinDetails.getIndexHopitalM());
                hopitalUpdated = true;
            }

            // Relationships (Many-to-One)
            if (medecinDetails.getService() != null) {
                serviceRepository.findById(medecinDetails.getService().getIdentifiantS())
                    .ifPresent(service -> {
                        validateServiceBelongsToMedecinHopital(m, service);
                        m.setService(service);
                    });
            } else if (hopitalUpdated && m.getService() != null) {
                validateServiceBelongsToMedecinHopital(m, m.getService());
            }

            return medecinRepository.save(m);
        }
        throw new RuntimeException("Médecin non trouvé");
    }

    /**
     * Supprimer un médecin (avec nettoyage des relations)
     */
    @Transactional("transactionManager")
    public void deleteMedecin(Integer medecinId) {
        Medecin medecin = medecinRepository.findById(medecinId)
            .orElseThrow(() -> new RuntimeException("Médecin non trouvé"));

        for (PatientIdAdmin patient : new HashSet<>(medecin.getPatients())) {
            patient.getMedecins().remove(medecin);
        }
        medecin.getPatients().clear();
        medecin.setService(null);
        medecinRepository.delete(medecin);
    }

    private void validateServiceBelongsToMedecinHopital(Medecin medecin, ServiceMedical service) {
        Integer medecinHopitalId = parseHopitalId(medecin.getIndexHopitalM());
        Integer serviceHopitalId = resolveServiceHopitalId(service);

        if (medecinHopitalId == null || serviceHopitalId == null) {
            return;
        }

        if (!medecinHopitalId.equals(serviceHopitalId)) {
            throw new IllegalArgumentException("Le mÃ©decin ne peut choisir qu'un service liÃ© Ã  son hÃ´pital.");
        }
    }

    private Integer parseHopitalId(String hopitalId) {
        if (hopitalId == null || hopitalId.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(hopitalId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer resolveServiceHopitalId(ServiceMedical service) {
        if (service.getHopital() != null && service.getHopital().getIdentifiantH() != null) {
            return service.getHopital().getIdentifiantH();
        }
        return service.getIdHopital();
    }
}

