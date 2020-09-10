package controllers;

import java.sql.Timestamp;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.Partenaire;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.PartenaireMainServices;
import utils.Secured;
import utils.ViewMode;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class PartenaireCtrl extends Controller {

	FormFactory formFactory;
	PartenaireMainServices partenaireService;

	@Inject
	public PartenaireCtrl(FormFactory formFactory, PartenaireMainServices partenaireService) {
		super();
		this.formFactory = formFactory;
		this.partenaireService = partenaireService;
	}

	public Result show(String subAction, Long idPart, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Partenaire c;
		List<Partenaire> partenaire = partenaireService.findAll();
		List<Partenaire> partenaireDeleted = partenaireService.findPartenaireupprimer();
		if (0 == idPart) {
			c = new Partenaire();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = partenaireService.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = partenaireService.findById(idPart);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = partenaireService.findById(idPart);

		}
		return ok(views.html.partenaires.render(viewMode, partenaire, partenaireDeleted, c, request));

	}

	public Result restaure(Long idPart, Request request) {
		Partenaire c = partenaireService.findById(idPart);
		c.setIsDeleted(false);
		partenaireService.update(c);
		return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");

		Form<Partenaire> uForm = formFactory.form(Partenaire.class).bindFromRequest(request);
		Partenaire c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setIsDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login")));
		c.setTauxCouverture(0.00);
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {

			if (partenaireService.saveLogical(c, true).equals("ok")) {

				// System.out.println("Partenaire : " + c);
				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Partenaire " + c.getLibelle() + " ajouter avec success");
			} else {

				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Partenaire " + c.getLibelle() + " non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			// a.setLogin(login);
			if (partenaireService.saveLogical(c, false).equals("ok")) {

				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Partenaire  modifier avec success");
			} else {

				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Partenaire  non modifier");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			c.setIsDeleted(true);
			if (partenaireService.saveLogical(c, false).equals("ok")) {

				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Partenaire  Supprimer avec success");
			} else {

				return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Partenaire  non supprimer");
			}
		}
		return redirect(routes.PartenaireCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

}
