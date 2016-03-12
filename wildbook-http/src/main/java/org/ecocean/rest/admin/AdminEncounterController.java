package org.ecocean.rest.admin;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.ecocean.encounter.Encounter;
import org.ecocean.encounter.EncounterFactory;
import org.ecocean.servlet.ServletUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/admin/api/encounter")
public class AdminEncounterController {
    @RequestMapping(value = "save", method = RequestMethod.POST)
    public EncounterSaveResult saveEncounter(final HttpServletRequest request,
                                             @RequestBody @Valid final Encounter encounter) throws DatabaseException {
        if (encounter == null) {
            return null;
        }

        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                EncounterFactory.saveEncounter(db, encounter);
            });
            EncounterSaveResult results = new EncounterSaveResult();
            results.encounterid = encounter.getId();
            results.individualid = encounter.getIndividual().getId();
            return results;
        }
    }

    @RequestMapping(value = "addmedia/{id}", method = RequestMethod.POST)
    public void addMedia(final HttpServletRequest request,
                         @PathVariable("id") final int encounterid,
                         @RequestBody @Valid final Integer[] mediaids) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int ii=0; ii < mediaids.length; ii++) {
                    EncounterFactory.addMedia(db, encounterid, mediaids[ii]);
                }
            });
        }
    }

    @RequestMapping(value = "detachmedia/{id}", method = RequestMethod.POST)
    public void detacchMedia(final HttpServletRequest request,
                             @PathVariable("id") final int encounterid,
                             @RequestBody @Valid final Integer[] mediaids) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            db.performTransaction(() -> {
                for (int ii=0; ii < mediaids.length; ii++) {
                    EncounterFactory.detachMedia(db, encounterid, mediaids[ii]);
                }
            });
        }
    }

    @RequestMapping(value = "delete", method = RequestMethod.POST)
    public void deleteEncounter(final HttpServletRequest request,
                                @RequestBody final Encounter encounter) throws DatabaseException {
        try (Database db = ServletUtils.getDb(request)) {
            EncounterFactory.deleteEncounter(db, encounter.getId());
        }
    }

    static class EncounterSaveResult {
        public int individualid;
        public int encounterid;
    }
}