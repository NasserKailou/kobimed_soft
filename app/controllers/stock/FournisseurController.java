/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.stock;

import com.google.inject.Inject;
import models.stock.tables.pojos.Article;
import models.stock.tables.pojos.Fournisseur;
import models.stock.tables.pojos.VArticle;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Request;
import utils.Secured;
import utils.ViewMode;

import java.sql.Timestamp;
import java.util.List;
import services.stock.*;
import views.*;

/**
 *
 * @author anasser
 */
@Security.Authenticated(Secured.class)
public class FournisseurController extends Controller {
	FormFactory formFactory;
	FournisseurService FournisseurService;

	@Inject
	public FournisseurController(FormFactory formFactory, FournisseurService FournisseurService) {
		this.FournisseurService = FournisseurService;
		this.formFactory = formFactory;
	}

	public Result index(Http.Request request) {
		if (!isAdmin(request)) {
			return redirect(controllers.routes.AuthenticationCtrl.logout());
		}
		return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0));
	}

	public Result show(String subAction, int idFournisseur, Http.Request request) {

		if (!isAdmin(request)) {
			return redirect(controllers.routes.AuthenticationCtrl.logout());
		}
		String viewMode;
		Fournisseur r;
		List<Fournisseur> Fournisseurs = FournisseurService.getAll();
		if (0 == idFournisseur) {
			r = new Fournisseur();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			r = FournisseurService.findById(idFournisseur);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			r = FournisseurService.findById(idFournisseur);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			r = FournisseurService.findById(idFournisseur);

		}
		return ok(views.html.stock.fournisseur.render(viewMode, Fournisseurs, r, request));

	}

	public Result save(Http.Request request) {
		if (!isAdmin(request)) {
			return redirect(controllers.routes.AuthenticationCtrl.logout());
		}
		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		final String FournisseurId = formFactory.form().bindFromRequest(request).get("id");
		Form<Fournisseur> uForm = formFactory.form(Fournisseur.class).bindFromRequest(request);
		Fournisseur r = uForm.get();
		if (FournisseurService.isFournisseurExist(r.getNomF()) && !viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			
			return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0)).flashing("error", "Fournisseur avec libelle " + r.getNomF() + " existe déja");
		}
		// r.setWhoDone(session("login"));

		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			if (FournisseurService.saveLogical(r, true).equals("ok")) {

				return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0))
						.flashing("success", "Fournisseur  " + r.getNomF() + " ajouter avec sucess");
			} else {

				return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0))
						.flashing("error", "Fournisseur " + r.getNomF() + " non ajouter");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			System.out.println("##############" + FournisseurId);
			System.out.println("##############" + viewMode);
			r.setId(Integer.parseInt(FournisseurId));
			if (!FournisseurService.findById(Integer.parseInt(FournisseurId)).getNomF().equals(r.getNomF())) {
				if (FournisseurService.isFournisseurExist(r.getNomF())) {

					return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0))
							.flashing("error", "Fournisseur avec libelle " + r.getNomF() + " existe déja");
				}
			}
			if (FournisseurService.saveLogical(r, false).equals("ok")) {

				return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0))
						.flashing("success", "Fournisseur " + r.getNomF() + " modifier avec sucess");
			} else {

				return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0))
						.flashing("error", "Fournisseur " + r.getNomF() + " non modifier");
			}
		}
		return redirect(routes.FournisseurController.show(ViewMode.VIEW_MODE_CREATE, 0));
	}

	private boolean isAdmin(Request request) {
		return String.valueOf(request.session().get("droit")).equals("Admin");
	}

}
