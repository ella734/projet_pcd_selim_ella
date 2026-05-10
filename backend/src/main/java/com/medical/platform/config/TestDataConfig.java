package com.medical.platform.config;

import com.medical.platform.entities.*;
import com.medical.platform.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Date;

@Configuration
@ConditionalOnProperty(name = "app.test-data.enabled", havingValue = "true")
public class TestDataConfig {

    @Bean
    public CommandLineRunner seedTestData(
        HopitalStructureSoinRepository hopitalRepo,
        ServiceRepository serviceRepo,
        MedecinRepository medecinRepo,
        PatientIdAdminRepository patientRepo,
        DonneurRepository donneurRepo,
        TransplantationRepository transplantationRepo,
        NephropathieInitialeRepository nephropathieRepo,
        GreffeRepository greffeRepo,
        BilanPreGreffeRepository bilanPreGreffeRepo,
        BilanGreffeRepository bilanGreffeRepo,
        TraitementImmunosuppresseurRepository tisRepo,
        MedicamentRepository medicamentRepo,
        EffetSecondaireRepository effetSecondaireRepo,
        HospitalisationPostTransplantationRepository hospitalisationRepo,
        EvolutionRenaleRepository evolutionRenaleRepo,
        ComorbiditeRepository comorbiditeRepo,
        DialyseRepository dialyseRepo,
        AntecedentMedicalRepository antecedentMedRepo,
        AntecedentChirurgicalRepository antecedentChirRepo
    ) {
        return args -> {
            if (medecinRepo.count() > 0) {
                System.out.println("[TestData] Data already present, skipping seed.");
                return;
            }

            // ── Hôpitaux (@GeneratedValue → pas d'ID manuel) ──────────
            HopitalStructureSoin h1 = new HopitalStructureSoin();
            h1.setLibelleH("CHU de Tunis");
            h1.setAdresseH("1 Av. Jebal Lakhdar, Tunis 1007");
            h1.setNbBlocH(8);
            h1.setNbServiceH(12);
            h1.setNbLitsH(450);
            h1.setDescriptionH("Centre Hospitalo-Universitaire principal");
            h1.setDateCreationH(new Date());
            HopitalStructureSoin savedH1 = hopitalRepo.save(h1);

            HopitalStructureSoin h2 = new HopitalStructureSoin();
            h2.setLibelleH("Hôpital Charles Nicolle");
            h2.setAdresseH("Bab Saadoun, Tunis 1006");
            h2.setNbBlocH(5);
            h2.setNbServiceH(8);
            h2.setNbLitsH(300);
            h2.setDescriptionH("Hôpital universitaire spécialisé");
            h2.setDateCreationH(new Date());
            HopitalStructureSoin savedH2 = hopitalRepo.save(h2);

            // ── Services (pas de @GeneratedValue → ID manuel à partir de 2) ──
            ServiceMedical s1 = new ServiceMedical();
            s1.setIdentifiantS(2);
            s1.setLibelleS("Service de Néphrologie");
            s1.setNbLitsS(40);
            s1.setNbChambresS(10);
            s1.setNbMedecinsS(5);
            s1.setHopital(savedH1);
            ServiceMedical savedS1 = serviceRepo.save(s1);

            ServiceMedical s2 = new ServiceMedical();
            s2.setIdentifiantS(3);
            s2.setLibelleS("Service de Transplantation");
            s2.setNbLitsS(20);
            s2.setNbChambresS(8);
            s2.setNbMedecinsS(4);
            s2.setHopital(savedH1);
            ServiceMedical savedS2 = serviceRepo.save(s2);

            ServiceMedical s3 = new ServiceMedical();
            s3.setIdentifiantS(4);
            s3.setLibelleS("Service d'Urologie");
            s3.setNbLitsS(30);
            s3.setNbChambresS(10);
            s3.setNbMedecinsS(6);
            s3.setHopital(savedH2);
            ServiceMedical savedS3 = serviceRepo.save(s3);

            // ── Médecins (@GeneratedValue → pas d'ID manuel) ──────────
            Medecin m1 = new Medecin();
            m1.setNomM("Ben Ali");
            m1.setPrenomM("Ahmed");
            m1.setSexeM("M");
            m1.setSpecialiteM("Néphrologie");
            m1.setNumTelM("+216 71 234 567");
            m1.setNumTelWhatsAppM("+216 22 234 567");
            m1.setAdresseDomM("12 Rue de la Liberté, Tunis");
            m1.setService(savedS1);
            Medecin savedM1 = medecinRepo.save(m1);

            Medecin m2 = new Medecin();
            m2.setNomM("Mansour");
            m2.setPrenomM("Sonia");
            m2.setSexeM("F");
            m2.setSpecialiteM("Transplantation rénale");
            m2.setNumTelM("+216 71 345 678");
            m2.setNumTelWhatsAppM("+216 22 345 678");
            m2.setAdresseDomM("45 Avenue Bourguiba, Tunis");
            m2.setService(savedS2);
            Medecin savedM2 = medecinRepo.save(m2);

            Medecin m3 = new Medecin();
            m3.setNomM("Trabelsi");
            m3.setPrenomM("Karim");
            m3.setSexeM("M");
            m3.setSpecialiteM("Urologie");
            m3.setNumTelM("+216 71 456 789");
            m3.setNumTelWhatsAppM("+216 22 456 789");
            m3.setAdresseDomM("7 Rue Ibn Khaldoun, Tunis");
            m3.setService(savedS3);
            Medecin savedM3 = medecinRepo.save(m3);

            // ── Patients (@GeneratedValue → pas d'ID manuel) ──────────
            PatientIdAdmin p1 = new PatientIdAdmin();
            p1.setNomP("Khalil");
            p1.setPrenomP("Mohamed");
            p1.setNationaliteP("Tunisienne");
            p1.setSexeP("M");
            p1.setOrigineGeogP("Tunis");
            p1.setAdresseP("23 Rue Habib Thameur, Tunis");
            p1.setTelephoneP("+216 55 123 456");
            p1.setAdressEmailP("m.khalil@email.tn");
            p1.setAdulteP(true);
            p1.setStatut("Transplanté");
            p1.setEvolution("Stable");
            p1.setNiveauEducation("Universitaire");
            p1.setEnEtatActivite(true);
            p1.setMedecinInvestigateur(savedM1);
            PatientIdAdmin savedP1 = patientRepo.save(p1);

            PatientIdAdmin p2 = new PatientIdAdmin();
            p2.setNomP("Haddad");
            p2.setPrenomP("Fatma");
            p2.setNationaliteP("Tunisienne");
            p2.setSexeP("F");
            p2.setOrigineGeogP("Sfax");
            p2.setAdresseP("8 Rue de la République, Sfax");
            p2.setTelephoneP("+216 55 234 567");
            p2.setAdressEmailP("f.haddad@email.tn");
            p2.setAdulteP(true);
            p2.setStatut("En attente");
            p2.setEvolution("Stable");
            p2.setNiveauEducation("Secondaire");
            p2.setEnEtatActivite(false);
            p2.setMedecinInvestigateur(savedM2);
            patientRepo.save(p2);

            PatientIdAdmin p3 = new PatientIdAdmin();
            p3.setNomP("Ben Salem");
            p3.setPrenomP("Ali");
            p3.setNationaliteP("Tunisienne");
            p3.setSexeP("M");
            p3.setOrigineGeogP("Sousse");
            p3.setAdresseP("15 Avenue Mohamed V, Sousse");
            p3.setTelephoneP("+216 55 345 678");
            p3.setAdressEmailP("a.bensalem@email.tn");
            p3.setAdulteP(true);
            p3.setStatut("Transplanté");
            p3.setEvolution("Amélioration");
            p3.setNiveauEducation("Primaire");
            p3.setEnEtatActivite(true);
            p3.setMedecinInvestigateur(savedM1);
            PatientIdAdmin savedP3 = patientRepo.save(p3);

            PatientIdAdmin p4 = new PatientIdAdmin();
            p4.setNomP("Bouaziz");
            p4.setPrenomP("Sara");
            p4.setNationaliteP("Tunisienne");
            p4.setSexeP("F");
            p4.setOrigineGeogP("Bizerte");
            p4.setAdresseP("3 Rue de la Corniche, Bizerte");
            p4.setTelephoneP("+216 55 456 789");
            p4.setAdressEmailP("s.bouaziz@email.tn");
            p4.setAdulteP(true);
            p4.setStatut("Dialysé");
            p4.setEvolution("Stable");
            p4.setNiveauEducation("Universitaire");
            p4.setEnEtatActivite(false);
            p4.setMedecinInvestigateur(savedM3);
            patientRepo.save(p4);

            // ── Donneurs (@GeneratedValue → pas d'ID manuel) ──────────
            Donneur d1 = new Donneur();
            d1.setNomP("Ridha");
            d1.setPrenomP("Youssef");
            d1.setNationaliteP("Tunisienne");
            d1.setSexeP("M");
            d1.setOrigineGeogP("Tunis");
            d1.setAdresseDomP("10 Rue du Lac, Tunis");
            d1.setTelephoneP("+216 55 567 890");
            d1.setAdresseEmailP("y.ridha@email.tn");
            d1.setAdulteP(true);
            d1.setTypeDonneur("Vivant");
            d1.setStatut("Actif");
            d1.setEvolution("Bon état général");
            donneurRepo.save(d1);

            Donneur d2 = new Donneur();
            d2.setNomP("Dridi");
            d2.setPrenomP("Amina");
            d2.setNationaliteP("Tunisienne");
            d2.setSexeP("F");
            d2.setOrigineGeogP("Nabeul");
            d2.setAdresseDomP("22 Avenue de la Plage, Nabeul");
            d2.setTelephoneP("+216 55 678 901");
            d2.setAdulteP(true);
            d2.setTypeDonneur("Décédé");
            d2.setStatut("Décédé");
            donneurRepo.save(d2);

            // ── Néphropathies initiales (@GeneratedValue) ──────────────
            NephropathieInitiale ni1 = new NephropathieInitiale();
            ni1.setTypeCliniqueNi("Syndrome néphrotique");
            ni1.setCauseNi("Glomérulonéphrite focale segmentaire");
            ni1.setTypeHistologiqueNi("HSF");
            ni1.setStadeMaladieNi("Stade 5");
            NephropathieInitiale savedNi1 = nephropathieRepo.save(ni1);

            NephropathieInitiale ni2 = new NephropathieInitiale();
            ni2.setTypeCliniqueNi("Néphropathie diabétique");
            ni2.setCauseNi("Diabète type 2 évoluant depuis 15 ans");
            ni2.setTypeHistologiqueNi("Kimmelstiel-Wilson");
            ni2.setStadeMaladieNi("Stade 4");
            NephropathieInitiale savedNi2 = nephropathieRepo.save(ni2);

            // ── Greffes (@GeneratedValue) ──────────────────────────────
            Greffe g1 = new Greffe();
            g1.setDateG(LocalDate.of(2022, 3, 15));
            g1.setDescriptionG("Greffe rénale de donneur vivant apparenté");
            g1.setAutresObservationsG("Bonne reprise fonctionnelle immédiate à J1");
            Greffe savedG1 = greffeRepo.save(g1);

            Greffe g2 = new Greffe();
            g2.setDateG(LocalDate.of(2023, 7, 20));
            g2.setDescriptionG("Greffe rénale de donneur décédé");
            g2.setAutresObservationsG("Reprise fonctionnelle retardée observée à J5");
            Greffe savedG2 = greffeRepo.save(g2);

            // ── Bilans pré-greffe (@GeneratedValue) ───────────────────
            BilanPreGreffe bpg1 = new BilanPreGreffe();
            bpg1.setNephropathieInitiale(savedNi1);
            bpg1.setDateBilanB(LocalDate.of(2021, 11, 10));
            bpg1.setDescriptionBilanB("Bilan complet pré-greffe : cardio, hémato, immuno");
            bpg1.setResultatBilanB("Patient éligible à la transplantation rénale");
            bilanPreGreffeRepo.save(bpg1);

            BilanPreGreffe bpg2 = new BilanPreGreffe();
            bpg2.setNephropathieInitiale(savedNi2);
            bpg2.setDateBilanB(LocalDate.of(2023, 2, 5));
            bpg2.setDescriptionBilanB("Bilan pré-greffe avec évaluation cardiologique approfondie");
            bpg2.setResultatBilanB("Éligible avec réserves cardiaques — suivi cardiologique requis");
            bilanPreGreffeRepo.save(bpg2);

            // ── Bilans post-greffe (@GeneratedValue) ──────────────────
            BilanGreffe bg1 = new BilanGreffe();
            bg1.setGreffe(savedG1);
            bg1.setDateBg(LocalDate.of(2022, 6, 15));
            bg1.setDescriptionBg("Bilan de contrôle à 3 mois post-transplantation");
            bg1.setResultatBg("Fonction rénale stable — créatininémie 120 µmol/L");
            bilanGreffeRepo.save(bg1);

            BilanGreffe bg2 = new BilanGreffe();
            bg2.setGreffe(savedG2);
            bg2.setDateBg(LocalDate.of(2023, 10, 20));
            bg2.setDescriptionBg("Bilan de contrôle à 3 mois post-transplantation");
            bg2.setResultatBg("Légère dégradation — créatininémie 180 µmol/L");
            bilanGreffeRepo.save(bg2);

            // ── Transplantations (@GeneratedValue) ────────────────────
            Transplantation t1 = new Transplantation();
            t1.setPatient(savedP1);
            t1.setDateTr(LocalDate.of(2022, 3, 15));
            t1.setLieuDeLaGreffe("CHU de Tunis - Bloc opératoire B");
            t1.setLieuDeSuivi("Service de Néphrologie");
            t1.setNbTransplantaion("1ère transplantation");
            t1.setRein("Gauche");
            t1.setKystes(false);
            t1.setSondeEnDoubleJ(true);
            t1.setTypeAnastomoseArterielle("Termino-terminale");
            t1.setTypeAnastomoseVeineuse("Termino-latérale");
            t1.setLiquideDeConservation("Eurocollins");
            t1.setLiquideDeRincage("Sérum physiologique");
            t1.setMachineAPerfusion(false);
            Transplantation savedT1 = transplantationRepo.save(t1);

            Transplantation t2 = new Transplantation();
            t2.setPatient(savedP3);
            t2.setDateTr(LocalDate.of(2023, 7, 20));
            t2.setLieuDeLaGreffe("CHU de Tunis - Bloc opératoire A");
            t2.setLieuDeSuivi("Service de Transplantation");
            t2.setNbTransplantaion("1ère transplantation");
            t2.setRein("Droit");
            t2.setKystes(true);
            t2.setSondeEnDoubleJ(true);
            t2.setTypeAnastomoseArterielle("Termino-latérale");
            t2.setTypeAnastomoseVeineuse("Termino-latérale");
            t2.setLiquideDeConservation("HTK (Custodiol)");
            t2.setLiquideDeRincage("Sérum physiologique");
            t2.setMachineAPerfusion(true);
            Transplantation savedT2 = transplantationRepo.save(t2);

            // ── Traitements immunosuppresseurs (@GeneratedValue) ───────
            TraitementImmunosuppresseur tis1 = new TraitementImmunosuppresseur();
            tis1.setPatient(savedP1);
            tis1.setDciTis("Tacrolimus");
            tis1.setDureeTraitementTis("À vie");
            tisRepo.save(tis1);

            TraitementImmunosuppresseur tis2 = new TraitementImmunosuppresseur();
            tis2.setPatient(savedP3);
            tis2.setDciTis("Ciclosporine");
            tis2.setDureeTraitementTis("À vie");
            tisRepo.save(tis2);

            // ── Médicaments (@GeneratedValue) ──────────────────────────
            Medicament med1 = new Medicament();
            med1.setNomCommercialMed("Prograf");
            med1.setDescriptionMed("Inhibiteur de la calcineurine — immunosuppresseur de référence");
            med1.setTypeMed("Immunosuppresseur");
            med1.setPosologieMed("0.1 mg/kg/jour en 2 prises orales");
            medicamentRepo.save(med1);

            Medicament med2 = new Medicament();
            med2.setNomCommercialMed("CellCept");
            med2.setDescriptionMed("Acide mycophénolique — inhibiteur de la synthèse des purines");
            med2.setTypeMed("Immunosuppresseur");
            med2.setPosologieMed("750 mg deux fois par jour");
            medicamentRepo.save(med2);

            Medicament med3 = new Medicament();
            med3.setNomCommercialMed("Solupred");
            med3.setDescriptionMed("Prednisolone — corticoïde anti-inflammatoire");
            med3.setTypeMed("Corticoïde");
            med3.setPosologieMed("5 mg/jour en maintenance");
            medicamentRepo.save(med3);

            // ── Effets secondaires (@GeneratedValue) ───────────────────
            EffetSecondaire es1 = new EffetSecondaire();
            es1.setLibelleEfs("Néphrotoxicité");
            es1.setDescriptionEfs("Toxicité rénale induite par le Tacrolimus");
            es1.setRecommendationEfs("Réduire la dose, surveiller la créatininémie hebdomadairement");
            effetSecondaireRepo.save(es1);

            EffetSecondaire es2 = new EffetSecondaire();
            es2.setLibelleEfs("Diabète post-transplantation");
            es2.setDescriptionEfs("Hyperglycémie induite par les immunosuppresseurs");
            es2.setRecommendationEfs("Contrôle glycémique strict, adapter le traitement corticoïde");
            effetSecondaireRepo.save(es2);

            // ── Hospitalisations (PK manuelle = numeroTr de la transplantation) ──
            HospitalisationPostTransplantation hosp1 = new HospitalisationPostTransplantation();
            hosp1.setNumeroTr(savedT1.getNumeroTr());
            hosp1.setDateEntree(LocalDate.of(2022, 4, 10));
            hosp1.setDateSortie(LocalDate.of(2022, 4, 17));
            hosp1.setTypeHospitalisation("Rejet aigu cellulaire");
            hosp1.setRejetAiguCellulaire("Oui — grade IIA");
            hosp1.setRejetAiguHumorale("Non");
            hosp1.setRejetChroniqueCellulaire("Non");
            hosp1.setThromboseArterielle("Non");
            hosp1.setThromboseVeineuse("Non");
            hospitalisationRepo.save(hosp1);

            HospitalisationPostTransplantation hosp2 = new HospitalisationPostTransplantation();
            hosp2.setNumeroTr(savedT2.getNumeroTr());
            hosp2.setDateEntree(LocalDate.of(2023, 9, 5));
            hosp2.setDateSortie(LocalDate.of(2023, 9, 12));
            hosp2.setTypeHospitalisation("Infection urinaire");
            hosp2.setRejetAiguCellulaire("Non");
            hosp2.setRejetAiguHumorale("Non");
            hosp2.setFistuleUrinaire("Non");
            hosp2.setThromboseArterielle("Non");
            hosp2.setThromboseVeineuse("Non");
            hospitalisationRepo.save(hosp2);

            // ── Évolution rénale (PK = LocalDate) ─────────────────────
            EvolutionRenale er1 = new EvolutionRenale();
            er1.setDateCreation(LocalDate.of(2022, 6, 1));
            er1.setNumeroTr(savedT1.getNumeroTr());
            er1.setSeanceDeDialyse(false);
            er1.setRepriseFonctionGreffon("Immédiate");
            evolutionRenaleRepo.save(er1);

            EvolutionRenale er2 = new EvolutionRenale();
            er2.setDateCreation(LocalDate.of(2023, 8, 15));
            er2.setNumeroTr(savedT2.getNumeroTr());
            er2.setSeanceDeDialyse(true);
            er2.setDateSeance(LocalDate.of(2023, 8, 16));
            er2.setRepriseFonctionGreffon("Retardée");
            er2.setCauseDeRetardDeReprise("Nécrose tubulaire aiguë");
            evolutionRenaleRepo.save(er2);

            // ── Comorbidités (@GeneratedValue) ─────────────────────────
            Comorbidite c1 = new Comorbidite();
            c1.setDiabete(false);
            c1.setCardiaque(true);
            comorbiditeRepo.save(c1);

            Comorbidite c2 = new Comorbidite();
            c2.setDiabete(true);
            c2.setCardiaque(false);
            comorbiditeRepo.save(c2);

            // ── Dialyses (@GeneratedValue) ─────────────────────────────
            Dialyse dl1 = new Dialyse();
            dl1.setTypeDialyse("Hémodialyse");
            dialyseRepo.save(dl1);

            Dialyse dl2 = new Dialyse();
            dl2.setTypeDialyse("Dialyse péritonéale ambulatoire continue");
            dialyseRepo.save(dl2);

            // ── Antécédents médicaux (@GeneratedValue) ─────────────────
            AntecedentMedical am1 = new AntecedentMedical();
            am1.setDescriptionAm("Hypertension artérielle traitée par Amlodipine depuis 5 ans");
            antecedentMedRepo.save(am1);

            AntecedentMedical am2 = new AntecedentMedical();
            am2.setDescriptionAm("Diabète de type 2 sous insulinothérapie depuis 8 ans");
            antecedentMedRepo.save(am2);

            // ── Antécédents chirurgicaux (@GeneratedValue) ─────────────
            AntecedentChirurgical ac1 = new AntecedentChirurgical();
            ac1.setDescription("Appendicectomie à l'âge de 20 ans (2005) sans complication");
            antecedentChirRepo.save(ac1);

            AntecedentChirurgical ac2 = new AntecedentChirurgical();
            ac2.setDescription("Cholécystectomie laparoscopique en 2018 pour lithiase biliaire");
            antecedentChirRepo.save(ac2);

            System.out.println("[TestData] ✓ Données insérées : 2 hôpitaux, 3 services, 3 médecins, " +
                "4 patients, 2 donneurs, 2 transplantations, et toutes les entités cliniques associées.");
        };
    }
}
