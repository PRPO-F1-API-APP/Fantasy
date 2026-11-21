package com.F1.app;

import java.util.List;

import jakarta.inject.Inject;
import si.unilj.fri.prpo.api.IgralciApi;
import si.unilj.fri.prpo.api.model.Igralec;

public class IgralciResource implements IgralciApi {

    @Inject
    FantasyService service;

    @Override
    public Igralec createIgralec(Igralec igralec) {
        return service.createIgralec(igralec);
    }

    @Override
    public List<Igralec> getAllIgralci() {
        return service.getAllIgralci();
    }

    @Override
    public Igralec getIgralec(Long igralecId) {
        return service.getIgralec(igralecId);
    }
}
