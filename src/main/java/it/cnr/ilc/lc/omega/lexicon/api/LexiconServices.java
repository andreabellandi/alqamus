/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omega.lexicon.api;

import it.cnr.ilc.lc.omega.lexicon.model.Lemma;
import it.cnr.ilc.lc.omega.lexicon.manager.LexiconManager;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 * @author andrea
 */
@Path("lexicon")
public class LexiconServices {
    
    @GET
    @Path("/lemmas")
    public List<Lemma> list() {
        return LexiconManager.getInstance().getLemmas();
    }
    
    @GET
    @Path("/lemma/{writtenForm}")
    public Lemma get(@PathParam("writtenForm") String writtenForm) {
        Lemma lemma = LexiconManager.getInstance().getLemma(writtenForm);
        return lemma;
    }
    
}
