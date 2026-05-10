package com.medical.platform.services;

import com.medical.platform.entities.ServiceMedical;
import com.medical.platform.entities.HopitalStructureSoin;
import com.medical.platform.entities.Medecin;
import com.medical.platform.entities.User;
import com.medical.platform.entities.AffectationPatientService;
import com.medical.platform.entities.PatientIdAdmin;
import com.medical.platform.repositories.ServiceRepository;
import com.medical.platform.repositories.HopitalStructureSoinRepository;
import com.medical.platform.repositories.MedecinRepository;
import com.medical.platform.repositories.PatientIdAdminRepository;
import com.medical.platform.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Service
public class ServiceHospitalierService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private HopitalStructureSoinRepository hopitalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private PatientIdAdminRepository patientRepository;

    /**
     * Assigner un utilisateur à un service
     */
    @Transactional("transactionManager")
    public ServiceMedical assignerUtilisateurAuService(Integer serviceId, Integer userId) {
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);
        Optional<User> user = userRepository.findById(userId);

        if (service.isPresent() && user.isPresent()) {
            ServiceMedical s = service.get();
            User u = user.get();
            s.getUsers().add(u);
            return serviceRepository.save(s);
        }
        throw new RuntimeException("Service ou Utilisateur non trouvé");
    }

    /**
     * Retirer un utilisateur d'un service
     */
    @Transactional("transactionManager")
    public ServiceMedical retirerUtilisateurDuService(Integer serviceId, Integer userId) {
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);
        Optional<User> user = userRepository.findById(userId);

        if (service.isPresent() && user.isPresent()) {
            ServiceMedical s = service.get();
            User u = user.get();
            s.getUsers().remove(u);
            return serviceRepository.save(s);
        }
        throw new RuntimeException("Service ou Utilisateur non trouvé");
    }

    /**
     * Récupérer tous les services
     */
    public List<ServiceMedical> getAllServices() {
        return serviceRepository.findAll();
    }

    /**
     * Récupérer tous les services d'un hôpital
     */
    public List<ServiceMedical> getServicesByHopital(Integer hopitalId) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        if (hopital.isPresent()) {
            return serviceRepository.findByHopital(hopital.get());
        }
        throw new RuntimeException("Hôpital non trouvé");
    }

    /**
     * Récupérer un service par ID
     */
    public Optional<ServiceMedical> getServiceById(Integer serviceId) {
        return serviceRepository.findById(serviceId);
    }

    /**
     * Créer un nouveau service
     */
    @Transactional("transactionManager")
    public ServiceMedical createService(ServiceMedical service, Integer hopitalId) {
        Optional<HopitalStructureSoin> hopital = hopitalRepository.findById(hopitalId);
        if (hopital.isPresent()) {
            service.setHopital(hopital.get());
            service.setIdHopital(hopitalId);
            if (service.getIdentifiantS() == null) {
                Integer nextId = serviceRepository.findAll().stream()
                    .map(ServiceMedical::getIdentifiantS)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .map(id -> id + 1)
                    .orElse(1);
                service.setIdentifiantS(nextId);
            }
            return serviceRepository.save(service);
        }
        throw new RuntimeException("Hôpital non trouvé");
    }

    /**
     * Mettre à jour un service
     */
    @Transactional("transactionManager")
    public ServiceMedical updateService(Integer serviceId, ServiceMedical serviceDetails) {
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);
        if (service.isPresent()) {
            ServiceMedical s = service.get();
            if (serviceDetails.getLibelleS() != null) s.setLibelleS(serviceDetails.getLibelleS());
            if (serviceDetails.getNbLitsS() != null) s.setNbLitsS(serviceDetails.getNbLitsS());
            if (serviceDetails.getNbChambresS() != null) s.setNbChambresS(serviceDetails.getNbChambresS());
            if (serviceDetails.getNbMedecinsS() != null) s.setNbMedecinsS(serviceDetails.getNbMedecinsS());
            return serviceRepository.save(s);
        }
        throw new RuntimeException("Service non trouvé");
    }

    /**
     * Supprimer un service (avec nettoyage des relations)
     */
    @Transactional("transactionManager")
    public void deleteService(Integer serviceId) {
        ServiceMedical service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new RuntimeException("Service non trouvé"));

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

    /**
     * Récupérer les utilisateurs d'un service
     */
    public List<User> getUsersByService(Integer serviceId) {
        Optional<ServiceMedical> service = serviceRepository.findById(serviceId);
        if (service.isPresent()) {
            return userRepository.findByServices(service.get());
        }
        throw new RuntimeException("Service non trouvé");
    }
}

