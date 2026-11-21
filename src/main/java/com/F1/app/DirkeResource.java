package com.F1.app;

import java.util.List;

import jakarta.inject.Inject;
import si.unilj.fri.prpo.api.DirkeApi;
import si.unilj.fri.prpo.api.model.RezultatDirke;
import si.unilj.fri.prpo.api.model.VnosLestvice;

public class DirkeResource implements DirkeApi {

    @Inject
    FantasyService service;

    @Override
    public List<VnosLestvice> vnesiRezultat(String dirkaId, RezultatDirke rezultatDirke) {
        return service.vnesiRezultat(dirkaId, rezultatDirke);
    }
}
