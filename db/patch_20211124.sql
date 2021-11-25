DROP VIEW public.v_examens;

CREATE OR REPLACE VIEW public.v_examens
 AS
 SELECT exam.id,
    exam.libelle,
    exam.resultat,
    exam.observation,
    exam.is_deleted,
    exam.when_done,
    exam.consultation,
    exam.who_done,
    cons.partenaire_id,
    cons.nom_partenaire,
    cons.taux_couverture,
    exam.cout_examen,
    exam.cout_examen::double precision * cons.taux_couverture AS montant_pris_en_charge,
    exam.cout_examen::double precision - exam.cout_examen::double precision * cons.taux_couverture AS montant_net_a_payer,
    cons.numero_consul,
    cons.medecin_traitrant AS medecin,
    cons.age,
    cons.id_patient,
    cons.nom_patient,
    cons.tel_patient,
    cons.when_done AS date_consultation
   FROM examens exam,
    v_consultations cons
  WHERE cons.id = exam.consultation AND exam.is_deleted = false;
  
 DROP TABLE IF EXISTS public.infos_medicale;

CREATE TABLE IF NOT EXISTS public.infos_medicale
(
    id bigserial NOT NULL ,
    libelle character varying(250) COLLATE pg_catalog."default" NOT NULL,
    is_deleted boolean DEFAULT false,
    is_ant boolean DEFAULT false,
    patient bigint NOT NULL,
    when_done timestamp without time zone NOT NULL,
    who_done character varying COLLATE pg_catalog."default",
    resultat character varying COLLATE pg_catalog."default",
    CONSTRAINT infos_medicale_pkey PRIMARY KEY (id),
    CONSTRAINT patient_fky FOREIGN KEY (patient)
        REFERENCES public.patients (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);



CREATE OR REPLACE VIEW public.vs_cons_medecin
 AS
 SELECT pat.id,
    cons.id AS ref_facture,
    pat.nom_p,
    pat.prenom_p,
    pat.categorie,
    pat.telephone,
    cons.montant_consultation AS montant,
    cons.when_done
   FROM personnels pat,
    v_consultations cons
  WHERE pat.id = cons.medecin_traitrant AND cons.montant_consultation > 0
  ORDER BY pat.id;
  
  
  CREATE OR REPLACE VIEW public.vs_exam_medecin
 AS
 SELECT concat('S', ss.consultation) AS ref_facture,
    pat.nom_p,
    pat.prenom_p,
    pat.categorie,
    pat.telephone,
    ss.total AS montant,
    ss.when_done
   FROM personnels pat,
    v_soins ss
  WHERE pat.id = ss.medecin AND ss.total > 0
  ORDER BY pat.id;