/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omega.lexicon.api;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author andrea
 */
@ApplicationPath("api")
public class LexiconApplication extends ResourceConfig {

    public LexiconApplication() {
        packages("it.cnr.ilc.lc.omega.lexicon.api");
    }
}
