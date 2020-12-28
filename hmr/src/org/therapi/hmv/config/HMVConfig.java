package org.therapi.hmv.config;

import org.therapi.hmv.dao.DiagnosegruppeDao;
import org.therapi.hmv.entities.Diagnosegruppe;

import hmv.Context;

public class HMVConfig {

    public  void load(Context context) {
        Diagnosegruppe.setContent(new DiagnosegruppeDao(context.mandant.ik()).all());
    }

}
