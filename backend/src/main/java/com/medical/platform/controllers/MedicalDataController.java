package com.medical.platform.controllers;

import com.medical.platform.entities.*;
import com.medical.platform.repositories.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Domaines :
 *   IMMUNO  = ADMIN | MEDECIN | MEDECIN_SUIVI | AGENT_IMMUNO
 *   LAB     = ADMIN | MEDECIN | MEDECIN_SUIVI | AGENT_LABORATOIRE
 *   SHARED  = ADMIN | MEDECIN | MEDECIN_SUIVI | AGENT_LABORATOIRE | AGENT_IMMUNO
 */
@RestController
@RequestMapping("/api/medical")
@Transactional("transactionManager")
public class MedicalDataController {

    // ── SpEL constants (repeated in annotations) ─────────────────────────────
    private static final String IMMUNO = "hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')";
    private static final String LAB    = "hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')";
    private static final String SHARED = "hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')";

    private final DonneurRepository donneurs;
    private final GreffeRepository greffes;
    private final NephropathieInitialeRepository nephropathies;
    private final DialyseRepository dialyses;
    private final ComorbiditeRepository comorbidites;
    private final AntecedentMedicalRepository antecedentsMedicaux;
    private final AntecedentChirurgicalRepository antecedentsChirurgicaux;
    private final BilanPreGreffeRepository bilansPreGreffe;
    private final BilanGreffeRepository bilansGreffe;
    private final TraitementImmunosuppresseurRepository traitements;
    private final MedicamentRepository medicaments;
    private final EffetSecondaireRepository effetsSecondaires;
    private final HospitalisationPostTransplantationRepository hospitalisations;
    private final EvolutionRenaleRepository evolutionsRenales;

    public MedicalDataController(
        DonneurRepository donneurs, GreffeRepository greffes,
        NephropathieInitialeRepository nephropathies, DialyseRepository dialyses,
        ComorbiditeRepository comorbidites,
        AntecedentMedicalRepository antecedentsMedicaux,
        AntecedentChirurgicalRepository antecedentsChirurgicaux,
        BilanPreGreffeRepository bilansPreGreffe, BilanGreffeRepository bilansGreffe,
        TraitementImmunosuppresseurRepository traitements,
        MedicamentRepository medicaments, EffetSecondaireRepository effetsSecondaires,
        HospitalisationPostTransplantationRepository hospitalisations,
        EvolutionRenaleRepository evolutionsRenales
    ) {
        this.donneurs = donneurs; this.greffes = greffes;
        this.nephropathies = nephropathies; this.dialyses = dialyses;
        this.comorbidites = comorbidites;
        this.antecedentsMedicaux = antecedentsMedicaux;
        this.antecedentsChirurgicaux = antecedentsChirurgicaux;
        this.bilansPreGreffe = bilansPreGreffe; this.bilansGreffe = bilansGreffe;
        this.traitements = traitements;
        this.medicaments = medicaments; this.effetsSecondaires = effetsSecondaires;
        this.hospitalisations = hospitalisations; this.evolutionsRenales = evolutionsRenales;
    }

    // ── IMMUNO : Donneurs ────────────────────────────────────────────────────
    @GetMapping("/donneurs")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<Donneur> getDonneurs() { return donneurs.findAll(); }

    @PostMapping("/donneurs")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Donneur saveDonneur(@RequestBody Donneur body) { return donneurs.save(body); }

    @PutMapping("/donneurs/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Donneur updateDonneur(@PathVariable Integer id, @RequestBody Donneur body) {
        body.setIdentifiantP2(id); return donneurs.save(body);
    }

