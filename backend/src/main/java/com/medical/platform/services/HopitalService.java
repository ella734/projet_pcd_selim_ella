package com.medical.platform.services;

import com.medical.platform.entities.HopitalStructureSoin;
import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.AffectationPatientService;
import com.medical.platform.entities.Medecin;
import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.entities.User;
import com.medical.platform.repositories.HopitalStructureSoinRepository;
import com.medical.platform.repositories.MedecinRepository;
import com.medical.platform.repositories.PatientIdAdminRepository;
import com.medical.platform.repositories.ServiceRepository;
import com.medical.platform.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class HopitalService {

    @Autowired
    private HopitalStructureSoinRepository hopitalRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private PatientIdAdminRepository patientRepository;

    /**
     * Récupérer tous les hôpitaux
     */
    public List<HopitalStructureSoin> getAllHopitaux() {
        return hopitalRepository.findAll();
    }

    /**
     * Récupérer un hôpital par ID
     */
    public Optional<HopitalStructureSoin> getHopitalById(Integer hopitalId) {
        return hopitalRepository.findById(hopitalId);
    }

    /**
     * Créer un nouvel hôpital
     */
    @Transactional("transactionManager")
    public HopitalStructureSoin createHopital(HopitalStructureSoin hopital) {
        return hopitalRepository.save(hopital);
    }

    /**
     * Mettre à jour un hôpital
     */
    @Transactional("transactionManager")
    public HopitalStructureSoin updateHopital(Integer hopitalId, HopitalStructureSoin hopitalDetails) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        if (hopital.isPresent()) {
            HopitalStructureSoin h = hopital.get();
            if (hopitalDetails.getLibelleH() != null) h.setLibelleH(hopitalDetails.getLibelleH());
            if (hopitalDetails.getAdresseH() != null) h.setAdresseH(hopitalDetails.getAdresseH());
            if (hopitalDetails.getNbBlocH() != null) h.setNbBlocH(hopitalDetails.getNbBlocH());
            if (hopitalDetails.getNbServiceH() != null) h.setNbServiceH(hopitalDetails.getNbServiceH());
            if (hopitalDetails.getNbLitsH() != null) h.setNbLitsH(hopitalDetails.getNbLitsH());
            if (hopitalDetails.getDescriptionH() != null) h.setDescriptionH(hopitalDetails.getDescriptionH());
            if (hopitalDetails.getDateCreationH() != null) h.setDateCreationH(hopitalDetails.getDateCreationH());
            return hopitalRepository.save(h);
        }
        throw new RuntimeException("Hôpital non trouvé");
    }

    /**
     * Supprimer un hôpital
     */
    @Transactional("transactionManager")
    public void deleteHopital(Integer hopitalId) {
        HopitalStructureSoin hopital = hopitalRepository.findById(hopitalId)
            .orElseThrow(() -> new RuntimeException("Hôpital non trouvé"));

        for (ServiceMedical service : new HashSet<>(serviceRepository.findByHopital(hopital))) {
            for (User user : new HashSet<>(service.getUsers())) {
                user.setService(null);
                userRepository.save(user);
            }
            service.getUsers().clear();

            for (Medecin medecin : new HashSet<>(service.getMedecins())) {
                medecin.setService(null);
                medecinRepository.save(medecin);
            }
            service.getMedecins().clear();

            for (AffectationPatientService affectation : new HashSet<>(service.getAffectations())) {
                PatientIdAdmin patient = affectation.getPatient();
                if (patient != null) {
                    patient.getAffectations().remove(affectation);
                    patientRepository.save(patient);
                }
            }
            service.getAffectations().clear();

            serviceRepository.delete(service);
        }

        hopitalRepository.delete(hopital);
    }

    /**
     * Récupérer tous les services d'un hôpital
     */
    public List<ServiceMedical> getServicesOfHopital(Integer hopitalId) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        if (hopital.isPresent()) {
            return serviceRepository.findByHopital(hopital.get());
        }
        throw new RuntimeException("Hôpital non trouvé");
    }

    /**
     * Ajouter un service à un hôpital
     */
    @Transactional("transactionManager")
    public HopitalStructureSoin ajouterServiceAHopital(Integer hopitalId, ServiceMedical service) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        if (hopital.isPresent()) {
            HopitalStructureSoin h = hopital.get();
            service.setHopital(h);
            service.setIdHopital(hopitalId);
            if (service.getIdentifiantS() == null) {
                Integer nextId = serviceRepository.findAll().stream()
                    .map(ServiceMedical::getIdentifiantS)
                    .filter(java.util.Objects::nonNull)
                    .max(Integer::compareTo)
                    .map(id -> id + 1)
                    .orElse(1);
                service.setIdentifiantS(nextId);
            }
            serviceRepository.save(service);
            h.getServices().add(service);
            return hopitalRepository.save(h);
        }
        throw new RuntimeException("Hôpital non trouvé");
    }

    /**
     * Retirer un service d'un hôpital
     */
    @Transactional("transactionManager")
    public HopitalStructureSoin retirerServiceDeHopital(Integer hopitalId, Integer serviceId) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);

        if (hopital.isPresent() && service.isPresent()) {
            HopitalStructureSoin h = hopital.get();
            ServiceMedical s = service.get();
            h.getServices().remove(s);
            return hopitalRepository.save(h);
        }
        throw new RuntimeException("Hôpital ou Service non trouvé");
    }
}

