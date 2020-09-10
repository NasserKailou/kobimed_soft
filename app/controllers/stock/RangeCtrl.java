/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.stock;

import com.google.inject.Inject;
import models.stock.tables.pojos.Article;
import models.stock.tables.pojos.Rangee;
import models.stock.tables.pojos.VArticle;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;
import services.stock.*;
import utils.Secured;
import utils.ViewMode;

import java.sql.Timestamp;
import java.util.List;
import views.*;

/**
 *
 * @author anasser
 */
@Security.Authenticated(Secured.class)
public class RangeCtrl extends Controller {
	private final FormFactory formFactory;
	private final RangeService rangeService;

	@Inject
	public RangeCtrl(FormFactory formFactory, RangeService rangeService) {
		this.rangeService = rangeService;
		this.formFactory = formFactory;
	}

	public Result index(Request request) {
//if(!isAdmin()){
//            return redirect(controllers.routes.AuthenticationCtrl.logout());
//        }
		return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	public Result show(String subAction, Long idRange, Request request) {

//if(!isAdmin()){
//            return redirect(controllers.routes.AuthenticationCtrl.logout());
//        }
		String viewMode;
		Rangee r;
		List<Rangee> rangees = rangeService.getAll();
		if (null == idRange || idRange.equals(0L)) {
			r = new Rangee();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			r = rangeService.findById(idRange);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			r = rangeService.findById(idRange);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			r = rangeService.findById(idRange);

		}
		return ok(views.html.stock.range.render(viewMode, rangees, r, request));

	}

	public Result save(Request request) {
//if(!isAdmin()){
//            return redirect(controllers.routes.AuthenticationCtrl.logout());
//        }
		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		final String rangeeId = formFactory.form().bindFromRequest(request).get("rangee-id");
		Form<Rangee> uForm = formFactory.form(Rangee.class).bindFromRequest(request);
		Rangee r = uForm.get();
		if (rangeService.isRangeExist(r.getLibelle()) && !viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {

			return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
					"Rangé avec libelle " + r.getLibelle() + " existe déja");
		}
		r.setWhoDone(String.valueOf(request.session().get("login")));
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			if (rangeService.saveLogical(r, true).equals("ok")) {

				return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Rangé  " + r.getLibelle() + " ajouter avec sucess");
			} else {

				return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Rangé " + r.getLibelle() + " non ajouter");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			r.setId(Long.parseLong(rangeeId));
			if (!rangeService.findById(Long.parseLong(rangeeId)).getLibelle().equals(r.getLibelle())) {
				if (rangeService.isRangeExist(r.getLibelle())) {

					return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
							"Rangé avec libelle " + r.getLibelle() + " existe déja");
				}
			}
			if (rangeService.saveLogical(r, false).equals("ok")) {

				return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success",
						"Rangé " + r.getLibelle() + " modifier avec sucess");
			} else {

				return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error",
						"Rangé " + r.getLibelle() + " non modifier");
			}
		}
		return redirect(routes.RangeCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}

	private boolean isAdmin(Request request) {
		return String.valueOf(request.session().get("droit")).equals("Admin");
	}

}
