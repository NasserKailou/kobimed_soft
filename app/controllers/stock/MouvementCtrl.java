/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.stock;

import com.google.inject.Inject;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.public_.tables.pojos.Users;
import models.stock.tables.pojos.*;
import models.stock.tables.pojos.VMouvement;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;

import static play.mvc.Results.ok;
import static play.mvc.Results.redirect;
import play.mvc.*;
import services.UserService;
import utils.CallJasperReport;
import utils.Secured;
import utils.ViewMode;
import services.stock.*;
import java.util.ArrayList;
import views.*;

/**
 *
 * @author anasser
 */
@Security.Authenticated(Secured.class)
public class MouvementCtrl extends Controller {

	private final FormFactory formFactory;
	private final ArticleService articleService;
	private final MouvementService mouvementService;
	private final UserService userService;
	private static List<VMouvement> vmt;
	private static String message;

	private final CallJasperReport jasper;

	@Inject
	public MouvementCtrl(FormFactory formFactory, ArticleService articleService, MouvementService mouvementService,
			UserService userService, CallJasperReport jasper) {
		this.articleService = articleService;
		this.formFactory = formFactory;
		this.mouvementService = mouvementService;
		this.userService = userService;
		this.vmt = new ArrayList<>();
		this.message = "";
		this.jasper = jasper;
	}

