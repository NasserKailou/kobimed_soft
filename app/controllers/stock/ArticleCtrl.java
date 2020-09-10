/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.stock;

import com.google.inject.Inject;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import models.stock.tables.pojos.Article;
import models.stock.tables.pojos.Rangee;
import models.stock.tables.pojos.VArticle;
import play.data.FormFactory;
import play.mvc.*;
import play.mvc.Http.Request;
import services.stock.ArticleService;
import services.stock.RangeService;

import static play.mvc.Results.ok;
import play.data.DynamicForm;
import play.data.Form;

import utils.CallJasperReport;
import utils.Secured;
import utils.ViewMode;
import views.html.*;
import views.*;

/**
 *
 * @author anasser
 */
@Security.Authenticated(Secured.class)
public class ArticleCtrl extends Controller {
	private final FormFactory formFactory;
	private final ArticleService articleService;
	private final RangeService rangeService;
	private final CallJasperReport jasper;

	@Inject
	public ArticleCtrl(FormFactory formFactory, ArticleService articleService, RangeService rangeService,
			CallJasperReport jasper) {
		this.articleService = articleService;
		this.rangeService = rangeService;
		this.formFactory = formFactory;
		this.jasper = jasper;
	}

	public Result index(Http.Request request) {
//		if (!isAdmin()) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result show(String subAction, Long idArticle,Http.Request request) {
//		if (!isAdmin()) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		VArticle a;
		List<VArticle> articleses;

		if (isFinancier(request))
			articleses = articleService.getArticleByOwner(String.valueOf(request.session().get("droit")));
		else
			articleses = articleService.getAllArticle();
		
		List<Rangee> rangees = rangeService.getAll();
		if (null == idArticle || idArticle.equals(0L)) {
			a = new VArticle();
			viewMode = ViewMode.VIEW_MODE_CREATE;
			a.setPQte(0L);
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			a = articleService.findArticleById(idArticle);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			a = articleService.findArticleById(idArticle);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			a = articleService.findArticleById(idArticle);

		}
		return ok(views.html.stock.article.render(viewMode, articleses, a, rangees, request));

	}

	public Result save(Http.Request request) {
//		if (!isAdmin()) {
//			return redirect(controllers.routes.AuthenticationCtrl.logout());
//		}
		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		final String articleId = formFactory.form().bindFromRequest(request).get("article-id");
		// final String rangeId = formFactory.form().bindFromRequest().get("range-id");
		final String rad = formFactory.form().bindFromRequest(request).get("rad");
		//final String transferer = formFactory.form().bindFromRequest().get("trans");
		Form<Article> uForm = formFactory.form(Article.class).bindFromRequest(request);
		Long qtePlaquette = 0L;
		Long idPlPdt = 0L;
		if (rad.equals("oui")) {
			qtePlaquette = Long.parseLong(formFactory.form().bindFromRequest(request).get("pQte"));
			idPlPdt = Long.parseLong(formFactory.form().bindFromRequest(request).get("particle"));
		}
		
			
		// String[] tvl = request().body().asFormUrlEncoded().get("rad");
		Article a = uForm.get();
		
//		if(transferer.equals("oui"))
//			a.setBeTransfert(true);
		
		if (articleService.isArticleExiste(a.getLibelle()) && !viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			
			return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "article avec libelle" + a.getLibelle() + " existe déja");
		}
		a.setWhenDone(new Timestamp(System.currentTimeMillis()));
		a.setWhoDone(String.valueOf(request.session().get("login")));
		a.setIdRange(1L);
		a.setIsDeleted(false);
		a.setPQte(qtePlaquette);
		a.setProduitQteId(idPlPdt);
		a.setPrixVente(0L);
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			if (articleService.saveLogical(a, true).equals("ok")) {
				
				return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", "article " + a.getLibelle() + " ajouter avec sucess");
			} else {
				System.out.println(articleService.saveLogical(a, true));
				
				return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "article " + a.getLibelle() + " non ajouter");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			a.setId(Long.parseLong(articleId));
			VArticle v = articleService.findArticleById(Long.parseLong(articleId));
			if (!a.getLibelle().equals(v.getLibelle())) {
				if (articleService.isArticleExiste(a.getLibelle())) {
					
					return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "article avec libelle" + a.getLibelle() + " existe déja");
				}
			}
			if (articleService.saveLogical(a, false).equals("ok")) {
				
				return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", "article " + a.getLibelle() + " modifier avec sucess");
			} else {
				
				return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "article " + a.getLibelle() + " non modifier");
			}
		}
		return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result delete(Long id, Http.Request request) {
		if (!isAdmin(request)) {
			return redirect(controllers.routes.AuthenticationCtrl.logout());
		}
		if (articleService.deleteArticle(id).equals("ok")) {
			
			return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", "article id : " + id + " supprimer avec sucess");
		} else {
			
			return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "article id : " + id + " non supprimer");
		}
	}

	private boolean isAdmin(Http.Request request) {
		return String.valueOf(request.session().get("droit")).equals("Admin");
	}

	public Result printSituationProduit(Long idFacture, Http.Request request) {
		String fileName = "recap_produit";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			jasper.generateReport(fileName, String.valueOf(idFacture));

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + idFacture + ".pdf")).flashing("success", "impression ok");
			// return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,
			// "Vente"));
		} catch (Exception e) {
			
			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "erreur impression");
		}

	}

	public Result printSituationAllProduit(Http.Request request) {
		String fileName = "recap_all_produit";
		LocalDateTime now = LocalDateTime.now();
		String now_string = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		String templateDir = new File("").getAbsolutePath() + "/reports/spool/";
		try {
			

			jasper.generateReport(fileName, "0");

			return ok(new java.io.File(templateDir + fileName + "_" + now_string + "_" + 0 + ".pdf")).flashing("success", "impression ok");
			// return redirect(routes.MouvementCtrl.show(ViewMode.VIEW_MODE_CREATE,
			// "Vente"));
		} catch (Exception e) {
			
			System.out.println(e.getMessage() + "+++++++--**///////++++++++");
			return redirect(routes.ArticleCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "erreur impression");
		}

	}
	
	private boolean isFinancier(Request request) {
		return String.valueOf(request.session().get("droit")).equals("Financier");
	}
}
