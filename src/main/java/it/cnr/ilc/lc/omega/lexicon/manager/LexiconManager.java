/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omega.lexicon.manager;

import it.cnr.ilc.lc.omega.lexicon.model.Lemma;
import it.cnr.ilc.lc.omega.lexicon.model.PropertyValue;
import it.cnr.ilc.lc.omega.lexicon.model.Sense;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 *
 * @author andrea
 */
public class LexiconManager {

    private final String LEMON_NS = "http://ditmao.ilc.cnr.it:8082/DitamoOntologies/lemon.rdf";
    private final String LEXINFO_NS = "http://ditmao.ilc.cnr.it:8082/DitamoOntologies/lexinfo.owl";
    private final String LEXICON_NS = "http://quamus.ilc.cnr.it/quamus";
    private final String ALQAMUS_LEMON_NS = "http://quamus.ilc.cnr.it/quamusLemon.owl";

    private OWLOntologyManager manager;
    private OWLOntology lexicon;
    private OWLDataFactory factory;
    private StructuralReasonerFactory reasonerFactory;
    private OWLReasoner reasoner;
    private OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();

    private PrefixManager pm;

    private static LexiconManager instance = new LexiconManager();

    public static LexiconManager getInstance() {
        return instance;
    }

    private LexiconManager() {
        manager = OWLManager.createOWLOntologyManager();        
        try (InputStream input = LexiconManager.class.getResourceAsStream("/quamus.owl")) {
            lexicon = manager.loadOntologyFromOntologyDocument(input);
            factory = manager.getOWLDataFactory();
            reasonerFactory = new StructuralReasonerFactory();
            reasoner = reasonerFactory.createReasoner(lexicon);
            setPrefixes();
        } catch (OWLOntologyCreationException | IOException ex) {
        }
    }

    private void setPrefixes() {
        pm = new DefaultPrefixManager();
        pm.setPrefix("lexicon", LEXICON_NS);
        pm.setPrefix("lemon", LEMON_NS);
        pm.setPrefix("lexinfo", LEXINFO_NS);
        pm.setPrefix("alqamuslemon", ALQAMUS_LEMON_NS);
    }

    // implements the rest /lemmas
    public List<Lemma> getLemmas() {
        List<Lemma> lemmas = new ArrayList<Lemma>();
        OWLClass Form = factory.getOWLClass(pm.getPrefix("lemon:") + "#Form");
        NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(Form, false);
        for (OWLNamedIndividual i : instances.getFlattened()) {
            if (isLemma(i.getIRI().toString())) {
                lemmas.add(getLemma(i));
            }
        }
        return lemmas;
    }

    // implements the rest /lemma/{writtenForm}
    public Lemma getLemma(String writtenForm) {
        OWLNamedIndividual lemmaInd = factory.getOWLNamedIndividual(pm.getPrefix("lexicon:") + "#" + writtenForm + "_ar_lemma");
        Lemma lemma = getLemma(lemmaInd);
        if (lemma.getUri() != null) {
            OWLNamedIndividual entryInd = factory.getOWLNamedIndividual(pm.getPrefix("lexicon:") + "#" + writtenForm + "_ar_entry");
            OWLObjectProperty senseProp = factory.getOWLObjectProperty(pm.getPrefix("lemon:") + "#sense");
            List<Sense> senses = new ArrayList();
            EntitySearcher.getObjectPropertyValues(entryInd, senseProp, lexicon).forEach(o -> {
                senses.add(getSense((OWLNamedIndividual) o));
            });
            lemma.setSenses(senses);
        }
        return lemma;
    }

    private Lemma getLemma(OWLNamedIndividual lemmaInd) {
        Lemma lemma = new Lemma();
        List<PropertyValue> pvl = getPropertyValues(lemmaInd);
        if (pvl.size() > 0) {
            lemma.setUri(lemmaInd.getIRI().toString());
            lemma.setShortForm(lemmaInd.getIRI().getShortForm());
            lemma.setPropertyValues(pvl);
        }
        return lemma;
    }

    private boolean isLemma(String lemma) {
        return lemma.contains("_lemma");
    }

    private List<PropertyValue> getPropertyValues(OWLNamedIndividual i) {
        List<PropertyValue> pvList = new ArrayList();
        for (OWLDataPropertyAssertionAxiom ax : lexicon.getDataPropertyAssertionAxioms(i)) {
            setPropertyValue(pvList, ax.getProperty().asOWLDataProperty().getIRI().getShortForm(), ax.getObject().getLiteral());
        }
        for (OWLObjectProperty objProp : lexicon.getObjectPropertiesInSignature()) {
            for (OWLNamedIndividual ind : reasoner.getObjectPropertyValues(i, objProp).getFlattened()) {
                setPropertyValue(pvList, renderer.render(objProp), renderer.render(ind));
            }
        }
        return pvList;
    }

    private void setPropertyValue(List<PropertyValue> l, String p, String v) {
        PropertyValue pv = new PropertyValue();
        pv.setProperty(p);
        pv.setValue(v);
        l.add(pv);
    }

    private Sense getSense(OWLNamedIndividual senseInd) {
        Sense sense = new Sense();
        sense.setUri(senseInd.getIRI().toString());
        sense.setShortForm(senseInd.getIRI().getShortForm());
        sense.setPropertyValues(getPropertyValues(senseInd));
        return sense;
    }

}