	public Result index(String operation, Http.Request request) {
//		if (!isAdmin() && (operation.equals("Approvisionnement") || operation.equals("Rejet"))) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}

		return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,operation));
	}

	public Result show(String subAction, String operation, Http.Request request) {

//		if (!isAdmin() && (operation.equals("Approvisionnement") || operation.equals("Rejet"))) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Article a;
		List<VArticle> articleses;

		if (isFinancier(request))
			articleses = articleService.getArticleByOwner(String.valueOf(request.session().get("droit")));
		else
			articleses = articleService.getAllArticle();

		// List<Facture> factureses =
		// mouvementService.getFactureByUser(session("login"), operation.substring(0,
		// 1).toLowerCase());
		// System.out.println(operation.substring(0).toLowerCase());
		List<VMouvement> mas = mouvementService.findMouvementByUserV(String.valueOf(request.session().get("login")),
				operation.substring(0).toLowerCase());
		// System.out.println("les mvt sont :"+ mas);
		if (ViewMode.VIEW_MODE_CREATE.equals(subAction)) {
			a = new Article();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
		}
		List<Fournisseur> fournisseurs = mouvementService.getAllFournisseur();
		Fournisseur f = new Fournisseur();
		f.setId(6);
		// fournisseurs.remove(0);
		return ok(views.html.stock.mouvement.render(viewMode, articleses, mas, operation, fournisseurs,
				mouvementService.getAllServices(), request));

	}

	public Result save(Http.Request request) {
		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		final String articleId = formFactory.form().bindFromRequest(request).get("article-id");
		final String operation = formFactory.form().bindFromRequest(request).get("op");

		Form<Mouvement> uForm = formFactory.form(Mouvement.class).bindFromRequest(request);
		Mouvement ma = uForm.get();
		ma.setPrix(0L);
		ma.setWhenDone(new Timestamp(System.currentTimeMillis()));
		ma.setWhoDone(String.valueOf(request.session().get("login")));
		ma.setMontantMouvement(ma.getPrix() * ma.getQuantite());
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			ma.setArticleId(Long.parseLong(articleId));

			if (!operation.equals("Rejet"))
				ma.setIsRejet(false);
			else
				ma.setIsRejet(true);

			ma.setOperation(getOPerationSign(operation));
			String message = mouvementService.saveLogical(ma, true);
			if (message.equals("ok")) {
				
				return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation)).flashing("success", "article id " + ma.getArticleId() + " ajouter a la liste selectionner");
			} else {
				if (message.equals("ok1"))
					return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation)).flashing("error", "quantité insuffisante pour cet article");
				else
					return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation)).flashing("error", "article id " + ma.getArticleId() + " non ajouter a la liste selectionner");
				
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			ma.setArticleId(Long.parseLong(articleId));
			if (mouvementService.saveLogical(ma, false).equals("ok")) {
				return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation));
			} else {
				return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation));
			}
		}
		return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation));
	}

	public Result deleteById(Long id, String operation, Http.Request request) {
//		if (!isAdmin() && (operation.equals("Approvisionnement") || operation.equals("Rejet"))) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		if (mouvementService.deleteMouvementById(id).equals("ok")) {
			
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation)).flashing("success", "article id " + id + " supprimer de la liste selectionner");
		} else {
			
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, operation)).flashing("error", "article id " + id + " non supprimer de la liste selectionner");
		}
	}

	public Result deleteByUser(String user, String operation, Http.Request request) {

//		if (!isAdmin() && (operation.equals("Approvisionnement") || operation.equals("Rejet"))) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}

		boolean isRejet = false;
		if (operation.equals("Rejet"))
			isRejet = true;

		if (mouvementService.deleteAllMouvementByUser(String.valueOf(request.session().get("login")), getOPerationSign(operation), isRejet)
				.equals("ok")) {

			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,operation))
					.flashing("success", "articles  supprimer de la liste selectionner");
		} else {

			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,operation))
					.flashing("success", "articles non supprimer de la liste selectionner");
			
		}
	}

	public Result getFactures(Http.Request request) {
		List<Users> users = userService.getAllUser();
		List<VFactureConsulster> vFactureConsulsters = new ArrayList<>();
		List<VMouvement> vMouvements = new ArrayList<>();
		boolean isRejet = false;
		String m = "Liste des Bons";
		return getf(m, users, vFactureConsulsters, vMouvements, request);
		// if (operation.equals("Rejet"))
		// isRejet = true;
		// return ok(views.html.facture.render());
	}

	public Result rFacture(Http.Request request) {
		String operation = "Distribution";
		String login = String.valueOf(request.session().get("login"));
		if (String.valueOf(request.session().get("dtoit")).equals("Admin")) {
			operation = formFactory.form().bindFromRequest(request).get("operation");
			login = formFactory.form().bindFromRequest(request).get("login");
		}
		final String dt = formFactory.form().bindFromRequest(request).get("date");
		Timestamp t = StringToTimestamp(dt);
		boolean isRejet = false;
		if (operation.equals("Rejet"))
			isRejet = true;
		List<Users> users = userService.getAllUser();
		List<VFactureConsulster> vFactureConsulsters1 = new ArrayList<>();
		List<VFactureConsulster> vFactureConsulsters = mouvementService.getFactureConsulter(login,
				getOPerationSign(operation), isRejet);
		vFactureConsulsters1 = filtreFactureDate(vFactureConsulsters, t);
		List<VMouvement> vMouvements = new ArrayList<>();
		String m = "Liste des Bons (" + operation + ") de " + login + " du " + dt;
		return getf(m, users, vFactureConsulsters1, vMouvements, request);
		//
	}

	public Result getf(String mF, List<Users> users, List<VFactureConsulster> vFactureConsulsters,
			List<VMouvement> vMouvements, Http.Request request) {
		return ok(views.html.stock.facture.render(vMouvements, vFactureConsulsters, users, mF,request));
	}

	public Result dispoover(Http.Request request) {
//		if (!isAdmin()) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		return ok(views.html.stock.dispoover.render(mouvementService.getDispoBaisse(), request));
	}

	public Result compta(Http.Request request) {
//		if (!isAdmin()) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		Long mA = getMontantAchat(mouvementService.getAllAchat());
		Long mV = getMontantVente(mouvementService.getAllVente());
		Long mR = getMontantRejet(mouvementService.getAllRejet());
		Long mo = getMontantOther(articleService.getAllArticle());

		return ok(views.html.stock.compta.render(mA, mV, mR, mo, request));
	}

	public Result jsonListeMouvement(String idf) {
		String lid = null == idf ? "" : idf;
		List<VMouvement> vMouvements = mouvementService.getMouvementByIdFacture(Long.parseLong(lid));
		HashMap<String, List<VMouvement>> response = new HashMap<>();
		response.put("options", vMouvements);
		return ok(Json.toJson(response));
	}

	public Result validerop(String op, Http.Request request) {

		final String serviceId = formFactory.form().bindFromRequest(request).get("servicesId");
		System.out.println("le services est :" + serviceId);

//		if (!isAdmin() && (op.equals("Approvisionnement") || op.equals("Rejet"))) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		String idFacture = "";
		Long sommeRecue = 0L;
		Integer idFournisseur = 5;
		if (op.equals("Distribution"))
			sommeRecue = 0L;// sommeRecue =
							// Long.parseLong(formFactory.form().bindFromRequest().get("sommerecue").replaceAll("\\s+",""));
		if (op.equals("Approvisionnement"))
			idFournisseur = 6;// Integer.parseInt(formFactory.form().bindFromRequest().get("fourniss"));
		if (op.equals("Transfert")) {
			System.out.println("la facture : " + idFacture);
			idFacture = mouvementService.validerOperation(String.valueOf(request.session().get("login")),
					getOPerationSign(op), sommeRecue, idFournisseur, Integer.valueOf(serviceId));
		} else {
			System.out.println("la facturekkkk : " + idFacture + " Operation :" + op);
			idFacture = mouvementService.validerOperation(String.valueOf(request.session().get("login")),
					getOPerationSign(op), sommeRecue, idFournisseur, Integer.valueOf("0"));
		}
		System.out.println("la facture fin : " + idFacture);
		if (!idFacture.equals("no")) {
			
			VFactureConsulster vFactureConsulster = mouvementService.getFactureConsulter(Long.parseLong(idFacture));
			vFactureConsulster.setOperation(op);
			return ok(views.html.stock.facturetoprint.render(vFactureConsulster,request)).flashing("success", op + " valider ");
		} else {
			
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, op)).flashing("error", op + " non valider ");
		}
	}

	public Result tindex(Http.Request request) {
		List<VArticle> articles = articleService.getAllArticle();
		return ok(views.html.stock.transform.render(articles, request));
	}

	public Result tsave(Http.Request request) {
		final String idArB = formFactory.form().bindFromRequest(request).get("barticle");
		VArticle ba = articleService.findArticleById(Long.parseLong(idArB));
		List<VArticle> articles = articleService.getAllArticle();
		String message = isBoiteToPlaqueteAndStockOk(ba);
		if (message.equals("ok")) {
			ba.setWhoDone(String.valueOf(request.session().get("login")));
			ba.setWhenDone(new Timestamp(System.currentTimeMillis()));
			// pa.setPrixVente(Long.parseLong(prix.replaceAll("\\s+","")));
			if (mouvementService.eclater(ba)) {

				return redirect(routes.MouvementCtrl.tindex()).flashing("success", "eclatement du carton ok");
			} else {

				return redirect(routes.MouvementCtrl.tindex()).flashing("error", "erreur eclatement");
			}

		} else {

			return redirect(routes.MouvementCtrl.tindex()).flashing("error", ba.getLibelle() + "    " + message);
		}
	}

