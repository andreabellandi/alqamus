/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.lc.omega.lexicon.model;

import java.util.List;

/**
 *
 * @author andrea
 */
public class Sense {

    private String uri;
    private String shortForm;
    private List<PropertyValue> propertyValues;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getShortForm() {
        return shortForm;
    }

    public void setShortForm(String shortForm) {
        this.shortForm = shortForm;
    }

    public List<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }

}
