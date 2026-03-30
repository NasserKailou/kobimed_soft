/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.stock;

import java.util.List;

import models.stock.tables.pojos.*;
import models.stock.tables.pojos.VMouvement;
/**
 * 
 * @author nasser
 *
 */
public interface IMouvement {
    public String saveLogical(Mouvement ma, boolean b);
    public Mouvement findMouvementById(Long id);
    public List<Mouvement> findMouvementByUser(String user, String op);
    public VMouvement findMouvementByIdV(Long id);
    public List<VMouvement> findMouvementByUserV(String user, String op);
    public String deleteAllMouvementByUser(String user, String operation, boolean isRejet);
    public String deleteMouvementById(Long id);
    public String validerOperation(String login, String op, Long sommeRecue, Integer idFournisseur,Integer idService);
    public List<VMouvement> findMouvementByUserVV(String user);
    public List<VMouvement> findMouvementAll();
    public List<Facture> getFactureByUser(String user, String op);
    public List<Facture> getAllFacture();
    public List<Facture> getAllFactureByOperation( String op);
    public List<VMouvement> findMouvementByIdFacture(Long factureId);
    public String viderReport(String user);
    public String mouvementToReport(List<VMouvement> vm);
    public List<VFactureConsulster> getFactureConsulter(String login, String operation, boolean isRejet);
    public List<VMouvement> getMouvementByIdFacture(Long idFacture);
    public VFactureConsulster getFactureConsulter(Long idFacture);
    public boolean eclater(VArticle ba);
    public List<VAchat> getAllAchat();
    public List<VVente> getAllVente();
    public List<VRejet> getAllRejet();
    public List<VArticle> getDispoBaisse();
    public Confi getConf(Long id);
    public List<Fournisseur> getAllFournisseur();
    public List<Services> getAllServices();

}
