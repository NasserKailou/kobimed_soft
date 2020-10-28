package controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.Examens;
import models.public_.tables.pojos.Rendezvous;
import models.public_.tables.pojos.VConsultations;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.mvc.Http.Request;
import services.ConsultationsMainServices;
import services.RdvMainService;
import utils.Secured;
import utils.ViewMode;

@Security.Authenticated(Secured.class)
public class RdvCtrl extends Controller {
	
	FormFactory formFactory;
	RdvMainService rdvService;
	ConsultationsMainServices consServices;

	@Inject
	public RdvCtrl(FormFactory formFactory, RdvMainService rdvService,
			ConsultationsMainServices consServices) {
		super();
		this.formFactory = formFactory;
		this.rdvService = rdvService;
		this.consServices = consServices;
	}
	
	public Result show(String subAction, Long idRdv, Request request) {

//		if (!isAdmin()) {
//			return redirect(routes.AuthenticationCtrl.logout());
//		}
		String viewMode;
		Rendezvous c;

		if (0 == idRdv || null == idRdv) {
			c = new Rendezvous();
			viewMode = ViewMode.VIEW_MODE_CREATE;
		} else if (ViewMode.VIEW_MODE_EDIT.equals(subAction)) {
			c = rdvService.findById(idRdv);
			viewMode = ViewMode.VIEW_MODE_EDIT;
		} else if (ViewMode.VIEW_MODE_TRAITE.equals(subAction)) {
			c = rdvService.findById(idRdv);
			viewMode = ViewMode.VIEW_MODE_TRAITE;
		} else if (ViewMode.VIEW_MODE_DELETE.equals(subAction)) {
			c = rdvService.findById(idRdv);
			viewMode = ViewMode.VIEW_MODE_DELETE;
		} else {
			viewMode = ViewMode.VIEW_MODE_VIEW;
			c = rdvService.findById(idRdv);

		}
		return ok(views.html.rdv_consultation.render(viewMode,rdvService.listeRdvAll(), c,request));

	}
	
	public Result restaure(Long idRdv,Request request) {
		Rendezvous c = rdvService.findById(idRdv);
		c.setOnDeleted(false);
		rdvService.update(c);
		return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}
	
	public Result save(Request request) {

		final String viewMode = formFactory.form().bindFromRequest(request).get("viewMode");
		String dateRdv = formFactory.form().bindFromRequest(request).get("tmpDate2");
		
		Form<Rendezvous> uForm = formFactory.form(Rendezvous.class).bindFromRequest(request);
		Rendezvous c = uForm.get();
		c.setWhenDone(new Timestamp(System.currentTimeMillis()));
		c.setOnDeleted(false);
		c.setWhoDone(String.valueOf(request.session().get("login").get()));
		c.setDateRdv(consServices.getDateT(dateRdv));
		
		if (viewMode.equals(ViewMode.VIEW_MODE_CREATE)) {
			
			if (rdvService.saveLogical(c, true).equals("ok")) {
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", " Rdv  ajouter avec success");
			} else {
				System.out.println("msg :" + rdvService.saveLogical(c, true));
				
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", " Examen  non ajouter ");
			}
		} else if (viewMode.equals(ViewMode.VIEW_MODE_EDIT)) {
			
			if (rdvService.saveLogical(c, false).equals("ok")) {
				
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", " Rendez-vous modifier avec success");
			} else {
				
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "R  dvnon modifier");
			}
		}  else if (viewMode.equals(ViewMode.VIEW_MODE_DELETE)) {
			//c.setOnDeleted(true);
		System.out.println("id rdv "+ c.getIdRdv());
			if (rdvService.suppression(rdvService.findById(c.getIdRdv())).equals("ok")) {
				
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("success", " Rdv  Supprimer avec success");
			} else {
				
				return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L)).flashing("error", "Rdv  non supprimer");
			}
		}
		return redirect(routes.RdvCtrl.show(ViewMode.VIEW_MODE_CREATE, 0L));
	}
	
	
	public Result listeRdv(Request request) {

		String dateD2 = formFactory.form().bindFromRequest(request).get("tmpDate2");
		Rendezvous r = new Rendezvous();
		List<Rendezvous> rdv = new ArrayList<>();
		if ((dateD2.isEmpty() || dateD2 == null))
			rdv = null;
		else
			rdv = rdvService.listeRDV(consServices.getDateT(dateD2));

		return ok(views.html.rdv_consultation.render(ViewMode.VIEW_MODE_CREATE, rdv,r,request));
	}
	
}