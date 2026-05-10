package com.medical.platform.controllers;

import com.medical.platform.entities.Transplantation;
import com.medical.platform.entities.*;
import com.medical.platform.repositories.TransplantationRepository;
import com.medical.platform.repositories.*;
import com.medical.platform.graph.SyncService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/transplantations")
@PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI')")
public class TransplantationController {

    @Autowired private TransplantationRepository repo;
    @Autowired private PatientIdAdminRepository patients;
    @Autowired private DonneurRepository donneurs;
    @Autowired private GreffeRepository greffes;
    @Autowired private NephropathieInitialeRepository nephropathies;
    @Autowired private DialyseRepository dialyses;
    @Autowired private ComorbiditeRepository comorbidites;
    @Autowired private AntecedentMedicalRepository antecedentsMedicaux;
    @Autowired private AntecedentChirurgicalRepository antecedentsChirurgicaux;
    @Autowired private BilanPreGreffeRepository bilansPreGreffe;
    @Autowired private BilanGreffeRepository bilansGreffe;
    @Autowired private TraitementImmunosuppresseurRepository traitements;
    @Autowired private MedicamentRepository medicaments;
    @Autowired private EffetSecondaireRepository effetsSecondaires;
    @Autowired private HospitalisationPostTransplantationRepository hospitalisations;
    @Autowired private EvolutionRenaleRepository evolutionsRenales;
    @Autowired private SyncService syncService;

    @GetMapping
    public List<Transplantation> getAll() {
        return repo.findAll();
    }

    @GetMapping("/patient/{patientId}")
    public List<Transplantation> getByPatient(@PathVariable Integer patientId) {
        return repo.findByPatientIdentifiantP(patientId);
    }

