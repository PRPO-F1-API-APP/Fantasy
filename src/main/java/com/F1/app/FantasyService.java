package com.F1.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import si.unilj.fri.prpo.api.model.Igralec;
import si.unilj.fri.prpo.api.model.Napoved;
import si.unilj.fri.prpo.api.model.RezultatDirke;
import si.unilj.fri.prpo.api.model.VnosLestvice;

@ApplicationScoped
public class FantasyService {

    private final Map<Long, Igralec> igralci = new LinkedHashMap<>();        // id -> igralec
    private final Map<String, List<Napoved>> napovedi = new HashMap<>();    // dirkaId -> napovedi
    private final Map<Long, Integer> tockeIgralcev = new HashMap<>();       // igralecId -> skupne točke
    private final Set<String> koncaneDirke = new HashSet<>();
    private long naslednjiIgralecId = 1;
    private long naslednjaNapovedId = 1;

    // ---------- Igralci ----------

    public synchronized Igralec createIgralec(Igralec igralec) {
        igralec.setId(naslednjiIgralecId++);
        igralci.put(igralec.getId(), igralec);
        tockeIgralcev.put(igralec.getId(), 0);
        return igralec;
    }

    public synchronized List<Igralec> getAllIgralci() {
        return new ArrayList<>(igralci.values());
    }

    public synchronized Igralec getIgralec(Long id) {
        Igralec igralec = igralci.get(id);
        if (igralec == null) {
            throw new NotFoundException("Igralec " + id + " ne obstaja");
        }
        return igralec;
    }

    // ---------- Napovedi ----------

    public synchronized Napoved createNapoved(String dirkaId, Napoved napoved) {
        getIgralec(napoved.getIgralecId());                      // 404, če igralec ne obstaja
        preveriRazlicne(napoved.getP1(), napoved.getP2(), napoved.getP3());

        if (koncaneDirke.contains(dirkaId)) {
            throw new WebApplicationException("Dirka " + dirkaId + " je že končana", 409);
        }
        List<Napoved> zaDirko = napovedi.computeIfAbsent(dirkaId, k -> new ArrayList<>());
        boolean zeOddal = zaDirko.stream()
                .anyMatch(n -> n.getIgralecId().equals(napoved.getIgralecId()));
        if (zeOddal) {
            throw new WebApplicationException("Igralec je za to dirko že oddal napoved", 409);
        }

        napoved.setId(naslednjaNapovedId++);
        napoved.setDirkaId(dirkaId);
        napoved.setTocke(null);
        zaDirko.add(napoved);
        return napoved;
    }

    public synchronized List<Napoved> getNapovediZaDirko(String dirkaId) {
        return new ArrayList<>(napovedi.getOrDefault(dirkaId, List.of()));
    }

    // ---------- Rezultat dirke ----------

    public synchronized List<VnosLestvice> vnesiRezultat(String dirkaId, RezultatDirke rezultat) {
        preveriRazlicne(rezultat.getP1(), rezultat.getP2(), rezultat.getP3());
        if (!koncaneDirke.add(dirkaId)) {
            throw new WebApplicationException("Rezultat za " + dirkaId + " je že vnesen", 409);
        }

        List<String> top3 = List.of(rezultat.getP1(), rezultat.getP2(), rezultat.getP3());
        for (Napoved napoved : napovedi.getOrDefault(dirkaId, List.of())) {
            int tocke = izracunajTocke(napoved, top3);
            napoved.setTocke(tocke);
            tockeIgralcev.merge(napoved.getIgralecId(), tocke, Integer::sum);   // prištej k skupnim
        }
        return getLestvica();
    }

    // Pravo mesto = 10, v top 3 na napačnem mestu = 5, podvojeno mesto = x2
    private int izracunajTocke(Napoved napoved, List<String> top3) {
        List<String> napovedano = List.of(napoved.getP1(), napoved.getP2(), napoved.getP3());
        int skupaj = 0;
        for (int i = 0; i < 3; i++) {
            String dirkac = napovedano.get(i);
            int tocke = 0;
            if (dirkac.equals(top3.get(i))) {
                tocke = 10;
            } else if (top3.contains(dirkac)) {
                tocke = 5;
            }
            // values()[0] = P1, [1] = P2, [2] = P3
            if (napoved.getDvojnoMesto() == Napoved.DvojnoMestoEnum.values()[i]) {
                tocke *= 2;
            }
            skupaj += tocke;
        }
        return skupaj;
    }

    private void preveriRazlicne(String p1, String p2, String p3) {
        if (p1.equals(p2) || p1.equals(p3) || p2.equals(p3)) {
            throw new BadRequestException("Isti dirkač je naveden večkrat");
        }
    }

    // ---------- Lestvica ----------

    public synchronized List<VnosLestvice> getLestvica() {
        List<Map.Entry<Long, Integer>> urejeno = new ArrayList<>(tockeIgralcev.entrySet());
        urejeno.sort(Map.Entry.<Long, Integer>comparingByValue().reversed());

        List<VnosLestvice> lestvica = new ArrayList<>();
        int mesto = 1;
        for (Map.Entry<Long, Integer> e : urejeno) {
            VnosLestvice vnos = new VnosLestvice();
            vnos.setMesto(mesto++);
            vnos.setIgralecId(e.getKey());
            vnos.setUporabniskoIme(igralci.get(e.getKey()).getUporabniskoIme());
            vnos.setTocke(e.getValue());
            lestvica.add(vnos);
        }
        return lestvica;
    }
}
