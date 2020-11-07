DROP VIEW public.v_soins;
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
	cons.medecin_traitrant as medecin,
    cons.age,
    cons.id_patient,
    cons.nom_patient,
    cons.tel_patient,
    cons.when_done AS date_consultation
   FROM examens exam,
    v_consultations cons
  WHERE cons.id = exam.consultation AND exam.is_deleted = false;

CREATE OR REPLACE VIEW public.v_soins
 AS
 SELECT ord.id,
    ord.libelle,
    ord.consultation,
    cons.numero_consul,
    cons.age,
    cons.id_patient,
    cons.tel_patient,
    cons.nom_patient,
    cons.when_done AS date_consultation,
    ord.is_deleted,
    ord.when_done,
    cons.libelle_type AS consultations,
    ord.who_done,
    ord.is_soin,
    ord.montant,
    ord.nombre,
    ord.total,
	cons.medecin_traitrant as medecin,
    cons.nom_partenaire,
    cons.partenaire_id,
    cons.taux_couverture,
    ord.total::double precision * cons.taux_couverture AS montant_pris_en_charge,
    ord.total::double precision - ord.total::double precision * cons.taux_couverture AS montant_net_a_payer
   FROM ordonances ord,
    v_consultations cons
  WHERE cons.id = ord.consultation AND ord.is_soin = true AND ord.is_deleted = false;

CREATE OR REPLACE VIEW public.vs_cons_medecin
 AS
 SELECT pat.id ,
 cons.id as ref_facture,
    pat.nom_p,
    pat.prenom_p,
    pat.categorie,
    pat.telephone,
	cons.montant_consultation as montant,
	cons.when_done
   FROM personnels pat,
   		v_consultations cons
  WHERE pat.id = cons.medecin_traitrant and cons.montant_consultation > 0
  ORDER BY pat.id ASC;
  
-- DROP VIEW  public.vs_exam_medecin;
  CREATE OR REPLACE VIEW public.vs_exam_medecin AS
 SELECT concat('E',exam.consultation) as ref_facture,
    pat.nom_p,
    pat.prenom_p,
    pat.categorie,
    pat.telephone,
	exam.cout_examen as montant,
	exam.when_done
   FROM personnels pat,
   		v_examens exam	
  WHERE pat.id = exam.medecin and exam.cout_examen > 0
  ORDER BY pat.id ASC;
  
   
  CREATE OR REPLACE VIEW public.vs_exam_medecin AS
 SELECT concat('S',ss.consultation) as ref_facture,
    pat.nom_p,
    pat.prenom_p,
    pat.categorie,
    pat.telephone,
	ss.total as montant,
	ss.when_done
   FROM personnels pat,
   		v_soins ss	
  WHERE pat.id = ss.medecin and ss.total > 0
  ORDER BY pat.id ASC;
  
  CREATE TABLE public.infos_medicale
(
    id bigint NOT NULL DEFAULT nextval('infos_medicale_id_seq'::regclass),
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


CREATE TABLE public.rendezvous
(
    id_rdv bigserial NOT NULL,
    nom_patient character varying COLLATE pg_catalog."default",
    num_tel character varying COLLATE pg_catalog."default",
    motif_rdv character varying COLLATE pg_catalog."default",
    date_rdv timestamp without time zone NOT NULL,
    who_done character varying COLLATE pg_catalog."default",
    when_done timestamp without time zone,
    on_deleted boolean,
    CONSTRAINT rendezvous_pkey PRIMARY KEY (id_rdv)
);

