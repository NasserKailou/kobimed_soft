package controllers;

import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import com.google.inject.Inject;

import models.public_.tables.pojos.SConsultations;
import models.public_.tables.pojos.VConsultations;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import play.mvc.Http.Request;
import services.ConsultationsMainServices;
import services.PartenaireMainServices;
import utils.Login;

/**
 * This controller contains an action to handle HTTP requests to the
 * application's home page.
 */

public class HomeController extends Controller {

	FormFactory formatFactory;
	ConsultationsMainServices consultationServices;
	
	PartenaireMainServices partenaireService;
	
	@Inject
	public HomeController(FormFactory formatFactory, ConsultationsMainServices consultationServices,PartenaireMainServices partenaireService) {

		this.formatFactory = formatFactory;
		this.consultationServices = consultationServices;
		this.partenaireService = partenaireService;

	}

	public Result index(Request request) {
//		if (null != request.session().get("login"))
//			//session().clear();
		return ok(views.html.index.render(request));
	}

//	public Result acceuil() {
//		if (session("login") == null) {
//			return ok(views.html.index.render());
//		} else {
//			List<VConsultations> consultations = consultationServices.listeConsultations();
//			String element = "";
//
//			for (VConsultations v : consultations) {
//				element += v.getLibelleType() + "@" + v.getNomPartenaire() + "@" + v.getMedecin() + "@"
//						+ v.getNomPartenaire() + "@" + v.getMontantAPayer() + "@" + v.getMontantPrisEnCharge() + "#";
//			}
//			
//			// System.out.println("les elemensts sont :" + element);
//			return ok(views.html.acceuil.render(element));
//		}
//	}

	public Result acceuil(Request request) {
		//System.out.println("les sessions sont login:" +request.session().get("login") +" droit :"+ request.session().get("droit") +" nonUser :" + request.session().get("nomUser"));
		if (request.session().get("login") == null) {
			return ok(views.html.index.render(request));
		} else {
			List<SConsultations> consultations =  partenaireService.consultationParPArtenaire();
			String element = "";

			for (SConsultations v : consultations) {
				element += v.getNomPartenaire() + "@" + v.getNombreDeConsultations() +"#";
			}
			
			// System.out.println("les elemensts sont :" + element);
			return ok(views.html.acceuil.render(element,request));
		}
	}
	/**
	 * Déconnexion d'un utilisateur
	 *
	 * @return
	 */
	public Result deconnecter(Request request) {	
		return redirect(controllers.routes.HomeController.index()).withNewSession().flashing("success", "vous avez été déconnecté");
	}

	/**
	 * @return
	 */
	public Result authentification(Request request) {

		Form<Login> forms = formatFactory.form(Login.class).bindFromRequest(request);
		if (forms.hasErrors()) {
			
			return redirect(controllers.routes.HomeController.index()).flashing("error", " Erreur de saisie");

		} else {
			Login login = forms.get();
			String log = formatFactory.form().bindFromRequest(request).get("login");
			String pass = formatFactory.form().bindFromRequest(request).get("pass_word");

			if (1 == 1) {
				
				return redirect(controllers.routes.HomeController.index()).flashing("error", "wrong email/password");
			} else {
				
				return redirect(controllers.routes.HomeController.acceuil()).addingToSession(request, "login", login.getLogin());
			}
		}
	}
}
