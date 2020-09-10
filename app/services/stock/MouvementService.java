/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stock;

import com.google.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import models.stock.tables.pojos.*;
import models.stock.tables.daos.MouvementDao;
import models.stock.tables.records.FactureRecord;
import services.ConnectionHelper;

import org.jooq.Result;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import java.util.Locale;
import models.stock.Tables;
import org.joda.time.DateTime;

/**
 * 
 * @author nasser
 *
 */
public class MouvementService extends MouvementDao implements IMouvement {

	private final ConnectionHelper con;
	@Inject
	private final ArticleService articleService;

	@Inject
	public MouvementService(ConnectionHelper con, ArticleService articleService) {
		this.con = con;
		this.setConfiguration(con.connection().configuration());
		this.articleService = articleService;
	}

	@Override
	public String saveLogical(Mouvement ma, boolean b) {
		if (!isQteOk2(ma).equals("ok") && ma.getOperation().equals("-")) {
			return "ok1";
		} else {
			try {
				if (b)
					super.insert(ma);
				else
					super.update(ma);
				return "ok";
			} catch (Exception e) {
				return e.getMessage();
			}
		}

	}

	@Override
	public Mouvement findMouvementById(Long id) {

		return super.findById(id);
	}

	@Override
	public List<Mouvement> findMouvementByUser(String user, String op) {
		return con.connection().selectFrom(Tables.MOUVEMENT).where(Tables.MOUVEMENT.WHO_DONE.eq(user))
				.and(Tables.MOUVEMENT.OPERATION.eq(op)).and(Tables.MOUVEMENT.IS_OK.eq(false))
				.fetchInto(Mouvement.class);
	}

	@Override
	public String deleteAllMouvementByUser(String user, String operation, boolean isRejet) {
		try {
			//System.out.println("+++++okkkkkkkkkkkkkkkkkkk" + user);
			con.connection().deleteFrom(Tables.MOUVEMENT).where(Tables.MOUVEMENT.WHO_DONE.eq(user))
					.and((Tables.MOUVEMENT.OPERATION.eq(operation))).and((Tables.MOUVEMENT.IS_OK.eq(false)))
					.and((Tables.MOUVEMENT.IS_REJET.eq(isRejet))).execute();
			con.connection().close();
			return "ok";

		} catch (Exception e) {
			//System.out.println(e.getMessage() + "+++++");
			return e.getMessage();
		}
	}

