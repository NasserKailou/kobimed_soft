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