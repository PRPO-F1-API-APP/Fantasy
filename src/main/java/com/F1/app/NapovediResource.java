package com.F1.app;

import java.util.List;

import jakarta.inject.Inject;
import si.unilj.fri.prpo.api.NapovediApi;
import si.unilj.fri.prpo.api.model.Napoved;

public class NapovediResource implements NapovediApi {

    @Inject
    FantasyService service;

    @Override
    public Napoved createNapoved(String dirkaId, Napoved napoved) {
        return service.createNapoved(dirkaId, napoved);
    }

    @Override
    public List<Napoved> getNapovediZaDirko(String dirkaId) {
        return service.getNapovediZaDirko(dirkaId);
    }
}