	@Override
	public String deleteMouvementById(Long id) {
		try {
			super.delete(super.findById(id));
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public VMouvement findMouvementByIdV(Long id) {
		return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.ID_M.eq(id))
				.fetchOneInto(VMouvement.class);
	}

	@Override
	public List<VMouvement> findMouvementByUserV(String user, String op) {
		String o = "";
		if (op.equals("approvisionnement"))
			o = "+";
		else if (op.equals("distribution") || op.equals("transfert"))
			o = "-";
		if (!o.equals(""))
			return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.WHO_DONE_M.eq(user))
					.and(Tables.V_MOUVEMENT.IS_OK.eq(false)).and(Tables.V_MOUVEMENT.OPERATION.eq(o))
					.and(Tables.V_MOUVEMENT.IS_REJET.eq(false)).fetchInto(models.stock.tables.pojos.VMouvement.class);
		else
			return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.WHO_DONE_M.eq(user))
					.and(Tables.V_MOUVEMENT.IS_OK.eq(false)).and(Tables.V_MOUVEMENT.OPERATION.eq("-"))
					.and(Tables.V_MOUVEMENT.IS_REJET.eq(true)).fetchInto(models.stock.tables.pojos.VMouvement.class);
	}

	@Override
	public String validerOperation(String login, String op, Long sommeRecue, Integer idFournisseur, Integer idService) {
		List<Mouvement> mt = findMouvementByUser(login, op);
		//System.out.println("la taille et les params:"+mt.size() +"-"+login+"-"+op);
		if (mt.size() <= 0)
			return "no";

		if (!mt.get(0).getOperation().equals("+")) {
			//System.out.println("ICI#######");
			if (!isQteOk1(mt))
				return "no";
		}

		Facture f = new Facture();
		Long montant = 0L;
		String montantLettre = "";
		Result<FactureRecord> record;

		if (idService == 0) {
			record = con.connection()
					.insertInto(Tables.FACTURE, Tables.FACTURE.WHEN_DONE, Tables.FACTURE.CLIENT,
							Tables.FACTURE.MONTANT_CLIENT)
					.values(new Timestamp(System.currentTimeMillis()), "", sommeRecue).returning(Tables.FACTURE.ID)
					.fetch();
		} else {
			record = con.connection()
					.insertInto(Tables.FACTURE, Tables.FACTURE.WHEN_DONE, Tables.FACTURE.CLIENT,
							Tables.FACTURE.MONTANT_CLIENT,Tables.FACTURE.IS_TRANSFERT, Tables.FACTURE.SERVICE_ID)
					.values(new Timestamp(System.currentTimeMillis()), "", sommeRecue,true, idService)
					.returning(Tables.FACTURE.ID).fetch();
		}

		Long idF = record.getValue(0, Tables.FACTURE.ID);
		montant = getMontantChiffre(mt);
		montantLettre = getMontantLettre(montant);
		try {
			if (valideok(mt, idF, idFournisseur)) {
				montantLettre = getMontantLettre(montant);
				con.connection().update(Tables.FACTURE).set(Tables.FACTURE.MONTANTCHIFFRE, montant)
						.set(Tables.FACTURE.MONTANTLETTRE, montantLettre).where(Tables.FACTURE.ID.eq(idF)).execute();
				con.connection().close();
			} else {
			//	System.out.println("ICI####### NO");
				return "no";
			}

			return idF.toString();

		} catch (Exception e) {
			return "no";
		}
	}

	@Override
	public List<VMouvement> findMouvementByUserVV(String user) {
		return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.WHO_DONE_M.eq(user))
				.fetchInto(models.stock.tables.pojos.VMouvement.class);
	}

	@Override
	public List<VMouvement> findMouvementAll() {
		return con.connection().selectFrom(Tables.V_MOUVEMENT).fetchInto(models.stock.tables.pojos.VMouvement.class);
	}

	// retourne le montant saisie en lettre, exemple: 500=cinq cent
	public String getMontantLettre(Long montant) {

		String montantLettres = "";

		NumberFormat formatter = new RuleBasedNumberFormat(Locale.FRANCE, RuleBasedNumberFormat.SPELLOUT);
		String montantL = formatter.format(montant);
		montantLettres = montantL.substring(0, 1).toUpperCase() + montantL.substring(1);

		return montantLettres;

	}

	public static String generateUserCode() {
		return String.valueOf(DateTime.now().toInstant().getMillis());
	}

	@Override
	public List<Facture> getFactureByUser(String user, String op) {
		List<VMouvement> mvs = con.connection().selectDistinct(Tables.V_MOUVEMENT.FACTURE_ID).from(Tables.V_MOUVEMENT)
				.where(Tables.V_MOUVEMENT.WHO_DONE_M.eq(user)).and(Tables.V_MOUVEMENT.OPERATION.eq(op))
				.fetchInto(models.stock.tables.pojos.VMouvement.class);
		List<Facture> lf = new ArrayList<>();
		for (VMouvement v : mvs) {
			lf.add(con.connection().selectFrom(Tables.FACTURE).where(Tables.FACTURE.ID.eq(v.getFactureId()))
					.orderBy(Tables.FACTURE.ID).fetchOneInto(Facture.class));
		}
		con.connection().close();
		return lf;
	}

	@Override
	public List<Facture> getAllFacture() {
		List<VMouvement> mvs = con.connection().selectDistinct(Tables.V_MOUVEMENT.FACTURE_ID).from(Tables.V_MOUVEMENT)
				.fetchInto(models.stock.tables.pojos.VMouvement.class);
		List<Facture> lf = new ArrayList<>();
		for (VMouvement v : mvs) {
			lf.add(con.connection().selectFrom(Tables.FACTURE).orderBy(Tables.FACTURE.ID).fetchOneInto(Facture.class));
		}
		con.connection().close();
		return lf;
	}

	@Override
	public List<Facture> getAllFactureByOperation(String op) {
		List<VMouvement> mvs = con.connection().selectDistinct(Tables.V_MOUVEMENT.FACTURE_ID).from(Tables.V_MOUVEMENT)
				.where(Tables.V_MOUVEMENT.OPERATION.eq(op)).fetchInto(models.stock.tables.pojos.VMouvement.class);
		List<Facture> lf = new ArrayList<>();
		for (VMouvement v : mvs) {
			lf.add(con.connection().selectFrom(Tables.FACTURE).where(Tables.FACTURE.ID.eq(v.getFactureId()))
					.orderBy(Tables.FACTURE.ID).fetchOneInto(Facture.class));
		}
		con.connection().close();
		return lf;
	}

	@Override
	public List<VMouvement> findMouvementByIdFacture(Long factureId) {
		return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.FACTURE_ID.eq(factureId))
				.fetchInto(models.stock.tables.pojos.VMouvement.class);
	}

	@Override
	public String viderReport(String user) {
		try {

			// con.connection().deleteFrom(Tables.REPORT).where(Tables.REPORT.WHO_DONE_M.eq(user)).execute();
			return "ok";

		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public String mouvementToReport(List<VMouvement> vm) {
		try {
			for (VMouvement v : vm) {
				//System.out.println(vm.size() + "+++++taille");
				/*
				 * con.connection().insertInto(Tables.REPORT, Tables.REPORT.CLIENT,
				 * Tables.REPORT.DESCRIPTION, Tables.REPORT.FACTURE_ID, Tables.REPORT.ID_ARTICLE
				 * , Tables.REPORT.ID_M, Tables.REPORT.LIBELLE, Tables.REPORT.LIBELLE_FACTURE,
				 * Tables.REPORT.MONTANTCHIFFRE, Tables.REPORT.MONTANTLETTRE,
				 * Tables.REPORT.MONTANT_MOUVEMENT , Tables.REPORT.OPERATION,
				 * Tables.REPORT.PRIX, Tables.REPORT.PRIX_VENTE, Tables.REPORT.QUANTITE_ARTICLE,
				 * Tables.REPORT.QUANTITE_M, Tables.REPORT.WHEN_DONE_ARTICLE ,
				 * Tables.REPORT.WHEN_DONE_F, Tables.REPORT.WHEN_DONE_M,
				 * Tables.REPORT.WHEN_IMPRIME, Tables.REPORT.WHO_DONE_ARTICLE,
				 * Tables.REPORT.WHO_DONE_M) .values(v.getClient(), v.getDescription(),
				 * v.getFactureId(), v.getIdArticle(), v.getIdM(), v.getLibelle(),
				 * v.getLibelleFacture() , v.getMontantchiffre(), v.getMontantlettre(),
				 * v.getMontantMouvement(), v.getOperation(), v.getPrix(), v.getPrixVente() ,
				 * v.getQuantiteArticle(), v.getQuantiteM(), v.getWhenDoneArticle(),
				 * v.getWhenDoneF(), v.getWhenDoneM() , v.getWhenImprime(),
				 * v.getWhoDoneArticle(), v.getWhoDoneM()) .execute();
				 */

			}
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public List<VFactureConsulster> getFactureConsulter(String login, String operation, boolean isRejet) {
		return con.connection().selectFrom(Tables.V_FACTURE_CONSULSTER)
				.where(Tables.V_FACTURE_CONSULSTER.WHO_DONE_M.eq(login))
				.and(Tables.V_FACTURE_CONSULSTER.OPERATION.eq(operation))
				.and(Tables.V_FACTURE_CONSULSTER.IS_REJET.eq(isRejet)).fetchInto(VFactureConsulster.class);
	}

	@Override
	public List<VMouvement> getMouvementByIdFacture(Long idFacture) {
		return con.connection().selectFrom(Tables.V_MOUVEMENT).where(Tables.V_MOUVEMENT.FACTURE_ID.eq(idFacture))
				.fetchInto(VMouvement.class);
	}

	@Override
	public VFactureConsulster getFactureConsulter(Long idFacture) {
		return con.connection().selectFrom(Tables.V_FACTURE_CONSULSTER)
				.where(Tables.V_FACTURE_CONSULSTER.FACTURE_ID.eq(idFacture)).fetchOneInto(VFactureConsulster.class);
	}

	@Override
	public boolean eclater(VArticle ba) {
		if (copyForEclater(ba).equals("ok")) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public List<VAchat> getAllAchat() {
		return con.connection().selectFrom(Tables.V_ACHAT).fetchInto(VAchat.class);
	}

	@Override
	public List<VVente> getAllVente() {
		return con.connection().selectFrom(Tables.V_VENTE).fetchInto(VVente.class);
	}

	@Override
	public List<VRejet> getAllRejet() {
		return con.connection().selectFrom(Tables.V_REJET).fetchInto(VRejet.class);
	}

	@Override
	public List<VArticle> getDispoBaisse() {
		return con.connection().selectFrom(Tables.V_ARTICLE).where(Tables.V_ARTICLE.QUANTITE_DISPONIBLE.between(0, 10))
				.fetchInto(VArticle.class);
	}

	@Override
	public Confi getConf(Long id) {
		return con.connection().selectFrom(Tables.CONFI).where(Tables.CONFI.ID.eq(id)).fetchOneInto(Confi.class);
	}

	@Override
	public List<Fournisseur> getAllFournisseur() {
		return con.connection().selectFrom(Tables.FOURNISSEUR).fetchInto(Fournisseur.class);
	}

	private boolean isQteOk1(List<Mouvement> mouvements) {
		boolean t = true;

		for (Mouvement v : mouvements) {
			if (v.getQuantite() > articleService.findArticleById(v.getArticleId()).getQuantiteDisponible())
				t = false;
		}
		return t;
	}

	private String copyForEclater(VArticle ba, VArticle pa) {
		try {
			con.connection()
					.insertInto(Tables.MOUVEMENT, Tables.MOUVEMENT.PRIX, Tables.MOUVEMENT.QUANTITE,
							Tables.MOUVEMENT.OPERATION, Tables.MOUVEMENT.ARTICLE_ID, Tables.MOUVEMENT.WHO_DONE,
							Tables.MOUVEMENT.MONTANT_MOUVEMENT, Tables.MOUVEMENT.IS_OK, Tables.MOUVEMENT.IS_REJET,
							Tables.MOUVEMENT.WHEN_DONE)
					.values(pa.getPrixVente(), ba.getPQte().intValue(), "+", pa.getId(), pa.getWhoDone(),
							pa.getPrixVente() * ba.getPQte(), true, false, pa.getWhenDone())
					.execute();

			con.connection()
					.insertInto(Tables.MOUVEMENT, Tables.MOUVEMENT.PRIX, Tables.MOUVEMENT.QUANTITE,
							Tables.MOUVEMENT.OPERATION, Tables.MOUVEMENT.ARTICLE_ID, Tables.MOUVEMENT.WHO_DONE,
							Tables.MOUVEMENT.MONTANT_MOUVEMENT, Tables.MOUVEMENT.IS_OK, Tables.MOUVEMENT.IS_REJET,
							Tables.MOUVEMENT.WHEN_DONE)
					.values(ba.getPrixVente(), 1, "-", ba.getId(), pa.getWhoDone(), ba.getPrixVente(), true, false,
							pa.getWhenDone())
					.execute();
			con.connection().close();
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String copyForEclater(VArticle vAB) {
		VArticle vAQ = getProduitPlaquette(vAB.getProduitQteId());
		try {
			con.connection()
					.insertInto(Tables.MOUVEMENT, Tables.MOUVEMENT.PRIX, Tables.MOUVEMENT.QUANTITE,
							Tables.MOUVEMENT.OPERATION, Tables.MOUVEMENT.ARTICLE_ID, Tables.MOUVEMENT.WHO_DONE,
							Tables.MOUVEMENT.MONTANT_MOUVEMENT, Tables.MOUVEMENT.IS_OK, Tables.MOUVEMENT.IS_REJET,
							Tables.MOUVEMENT.WHEN_DONE)
					.values(getMontantAchatProd(vAB) / vAB.getPQte(), vAB.getPQte().intValue(), "+", vAQ.getId(),
							vAB.getWhoDone(), getMontantAchatProd(vAB) / vAB.getPQte() * vAB.getPQte(), true, false,
							vAB.getWhenDone())
					.execute();

			con.connection()
					.insertInto(Tables.MOUVEMENT, Tables.MOUVEMENT.PRIX, Tables.MOUVEMENT.QUANTITE,
							Tables.MOUVEMENT.OPERATION, Tables.MOUVEMENT.ARTICLE_ID, Tables.MOUVEMENT.WHO_DONE,
							Tables.MOUVEMENT.MONTANT_MOUVEMENT, Tables.MOUVEMENT.IS_OK, Tables.MOUVEMENT.IS_REJET,
							Tables.MOUVEMENT.WHEN_DONE)
					.values(vAB.getPrixVente(), 1, "-", vAB.getId(), vAB.getWhoDone(), vAB.getPrixVente(), true, false,
							vAB.getWhenDone())
					.execute();
			con.connection().close();
			return "ok";
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private String isQteOk2(Mouvement mouvement) {
		String t = "ok";
		if (mouvement.getQuantite() > articleService.findArticleById(mouvement.getArticleId()).getQuantiteDisponible())
			t = "Quantité insuffiante";
		return t;
	}

	private boolean isQteOk(String user) {
		boolean t = true;
		List<models.stock.tables.pojos.VMouvement> vMouvements = findMouvementByUserV(user, "");
		for (models.stock.tables.pojos.VMouvement v : vMouvements) {
			// if(v.getQuantiteM() > v.getQuantiteArticle()){
			// t=false;
			// }
		}
		return t;
	}

	private boolean valideok(List<Mouvement> mouvements, Long idFacture, Integer idFournisseur) {
		try {
			for (Mouvement v : mouvements) {
				con.connection().update(Tables.MOUVEMENT).set(Tables.MOUVEMENT.IS_OK, true)
						.set(Tables.MOUVEMENT.FOURNISSEUR_ID, idFournisseur).set(Tables.MOUVEMENT.FACTURE_ID, idFacture)
						.where(Tables.MOUVEMENT.ID.eq(v.getId())).execute();
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	private Long getMontantChiffre(List<Mouvement> mouvements) {
		Long m = 0L;
		for (Mouvement v : mouvements) {
			//m += v.getMontantMouvement();
			m += v.getQuantite();
		}
		return m;
	}

	private VArticle getProduitPlaquette(Long id) {
		return con.connection().selectFrom(Tables.V_ARTICLE).where(Tables.V_ARTICLE.ID.eq(id))
				.fetchOneInto(VArticle.class);
	}

	private Long getMontantAchatProd(VArticle v) {
		return getMouvementProd(v).getPrix();
	}

	private VMouvement getMouvementProd(VArticle v) {
		List<VMouvement> vm = con.connection().selectFrom(Tables.V_MOUVEMENT)
				.where(Tables.V_MOUVEMENT.ID_ARTICLE.eq(v.getId())).and(Tables.V_MOUVEMENT.OPERATION.eq("+"))
				.fetchInto(VMouvement.class);
		con.connection().close();
		VMouvement m = new VMouvement();
		for (VMouvement vmm : vm) {
			m = vmm;
		}
		return m;
	}

	@Override
	public List<Services> getAllServices() {

		return con.connection().selectFrom(Tables.SERVICES).fetchInto(Services.class);

	}

}