//    public Result printRecu(Long idFacture) {
//        String path="";
//        PdfPrinter p=new PdfPrinter(mouvementService.getConf(1L));
//        try {
//            path=  p.printer(idFacture);
//            Runtime rt = Runtime.getRuntime();
//            Process pr = rt.exec("evince "+path);
//            //return ok(new java.io.File(path));
//            flash("success",  "impression ok");
//            return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, "Vente"));
//        } catch (Exception e) {
//            flash("error",  "erreur impression");
//	    System.out.println(e.getMessage() + "+++++++--**///////++++++++");
//            return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, "Vente"));
//        }
//
//
//    }

	public Result printRecu(Long idFacture, Http.Request request) {
		String fileName = "facture";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {

			jasper.generateReport(fileName, String.valueOf(idFacture));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + idFacture + ".pdf"))
					.flashing("success", "impression ok");
			// return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,
			// "Vente"));
		} catch (Exception e) {

			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, "Distribution"))
					.flashing("error", "erreur impression");
		}
	}

	public Result printBonCaise(Long idFacture, Http.Request request) {
		String fileName = "bonSortie";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {

			jasper.generateReport(fileName, String.valueOf(idFacture));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + idFacture + ".pdf"))
					.flashing("success", "impression ok");
			// return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,
			// "Vente"));
		} catch (Exception e) {

			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, "Distribution"))
					.flashing("error", "erreur impression");
		}
	}

	public Result printBonEntrerCaisse(Long idFacture, Http.Request request) {
		String fileName = "bonEntrer";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {

			jasper.generateReport(fileName, String.valueOf(idFacture));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + idFacture + ".pdf"))
					.flashing("success", "impression ok");
			// return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,
			// "Vente"));
		} catch (Exception e) {

			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE, "Distribution"))
					.flashing("error", "erreur impression");
		}
	}

	private String getOPerationSign(String operation) {
		String sign = "";
		if (operation.equals("Approvisionnement"))
			sign = "+";
		else
			sign = "-";
		return sign;
	}

	private Timestamp StringToTimestamp(String st) {
		try {

			DateFormat formatter;
			formatter = new SimpleDateFormat("dd/MM/yyyy");
			// you can change format of date
			Date date = formatter.parse(st);
			java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
			return timeStampDate;
		} catch (Exception e) {
			System.out.println(e.getMessage() + "+++++++++++++++");
			return null;

		}

	}

	private List<VMouvement> filtreMouvementDate(List<VMouvement> vms, Timestamp t) {
		List<VMouvement> vs = new ArrayList<>();
		for (VMouvement v : vms) {
			// if (v.getWhenDoneF().getDate() == t.getDate())
		}
		return vs;
	}

	private List<VFactureConsulster> filtreFactureDate(List<VFactureConsulster> vms, Timestamp t) {
		List<VFactureConsulster> vs = new ArrayList<>();
		for (VFactureConsulster v : vms) {
			if (v.getWhenDoneF().getDate() == t.getDate()) {

				if (v.getOperation().equals("+")) {
					v.setOperation("Approvisionnement");
				} else {
					if (v.getIsRejet()) {
						v.setOperation("Rejet");
					} else {
						v.setOperation("Distribution");
					}

				}
				vs.add(v);
			}

		}
		return vs;
	}

	private boolean isBoiteToPlaquette(VArticle vArticle) {
		if (vArticle.getPQte() > 0)
			return true;
		else
			return false;
	}

	private String isBoiteToPlaqueteAndStockOk(VArticle vArticle) {
		if (vArticle.getPQte().equals(0L)) {
			return "Le produit n'est peut pas être éclater en unité!!!";
		} else if (vArticle.getQuantiteDisponible() < 1L) {
			return "Stock insuffisant pour éclater le produit";
		} else
			return "ok";
	}

	private Long getMontantAchat(List<VAchat> vAchats) {
		Long m = 0L;
		for (VAchat va : vAchats) {
			m += va.getMontantMouvement();
		}
		return m;
	}

	private Long getMontantVente(List<VVente> vVentes) {
		Long m = 0L;
		for (VVente va : vVentes) {
			m += va.getMontantMouvement();
		}
		return m;
	}

	private Long getMontantRejet(List<VRejet> vRejets) {
		Long m = 0L;
		for (VRejet va : vRejets) {
			m += va.getMontantMouvement();
		}
		return m;
	}

	private Long getMontantOther(List<VArticle> vRejets) {
		Long m = 0L;
		for (VArticle va : vRejets) {
			m += va.getPrixVente() * va.getQuantiteDisponible();
		}
		return m;
	}

	private boolean isAdmin(Http.Request request) {
		return String.valueOf(request.session().get("droit")).equals("Admin");
	}

	private boolean isFinancier(Http.Request request) {
		return String.valueOf(request.session().get("droit")).equals("Financier");
	}

}
