alter table consultations add column r_nbr_fact character varying(15);
alter table consultations add column r_nbr_total character varying(15);
alter table consultations add column r_type_fact character varying(5);
alter table consultations add column r_date_fact character varying(30);
alter table consultations add column r_num_dispositif_fact character varying(50);
alter table consultations add column r_nif_fact character varying(20);
alter table consultations add column r_signature_fact character varying(100);


CREATE TABLE IF NOT EXISTS public.factures
(
    id bigint NOT NULL DEFAULT nextval('factures_id_seq'::regclass),
    num_fact character varying COLLATE pg_catalog."default" NOT NULL,
    date_edition timestamp without time zone,
    doit character varying COLLATE pg_catalog."default",
    montant_total bigint,
    montant_lettre character varying COLLATE pg_catalog."default",
    who_done character varying COLLATE pg_catalog."default",
    when_done timestamp without time zone,
    is_deleted boolean,
    type_facture character varying COLLATE pg_catalog."default",
    r_nbr_fact character varying(15) COLLATE pg_catalog."default",
    r_nbr_total character varying(15) COLLATE pg_catalog."default",
    r_type_fact character varying(5) COLLATE pg_catalog."default",
    r_date_fact character varying(30) COLLATE pg_catalog."default",
    r_num_dispositif_fact character varying(50) COLLATE pg_catalog."default",
    r_nif_fact character varying(20) COLLATE pg_catalog."default",
    r_signature_fact character varying(100) COLLATE pg_catalog."default",
    CONSTRAINT factures_pkey PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS public.factures_details
(
    id bigint NOT NULL DEFAULT nextval('factures_details_id_seq'::regclass),
    facture bigint,
    libelle character varying COLLATE pg_catalog."default",
    quantie bigint,
    prix_unitaire bigint,
    taux_tva bigint,
    total_ttc bigint,
    who_done character varying COLLATE pg_catalog."default",
    when_done timestamp without time zone,
    is_deleted boolean,
    CONSTRAINT factures_details_pkey PRIMARY KEY (id),
    CONSTRAINT fk_facture FOREIGN KEY (facture)
        REFERENCES public.factures (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);



CREATE OR REPLACE VIEW public.v_consultations
 AS
 SELECT cons.id,
    cons.numero_consul,
    cons.type_consultation,
    typ.libelle AS libelle_type,
    cons.patient,
    pat.id AS id_patient,
    pat.nom_prenom AS nom_patient,
    pat.age,
    part.id AS partenaire_id,
    part.libelle AS nom_partenaire,
    cons.structure_patient,
    cons.taux_couverture,
    cons.tel_patient,
    cons.medecin_traitrant,
    pers.nom_p AS medecin,
    get_montant(cons.taux_couverture, typ.id) AS prix_consultation,
    cons.cout AS montant_consultation,
    cons.montant_en_lettre,
    cons.somme_recu,
    cons.somme_remise,
    cons.taux_couverture * get_montant(cons.taux_couverture, typ.id)::double precision AS montant_pris_en_charge,
    get_montant(cons.taux_couverture, typ.id)::double precision - cons.taux_couverture * get_montant(cons.taux_couverture, typ.id)::double precision AS montant_a_payer,
    cons.observation,
    cons.date_rdv,
    cons.is_closed,
    cons.is_deleted,
    cons.when_done,
    cons.who_done,
	cons.r_nbr_fact,
	cons.r_nbr_total,
	cons.r_type_fact,
	cons.r_date_fact,
	cons.r_num_dispositif_fact,
	cons.r_nif_fact,
	cons.r_signature_fact
   FROM consultations cons,
    patients pat,
    partenaire part,
    type_consultation typ,
    personnels pers
  WHERE pat.id = cons.patient AND pers.id = cons.medecin_traitrant AND part.id = pat.partenaire AND typ.id = cons.type_consultation
  ORDER BY cons.id DESC;
  

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