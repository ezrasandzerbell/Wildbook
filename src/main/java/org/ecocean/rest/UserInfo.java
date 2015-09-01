package org.ecocean.rest;

import java.util.ArrayList;
import java.util.List;

import org.ecocean.survey.SurveyTrack;

public class UserInfo {
    private final SimpleBeing user;
    private int totalPhotoCount = 0;
    private final List<SimpleEncounter> encounters = new ArrayList<SimpleEncounter>();
    private final List<SimplePhoto> photos = new ArrayList<SimplePhoto>();
    private List<SimpleIndividual> individuals;
    private List<SurveyTrack> voyages;


    public UserInfo(final SimpleBeing user)
    {
        this.user = user;
    }

    public SimpleBeing getUser()
    {
        return user;
    }

    public List<SimpleEncounter> getEncounters() {
        return encounters;
    }

    public void addEncounter(final SimpleEncounter encounter)
    {
        encounters.add(encounter);
    }

    public List<SimplePhoto> getPhotos() {
        return photos;
    }

    public void addPhoto(final SimplePhoto photo) {
        if (photo == null) {
            return;
        }

        for (SimplePhoto foto : photos) {
            if (foto.getId() == photo.getId()) {
                // don't add the same photo twice
                return;
            }
        }

        photos.add(photo);
    }

    public int getTotalPhotoCount() {
        return totalPhotoCount;
    }

    public void setTotalPhotoCount(final int totalPhotoCount) {
        this.totalPhotoCount = totalPhotoCount;
    }

    public List<SimpleIndividual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(final List<SimpleIndividual> individuals) {
        this.individuals = individuals;
    }

    public List<SurveyTrack> getVoyages() {
        return voyages;
    }

    public void setVoyages(final List<SurveyTrack> voyages) {
        this.voyages = voyages;
    }

    public void addVoyage(final SurveyTrack voyage)
    {
        if (voyages == null) {
            voyages = new ArrayList<SurveyTrack>();
        }

        voyages.add(voyage);
    }
}