    @DeleteMapping("/donneurs/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteDonneur(@PathVariable Integer id) { return deleteById(donneurs, id); }

    // ── IMMUNO : Greffes ─────────────────────────────────────────────────────
    @GetMapping("/greffes")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<Greffe> getGreffes() { return greffes.findAll(); }

    @PostMapping("/greffes")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Greffe saveGreffe(@RequestBody Greffe body) { return greffes.save(body); }

    @PutMapping("/greffes/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Greffe updateGreffe(@PathVariable Integer id, @RequestBody Greffe body) {
        body.setIdentifiantG(id); return greffes.save(body);
    }

    @DeleteMapping("/greffes/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteGreffe(@PathVariable Integer id) { return deleteById(greffes, id); }

    // ── IMMUNO : Néphropathies ───────────────────────────────────────────────
    @GetMapping("/nephropathies")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<NephropathieInitiale> getNephropathies() { return nephropathies.findAll(); }

    @PostMapping("/nephropathies")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public NephropathieInitiale saveNephropathie(@RequestBody NephropathieInitiale body) { return nephropathies.save(body); }

    @PutMapping("/nephropathies/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public NephropathieInitiale updateNephropathie(@PathVariable Integer id, @RequestBody NephropathieInitiale body) {
        body.setIdentifiantNi(id); return nephropathies.save(body);
    }

    @DeleteMapping("/nephropathies/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteNephropathie(@PathVariable Integer id) { return deleteById(nephropathies, id); }

    // ── LAB : Dialyses ───────────────────────────────────────────────────────
    @GetMapping("/dialyses")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public List<Dialyse> getDialyses() { return dialyses.findAll(); }

    @PostMapping("/dialyses")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public Dialyse saveDialyse(@RequestBody Dialyse body) { return dialyses.save(body); }

    @PutMapping("/dialyses/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public Dialyse updateDialyse(@PathVariable Integer id, @RequestBody Dialyse body) {
        body.setIdentifiantD(id); return dialyses.save(body);
    }

    @DeleteMapping("/dialyses/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public ResponseEntity<Void> deleteDialyse(@PathVariable Integer id) { return deleteById(dialyses, id); }

    // ── IMMUNO : Comorbidités ────────────────────────────────────────────────
    @GetMapping("/comorbidites")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<Comorbidite> getComorbidites() { return comorbidites.findAll(); }

    @PostMapping("/comorbidites")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Comorbidite saveComorbidite(@RequestBody Comorbidite body) { return comorbidites.save(body); }

    @PutMapping("/comorbidites/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public Comorbidite updateComorbidite(@PathVariable Integer id, @RequestBody Comorbidite body) {
        body.setIdentifiantC(id); return comorbidites.save(body);
    }

    @DeleteMapping("/comorbidites/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteComorbidite(@PathVariable Integer id) { return deleteById(comorbidites, id); }

    // ── IMMUNO : Antécédents médicaux ────────────────────────────────────────
    @GetMapping("/antecedents-medicaux")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<AntecedentMedical> getAntecedentsMedicaux() { return antecedentsMedicaux.findAll(); }

    @PostMapping("/antecedents-medicaux")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public AntecedentMedical saveAntecedentMedical(@RequestBody AntecedentMedical body) { return antecedentsMedicaux.save(body); }

    @PutMapping("/antecedents-medicaux/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public AntecedentMedical updateAntecedentMedical(@PathVariable Integer id, @RequestBody AntecedentMedical body) {
        body.setIdentifiantAm(id); return antecedentsMedicaux.save(body);
    }

    @DeleteMapping("/antecedents-medicaux/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteAntecedentMedical(@PathVariable Integer id) { return deleteById(antecedentsMedicaux, id); }

    // ── IMMUNO : Antécédents chirurgicaux ────────────────────────────────────
    @GetMapping("/antecedents-chirurgicaux")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<AntecedentChirurgical> getAntecedentsChirurgicaux() { return antecedentsChirurgicaux.findAll(); }

    @PostMapping("/antecedents-chirurgicaux")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public AntecedentChirurgical saveAntecedentChirurgical(@RequestBody AntecedentChirurgical body) { return antecedentsChirurgicaux.save(body); }

    @PutMapping("/antecedents-chirurgicaux/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public AntecedentChirurgical updateAntecedentChirurgical(@PathVariable Integer id, @RequestBody AntecedentChirurgical body) {
        body.setIdentifiantAc(id); return antecedentsChirurgicaux.save(body);
    }

    @DeleteMapping("/antecedents-chirurgicaux/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteAntecedentChirurgical(@PathVariable Integer id) { return deleteById(antecedentsChirurgicaux, id); }

    // ── LAB : Bilans pré-greffe ──────────────────────────────────────────────
    @GetMapping("/bilans-pre-greffe")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public List<BilanPreGreffe> getBilansPreGreffe() { return bilansPreGreffe.findAll(); }

    @PostMapping("/bilans-pre-greffe")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public BilanPreGreffe saveBilanPreGreffe(@RequestBody BilanPreGreffe body) { return bilansPreGreffe.save(body); }

    @PutMapping("/bilans-pre-greffe/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public BilanPreGreffe updateBilanPreGreffe(@PathVariable Integer id, @RequestBody BilanPreGreffe body) {
        body.setIdentifiantB(id); return bilansPreGreffe.save(body);
    }

    @DeleteMapping("/bilans-pre-greffe/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public ResponseEntity<Void> deleteBilanPreGreffe(@PathVariable Integer id) { return deleteById(bilansPreGreffe, id); }

    // ── LAB : Bilans greffe ──────────────────────────────────────────────────
    @GetMapping("/bilans-greffe")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public List<BilanGreffe> getBilansGreffe() { return bilansGreffe.findAll(); }

    @PostMapping("/bilans-greffe")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public BilanGreffe saveBilanGreffe(@RequestBody BilanGreffe body) { return bilansGreffe.save(body); }

    @PutMapping("/bilans-greffe/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public BilanGreffe updateBilanGreffe(@PathVariable Integer id, @RequestBody BilanGreffe body) {
        body.setIdentifiantBg(id); return bilansGreffe.save(body);
    }

    @DeleteMapping("/bilans-greffe/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public ResponseEntity<Void> deleteBilanGreffe(@PathVariable Integer id) { return deleteById(bilansGreffe, id); }

    // ── IMMUNO : Traitements immunosuppresseurs ──────────────────────────────
    @GetMapping("/traitements")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public List<TraitementImmunosuppresseur> getTraitements(@RequestParam(required = false) Integer patientId) {
        return patientId == null ? traitements.findAll() : traitements.findByPatientIdentifiantP(patientId);
    }

    @PostMapping("/traitements")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public TraitementImmunosuppresseur saveTraitement(@RequestBody TraitementImmunosuppresseur body) {
        return traitements.save(body);
    }

    @PutMapping("/traitements/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public TraitementImmunosuppresseur updateTraitement(@PathVariable Integer id, @RequestBody TraitementImmunosuppresseur body) {
        body.setIdentifiantTis(id); return traitements.save(body);
    }

    @DeleteMapping("/traitements/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteTraitement(@PathVariable Integer id) { return deleteById(traitements, id); }

    // ── SHARED : Médicaments ─────────────────────────────────────────────────
    @GetMapping("/medicaments")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public List<Medicament> getMedicaments() { return medicaments.findAll(); }

    @PostMapping("/medicaments")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public Medicament saveMedicament(@RequestBody Medicament body) { return medicaments.save(body); }

    @PutMapping("/medicaments/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public Medicament updateMedicament(@PathVariable Integer id, @RequestBody Medicament body) {
        body.setIdentifiantMed(id); return medicaments.save(body);
    }

    @DeleteMapping("/medicaments/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteMedicament(@PathVariable Integer id) { return deleteById(medicaments, id); }

    // ── SHARED : Effets secondaires ──────────────────────────────────────────
    @GetMapping("/effets-secondaires")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public List<EffetSecondaire> getEffetsSecondaires() { return effetsSecondaires.findAll(); }

    @PostMapping("/effets-secondaires")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public EffetSecondaire saveEffetSecondaire(@RequestBody EffetSecondaire body) { return effetsSecondaires.save(body); }

    @PutMapping("/effets-secondaires/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public EffetSecondaire updateEffetSecondaire(@PathVariable Integer id, @RequestBody EffetSecondaire body) {
        body.setIdentifiantEfs(id); return effetsSecondaires.save(body);
    }

    @DeleteMapping("/effets-secondaires/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE','AGENT_IMMUNO')")
    public ResponseEntity<Void> deleteEffetSecondaire(@PathVariable Integer id) { return deleteById(effetsSecondaires, id); }

    // ── LAB : Hospitalisations ───────────────────────────────────────────────
    @GetMapping("/hospitalisations")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public List<HospitalisationPostTransplantation> getHospitalisations() { return hospitalisations.findAll(); }

    @PostMapping("/hospitalisations")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public HospitalisationPostTransplantation saveHospitalisation(@RequestBody HospitalisationPostTransplantation body) {
        return hospitalisations.save(body);
    }

    @PutMapping("/hospitalisations/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public HospitalisationPostTransplantation updateHospitalisation(@PathVariable Integer id, @RequestBody HospitalisationPostTransplantation body) {
        body.setNumeroTr(id); return hospitalisations.save(body);
    }

    @DeleteMapping("/hospitalisations/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public ResponseEntity<Void> deleteHospitalisation(@PathVariable Integer id) { return deleteById(hospitalisations, id); }

    // ── LAB : Évolutions rénales ─────────────────────────────────────────────
    @GetMapping("/evolutions-renales")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public List<EvolutionRenale> getEvolutionsRenales(@RequestParam(required = false) Integer numeroTr) {
        return numeroTr == null ? evolutionsRenales.findAll() : evolutionsRenales.findByNumeroTr(numeroTr);
    }

    @PostMapping("/evolutions-renales")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public EvolutionRenale saveEvolutionRenale(@RequestBody EvolutionRenale body) { return evolutionsRenales.save(body); }

    @PutMapping("/evolutions-renales/{dateCreation}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public EvolutionRenale updateEvolutionRenale(@PathVariable String dateCreation, @RequestBody EvolutionRenale body) {
        body.setDateCreation(LocalDate.parse(dateCreation)); return evolutionsRenales.save(body);
    }

    @DeleteMapping("/evolutions-renales/{dateCreation}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MEDECIN','MEDECIN_SUIVI','AGENT_LABORATOIRE')")
    public ResponseEntity<Void> deleteEvolutionRenale(@PathVariable String dateCreation) {
        return deleteById(evolutionsRenales, LocalDate.parse(dateCreation));
    }

    // ── util ─────────────────────────────────────────────────────────────────
    private <T, ID> ResponseEntity<Void> deleteById(JpaRepository<T, ID> repository, ID id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
