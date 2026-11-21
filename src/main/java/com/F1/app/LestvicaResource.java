package com.F1.app;

import java.util.List;

import jakarta.inject.Inject;
import si.unilj.fri.prpo.api.LestvicaApi;
import si.unilj.fri.prpo.api.model.VnosLestvice;

public class LestvicaResource implements LestvicaApi {

    @Inject
    FantasyService service;

    @Override
    public List<VnosLestvice> getLestvica() {
        return service.getLestvica();
    }
}
