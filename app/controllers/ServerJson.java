package controllers;

import static play.libs.Json.toJson;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import models.public_.tables.pojos.BasExamens;
import models.public_.tables.pojos.BasSoins;
import models.public_.tables.pojos.Partenaire;
import models.public_.tables.pojos.Patients;
import models.public_.tables.pojos.SConsultations;
import models.public_.tables.pojos.TypeConsultation;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import services.BaseExamenServices;
import services.BaseSoinServices;
import services.PartenaireMainServices;
import services.PatientsMainServices;
import services.TypeConsultationsMainServices;
import utils.Secured;

/**
 * 
 * @author nasser
 *
 */
@Security.Authenticated(Secured.class)
public class ServerJson extends Controller{

	@Inject
	PatientsMainServices patientService;
	
	@Inject
	PartenaireMainServices partenaireService;
	
	@Inject
	TypeConsultationsMainServices typeConsService;
	
	@Inject
	BaseSoinServices baseSoinServices;
	
	@Inject
	BaseExamenServices examServices;
	
	/**
	 * @author nasser
	 * @param query
	 * @return
	 */
	public Result jsonPatients(String query) {
		//System.out.println("ICI JSON patient");
		String lQuery = null == query ? "" : query;
		List<Patients> details = patientService.findLikePatient(lQuery);
		HashMap<String, List<Patients>> response = new HashMap<>();
		response.put("options", details);
		return ok(toJson(response));
	}
	
	/**
	 * @author nasser
	 * @param query
	 * @return
	 */
	public Result jsonTypeConsultation(String query) {
		//System.out.println("ICI JSON");
		String lQuery = null == query ? "" : query;
		List<TypeConsultation> details = typeConsService.findLikeName(lQuery);
		HashMap<String, List<TypeConsultation>> response = new HashMap<>();
		response.put("options", details);
		return ok(toJson(response));
	}
	
	/**
	 * @author nasser
	 * @param query
	 * @return
	 */
	public Result jsonPartenaire(String query) {
		//System.out.println("ICI JSON");
		String lQuery = null == query ? "" : query;
		List<Partenaire> details = partenaireService.findLikeName(lQuery);
		HashMap<String, List<Partenaire>> response = new HashMap<>();
		response.put("option", details);
		return ok(toJson(response));
	}
	
	//propose la liste des examens à faire
	public Result jsonExamens(String query) {
		//System.out.println("ICI JSON");
		String lQuery = null == query ? "" : query;
		List<BasExamens> details = examServices.findLikeName(lQuery);
		HashMap<String, List<BasExamens>> response = new HashMap<>();
		response.put("option", details);
		return ok(toJson(response));
	}
	
	
	//propose la liste des soins à faire
		public Result jsonSoins(String query) {
			//System.out.println("ICI JSON");
			String lQuery = null == query ? "" : query;
			List<BasSoins> details = baseSoinServices.findLikeName(lQuery);
			HashMap<String, List<BasSoins>> response = new HashMap<>();
			response.put("option", details);
			return ok(toJson(response));
		}
	
	public Result jsonPartenaires() {
		//System.out.println("ICI JSON Partenaire");
		List<Partenaire> details = partenaireService.findAll();
		HashMap<String, List<Partenaire>> response = new HashMap<>();
		response.put("option", details);
		return ok(toJson(response));
	}
	
	public Result jsonConsParPartenaire() {
		//System.out.println("ICI JSON Partenaire");
		List<SConsultations> details = partenaireService.consultationParPArtenaire();
		//System.out.println("les eleamanets sont :"+details);
		HashMap<String, List<SConsultations>> response = new HashMap<>();
		response.put("option", details);
		//System.out.println("les eleamanets sont :"+response);
		return ok(toJson(response));
	}
}