    @GetMapping("/historique/{patientId}")
    public List<Transplantation> getHistorique(@PathVariable Integer patientId) {
        return repo.findByPatientIdentifiantP(patientId);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Transplantation> create(@RequestBody Map<String, Object> body) {
        Transplantation saved = savePatientSheet(body, null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Transplantation> update(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(savePatientSheet(body, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Transplantation savePatientSheet(Map<String, Object> body, Integer existingId) {
        Integer patientId = intValue(nested(body, "patient", "identifiantP"));
        PatientIdAdmin patient = patientId == null
            ? null
            : patients.findById(patientId).orElseThrow(() -> new IllegalArgumentException("Patient introuvable: " + patientId));

        if (patient != null) {
            updatePatientSocialFields(patient, body);
        }

        Transplantation transplantation = existingId == null
            ? new Transplantation()
            : repo.findById(existingId).orElseGet(Transplantation::new);
        if (existingId != null) {
            transplantation.setNumeroTr(existingId);
        }
        transplantation.setPatient(patient);
        transplantation.setDateTr(dateValue(body.get("dateTr")));
        transplantation.setLieuTr(text(body.get("lieuTr")));
        transplantation.setLieuDeSuivi(text(body.get("lieuDeSuivi")));
        transplantation.setNbTransplantaion(text(body.get("nbTransplantaion")));
        transplantation.setNbUretre(text(body.get("nbUretre")));
        transplantation.setRein(text(body.get("rein")));
        transplantation.setNbArteresVeines(text(body.get("nbArteresVeines")));
        transplantation.setKystes(boolValue(body.get("kystes")));
        transplantation.setDureeDyschesie(text(body.get("ischemieFroideH")));
        transplantation.setDureeDyschesieChaude(text(body.get("ischemieChaudeMin")));
        transplantation.setLiquideDeConservation(text(body.get("liquideDeConservation")));
        transplantation.setLiquideDeRincage(text(body.get("liquideDeRincage")));
        transplantation.setMachineAPerfusion(boolValue(body.get("machineAPerfusion")));
        transplantation.setTypeAnastomoseArterielle(text(body.get("typeAnastomoseArterielle")));
        transplantation.setTypeAnastomoseVeineuse(text(body.get("typeAnastomoseVeineuse")));
        transplantation.setTypeAnastomoseUreteroVesicale(text(body.get("typeAnastomoseUreteroVesicale")));
        transplantation.setSondeEnDoubleJ(boolValue(body.get("sondeEnDoubleJ")));

        Transplantation saved = repo.save(transplantation);
        saveMedicalDetails(body, patient, saved);

        syncPatientGraphBestEffort(saved);
        return saved;
    }

    private void updatePatientSocialFields(PatientIdAdmin patient, Map<String, Object> body) {
        boolean changed = false;
        if (hasText(body.get("statutSocioEconomique"))) {
            patient.setStatut(text(body.get("statutSocioEconomique")));
            changed = true;
        }
        if (hasText(body.get("niveauEducation"))) {
            patient.setNiveauEducation(text(body.get("niveauEducation")));
            changed = true;
        }
        if (body.containsKey("enEtatActivite")) {
            patient.setEnEtatActivite(boolValue(body.get("enEtatActivite")));
            changed = true;
        }
        if (changed) {
            patients.save(patient);
        }
    }

    private void saveMedicalDetails(Map<String, Object> body, PatientIdAdmin patient, Transplantation transplantation) {
        NephropathieInitiale nephropathie = null;
        if (hasAny(body, "nephropathieInitiale", "etiologieIrc", "modaliteEer")) {
            nephropathie = new NephropathieInitiale();
            nephropathie.setTypeCliniqueNi(text(body.get("nephropathieInitiale")));
            nephropathie.setCauseNi(text(body.get("etiologieIrc")));
            nephropathie.setStadeMaladieNi(text(body.get("modaliteEer")));
            nephropathie = nephropathies.save(nephropathie);
        }

        if (hasAny(body, "diabetePreTr", "htaPreTr")) {
            Comorbidite comorbidite = new Comorbidite();
            comorbidite.setDiabete(boolValue(body.get("diabetePreTr")));
            comorbidite.setCardiaque(boolValue(body.get("htaPreTr")));
            comorbidites.save(comorbidite);
        }

        if (hasText(body.get("antecedentMedical"))) {
            AntecedentMedical antecedent = new AntecedentMedical();
            antecedent.setDescriptionAm(text(body.get("antecedentMedical")));
            antecedentsMedicaux.save(antecedent);
        }

        if (hasText(body.get("antecedentChirurgical"))) {
            AntecedentChirurgical antecedent = new AntecedentChirurgical();
            antecedent.setDescription(text(body.get("antecedentChirurgical")));
            antecedentsChirurgicaux.save(antecedent);
        }

        if (hasAny(body, "typeDialyse", "modaliteEer")) {
            Dialyse dialyse = new Dialyse();
            dialyse.setTypeDialyse(firstText(body.get("typeDialyse"), body.get("modaliteEer")));
            dialyses.save(dialyse);
        }

        if (hasAny(body, "typeDonneur", "sexeDonneur")) {
            Donneur donneur = new Donneur();
            donneur.setTypeDonneur(text(body.get("typeDonneur")));
            donneur.setSexeP(text(body.get("sexeDonneur")));
            donneurs.save(donneur);
        }

        Greffe greffe = null;
        if (hasAny(body, "dateGreffe", "descriptionGreffe")) {
            greffe = new Greffe();
            greffe.setDateG(dateValue(body.get("dateGreffe")));
            greffe.setDescriptionG(text(body.get("descriptionGreffe")));
            greffe = greffes.save(greffe);
        }

        if (nephropathie != null && hasText(body.get("bilanReceveur"))) {
            BilanPreGreffe bilan = new BilanPreGreffe();
            bilan.setNephropathieInitiale(nephropathie);
            bilan.setDateBilanB(dateValue(body.get("dateTr")));
            bilan.setDescriptionBilanB(text(body.get("bilanReceveur")));
            bilansPreGreffe.save(bilan);
        }

        if (greffe != null && hasAny(body, "bilanReceveur", "bilanDonneur")) {
            BilanGreffe bilanReceveur = new BilanGreffe();
            bilanReceveur.setGreffe(greffe);
            bilanReceveur.setDateBg(dateValue(body.get("dateTr")));
            bilanReceveur.setDescriptionBg(text(body.get("bilanReceveur")));
            bilanReceveur.setResultatBg(text(body.get("bilanDonneur")));
            bilansGreffe.save(bilanReceveur);
        }

        if (patient != null && hasAny(body, "dciTis", "dureeTraitementTis", "typeInduction", "tisEntretien")) {
            TraitementImmunosuppresseur traitement = new TraitementImmunosuppresseur();
            traitement.setPatient(patient);
            traitement.setDciTis(firstText(body.get("dciTis"), body.get("typeInduction"), body.get("tisEntretien")));
            traitement.setDureeTraitementTis(text(body.get("dureeTraitementTis")));
            traitements.save(traitement);
        }

        if (hasText(body.get("medicament"))) {
            Medicament medicament = new Medicament();
            medicament.setNomCommercialMed(text(body.get("medicament")));
            medicament.setDescriptionMed(text(body.get("medicament")));
            medicaments.save(medicament);
        }

        if (hasText(body.get("effetSecondaire"))) {
            EffetSecondaire effet = new EffetSecondaire();
            effet.setLibelleEfs(text(body.get("effetSecondaire")));
            effet.setDescriptionEfs(text(body.get("effetSecondaire")));
            effetsSecondaires.save(effet);
        }

        if (hasText(body.get("dateCreationEvolution"))) {
            EvolutionRenale evolution = new EvolutionRenale();
            evolution.setDateCreation(dateValue(body.get("dateCreationEvolution")));
            evolution.setNumeroTr(transplantation.getNumeroTr());
            evolution.setSeanceDeDialyse(boolValue(body.get("retourDialyse")));
            evolution.setDateSeance(dateValue(body.get("dateDernieresNvl")));
            evolution.setRepriseFonctionGreffon(boolValue(body.get("vivantGreffon")) ? "Oui" : null);
            evolution.setCauseDeRetardDeReprise(boolValue(body.get("retardRepriseFct")) ? "Retard reprise fonction" : null);
            evolutionsRenales.save(evolution);
        }

        if (hasAny(body, "dateEntreeHospitalisation", "dateSortieHospitalisation", "typeHospitalisation", "rejetAigu1ereAnnee")) {
            HospitalisationPostTransplantation hospitalisation = new HospitalisationPostTransplantation();
            hospitalisation.setNumeroTr(transplantation.getNumeroTr());
            hospitalisation.setDateEntree(dateValue(body.get("dateEntreeHospitalisation")));
            hospitalisation.setDateSortie(dateValue(body.get("dateSortieHospitalisation")));
            hospitalisation.setTypeHospitalisation(text(body.get("typeHospitalisation")));
            hospitalisation.setRejetAiguCellulaire(boolValue(body.get("rejetAigu1ereAnnee")) ? "Oui" : null);
            hospitalisations.save(hospitalisation);
        }
    }

    @SuppressWarnings("unchecked")
    private Object nested(Map<String, Object> body, String parent, String child) {
        Object value = body.get(parent);
        if (value instanceof Map<?, ?> map) {
            return ((Map<String, Object>) map).get(child);
        }
        return null;
    }

    private String text(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String firstText(Object... values) {
        for (Object value : values) {
            String text = text(value);
            if (text != null) return text;
        }
        return null;
    }

    private boolean hasText(Object value) {
        return text(value) != null;
    }

    private boolean hasAny(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value instanceof Boolean) return true;
            if (hasText(value)) return true;
        }
        return false;
    }

    private Boolean boolValue(Object value) {
        if (value instanceof Boolean bool) return bool;
        String text = text(value);
        return text == null ? null : Boolean.valueOf(text);
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) return number.intValue();
        String text = text(value);
        if (text == null) return null;
        return Integer.valueOf(text);
    }

    private LocalDate dateValue(Object value) {
        String text = text(value);
        if (text == null) return null;
        return LocalDate.parse(text.substring(0, Math.min(10, text.length())));
    }

    private void syncPatientGraphBestEffort(Transplantation saved) {
        if (saved.getPatient() == null) {
            return;
        }
        try {
            syncService.syncPatientById(saved.getPatient().getIdentifiantP());
        } catch (Exception exception) {
            System.err.println("Synchronisation Neo4j ignoree: " + exception.getMessage());
        }
    }
}
