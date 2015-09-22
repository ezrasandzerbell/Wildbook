package org.ecocean.rest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.ecocean.email.EmailUtils;
import org.ecocean.media.LocalAssetStore;
import org.ecocean.media.MediaAsset;
import org.ecocean.media.MediaAssetFactory;
import org.ecocean.media.MediaSubmission;
import org.ecocean.security.User;
import org.ecocean.security.UserFactory;
import org.ecocean.servlet.ServletUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.samsix.database.Database;
import com.samsix.database.DatabaseException;
import com.samsix.database.RecordSet;
import com.samsix.database.SqlFormatter;
import com.samsix.database.SqlInsertFormatter;
import com.samsix.database.SqlUpdateFormatter;
import com.samsix.database.SqlWhereFormatter;
import com.samsix.database.Table;

import de.neuland.jade4j.exceptions.JadeException;

@RestController
@RequestMapping(value = "/obj/mediasubmission")
public class MediaSubmissionController
{
    private static Logger logger = LoggerFactory.getLogger(MediaSubmissionController.class);

    private void save(final Database db,
                      final MediaSubmission media)
        throws
            DatabaseException
    {
        Table table = db.getTable("mediasubmission");
        if (media.getId() == null) {
            SqlInsertFormatter formatter;
            formatter = new SqlInsertFormatter();
            fillFormatter(db, formatter, media);
            media.setId(table.insertSequencedRow(formatter, "id"));
        } else {
            SqlUpdateFormatter formatter;
            formatter = new SqlUpdateFormatter();
            formatter.append("id", media.getId());
            fillFormatter(db, formatter, media);
            SqlWhereFormatter where = new SqlWhereFormatter();
            where.append("id", media.getId());
            table.updateRow(formatter.getUpdateClause(), where.getWhereClause());
        }
    }


    private void fillFormatter(final Database db,
                               final SqlFormatter formatter,
                               final MediaSubmission media)
    {
        formatter.append("description", media.getDescription());
        formatter.append("email", media.getEmail());
        formatter.append("endtime", media.getEndTime());
        formatter.append("latitude", media.getLatitude());
        formatter.append("longitude", media.getLongitude());
        formatter.append("name", media.getName());
        formatter.append("starttime", media.getStartTime());
        formatter.append("submissionid", media.getSubmissionid());
        formatter.append("timesubmitted", media.getTimeSubmitted());
        if (media.getUser() != null) {
            formatter.append("userid", media.getUser().getId());
        }
        formatter.append("verbatimlocation", media.getVerbatimLocation());
        formatter.append("status", media.getStatus());
    }


    private List<MediaAsset> getMediaSubmissionMedia(final HttpServletRequest request,
                                                     final long msid) throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            String sql = "SELECT ma.* FROM mediasubmission_media m"
                    + " INNER JOIN mediaasset ma ON ma.id = m.mediaid"
                    + " WHERE m.mediasubmissionid = " + msid;
            RecordSet rs = db.getRecordSet(sql);
            List<MediaAsset> media = new ArrayList<>();
            while (rs.next()) {
                media.add(MediaAssetFactory.valueOf(rs));
            }

            return media;
        }
    }


    private static MediaSubmission readMediaSubmission(final RecordSet rs) throws DatabaseException
    {
        MediaSubmission ms = new MediaSubmission();
        ms.setDescription(rs.getString("description"));
        ms.setEmail(rs.getString("email"));
        ms.setEndTime(rs.getLongObj("endtime"));
        ms.setId(rs.getInteger("id"));
        ms.setLatitude(rs.getDoubleObj("latitude"));
        ms.setLongitude(rs.getDoubleObj("longitude"));
        ms.setName(rs.getString("name"));
        ms.setStartTime(rs.getLongObj("starttime"));
        ms.setSubmissionid(rs.getString("submissionid"));
        ms.setTimeSubmitted(rs.getLongObj("timesubmitted"));
        ms.setUser(UserFactory.readSimpleUser(rs));
        ms.setVerbatimLocation(rs.getString("verbatimlocation"));
        ms.setStatus(rs.getString("status"));

        return ms;
    }


    private List<MediaSubmission> get(final Database db,
                                      final SqlWhereFormatter where) throws DatabaseException
    {
        String whereClause = where.getWhereClause();
        List<MediaSubmission> mss = new ArrayList<MediaSubmission>();
        String sql = "SELECT * FROM mediasubmission ms"
                + " LEFT OUTER JOIN users u on u.userid = ms.userid"
                + " LEFT OUTER JOIN mediaasset ma ON ma.id = u.avatarid";
        if (! StringUtils.isBlank(whereClause)) {
            sql += " WHERE " + whereClause;
        }
        sql += " ORDER BY timesubmitted desc";

        RecordSet rs = db.getRecordSet(sql);
        while (rs.next()) {
            mss.add(readMediaSubmission(rs));
        }

        return mss;
    }


//    private final UserService userService;
//
//    @Inject
//    public UserController(final UserService userService) {
//        this.userService = userService;
//    }



    @RequestMapping(value = "/photos/{submissionid}", method = RequestMethod.GET)
    public static List<SimplePhoto> getPhotos(final HttpServletRequest request,
                                              @PathVariable("submissionid")
                                              final String submissionid) throws DatabaseException
    {
        //
        // Add photos
        //
        try (Database db = ServletUtilities.getDb(request)) {
            String sql = "SELECT ma.* FROM mediasubmission_media m"
                    + " INNER JOIN mediaasset ma ON ma.id = m.mediaid"
                    + " WHERE m.mediasubmissionid = " + submissionid;
            RecordSet rs = db.getRecordSet(sql);
            List<SimplePhoto> photos = new ArrayList<SimplePhoto>();
            while (rs.next()) {
                photos.add(SimpleFactory.readPhoto(rs));
            }

            return photos;
        }
    }


//    @RequestMapping(value = "/get/id/{mediaid}", method = RequestMethod.GET)
//    public MediaSubmission get(final HttpServletRequest request,
//                               @PathVariable("mediaid")
//                               final long mediaid)
//        throws DatabaseException
//    {
//        return getMediaSubmission(mediaid);
//    }


    @RequestMapping(value = "/get/status", method = RequestMethod.GET)
    public List<MediaSubmission> getStatus(final HttpServletRequest request)
        throws DatabaseException
    {
        return getStatus(request, null);
    }


    @RequestMapping(value = "/get/status/{status}", method = RequestMethod.GET)
    public List<MediaSubmission> getStatus(final HttpServletRequest request,
                                           @PathVariable("status")
                                           final String status)
        throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            SqlWhereFormatter where = new SqlWhereFormatter();
            // * will mean get all, so we just have an empty where formatter
            // we want all other values, included null, to pass to the append method
            if (! "*".equals(status)) {
                where.append("ms.status", status);
            }
            return get(db, where);
        }
    }

//    @RequestMapping(value = "/get/sources/{id}", method = RequestMethod.GET)
//    public List<MediaSubmission> getSources(final HttpServletRequest request,
//                                            @PathVariable("id") final int id) throws DatabaseException {
//        if (id < 1) return null;
//
//        try (Database db = new Database(ShepherdPMF.getConnectionInfo())) {
//            String sql = "SELECT \"DATACOLLECTIONEVENTID_EID\" AS mid FROM \"SURVEYTRACK_MEDIA\" WHERE \"ID_OID\"=" + id;
//            RecordSet rs = db.getRecordSet(sql);
//            List<SinglePhotoVideo> media = new ArrayList<SinglePhotoVideo>();
//            while (rs.next()) {
//                SinglePhotoVideo spv = new SinglePhotoVideo();
//                spv.setDataCollectionEventID(rs.getString("mid"));
//                media.add(spv);
//            }
//
//            return findMediaSources(media, ServletUtilities.getContext(request));
//        }
//    }
//
//    @RequestMapping(value = "/get/sources", method = RequestMethod.POST)
//    public List<MediaSubmission> getSources(final HttpServletRequest request,
//                                            @RequestBody final List<SinglePhotoVideo> media) throws DatabaseException {
//        return findMediaSources(media, ServletUtilities.getContext(request));
//    }

    public static void deleteMedia(final Database db,
                                   final int submissionid,
                                   final int mediaid) throws DatabaseException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting mediaid [" + mediaid + "] from submission [" + submissionid + "]");
        }

        MediaAsset ma = MediaAssetFactory.load(db, mediaid);

        if (ma == null) {
            throw new IllegalArgumentException("No media with id [" + mediaid + "] found.");
        }

        Table table = db.getTable("mediasubmission_media");
        String where = "mediasubmissionid = "
                + submissionid
                + " AND mediaid = "
                + mediaid;
        table.deleteRows(where);

        MediaAssetFactory.delete(db, ma.getID());
        MediaAssetFactory.deleteFromStore(ma);
    }

    @RequestMapping(value = "/deletemedia", method = RequestMethod.POST)
    public void deleteMedia(final HttpServletRequest request,
                            @RequestBody final MSMEntry msm)
        throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            deleteMedia(db, msm.submissionid, msm.mediaid);
        }
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public void deleteSubmission(final HttpServletRequest request,
                                 @RequestBody final MSMEntry msm)
        throws DatabaseException, IOException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            String mWhere = "mediasubmissionid = " + msm.submissionid;

            String sql = "SELECT ma.* FROM mediaasset ma"
                    + " INNER JOIN mediasubmission_media msm ON msm.mediaid = ma.id"
                    + " WHERE " + mWhere;
            List<MediaAsset> media = new ArrayList<>();
            RecordSet rs = db.getRecordSet(sql);
            while (rs.next()) {
                media.add(MediaAssetFactory.valueOf(rs));
            }

            db.beginTransaction();

            //
            // Delete all the joins
            //
            Table table = db.getTable("mediasubmission_media");
            table.deleteRows(mWhere);

            //
            // Delete the media submission itself.
            //
            table = db.getTable("mediasubmission");
            table.deleteRows("id = " + msm.submissionid);

            //
            // Delete all the files.
            //
            for (MediaAsset aMedia : media) {
                MediaAssetFactory.delete(db, aMedia.getID());
            }

            db.commitTransaction();

            //
            // Now delete the physical files.
            //
            for (MediaAsset aMedia : media) {
                MediaAssetFactory.deleteFromStore(aMedia);
            }
        }
    }


    @RequestMapping(value = "/complete", method = RequestMethod.POST)
    public void complete(final HttpServletRequest request,
                         @RequestBody final MediaSubmission media)
        throws DatabaseException
    {
        try (Database db = ServletUtilities.getDb(request)) {
            save(db, media);

            SimpleUser user = media.getUser();

            String email;
            if (user != null) {
                User wbUser = UserFactory.getUserById(db, user.getId());
                if (wbUser == null) {
                    logger.warn("curious: unable to load a user with id [" + user.getId() + "]");
                    email = null;
                } else {
                    email = wbUser.getEmail();
                }
            } else {
                email = media.getEmail();
            }

            //
            // Email notify admin of new mediasubmission in WIldbook
            //
            Map<String, Object> model = new HashMap<>();
            model.put("submission", media);

            try {
                EmailUtils.sendJadeTemplate(EmailUtils.getAdminSender(),
                                            EmailUtils.getAdminRecipients(),
                                            "submission/newadmin",
                                            model);
            } catch (JadeException | IOException | MessagingException ex) {
                logger.error("Trouble sending admin email", ex);
            }

            if (email != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("sending thankyou email to:" + email);
                }
                try {
                    EmailUtils.sendJadeTemplate(EmailUtils.getAdminSender(),
                                                email,
                                                "submission/newthankyou",
                                                model);
                } catch (JadeException | IOException | MessagingException ex) {
                    logger.error("Trouble sending thank you email to [" + email + "]", ex);
                }
            }


            //
            // Now finally remove the files from the users session object so that
            // they can submit again with a fresh set.
            //
            MediaUploadServlet.clearFileSet(request.getSession(), media.getSubmissionid());
        }
    }


    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public Integer save(final HttpServletRequest request,
                        @RequestBody final MediaSubmission media)
        throws DatabaseException
    {
        if (logger.isDebugEnabled()) {
            logger.debug("Calling MediaSubmission save for media [" + media + "]");
        }

        try (Database db = ServletUtilities.getDb(request)){
            //
            // Save media submission
            //
//            boolean isNew = (media.getId() == null);
            save(db, media);

            //
            // TODO: This code works as is EXCEPT due to the stupid IDX ordering column
            // that DataNucleus put on our SURVEY_MEDIA table along with making SURVEY_ID_OID/IDX being
            // the primary key (Why?!!!) we can't just add 0 for the IDX column. Sheesh.
            // Recreate the SURVEY_MEDIA table the way you want it and fix this later.
            //
//            if (isNew) {
//                //
//                // Check if the media submissionId matches a survey and if so
//                // insert into SURVEY_MEDIA table.
//                // TODO: Add a parameter to the save method to indicate that this
//                // media submission was intended for a survey so that we know if
//                // we should be doing this or something else with the submissionId.
//                //
//                RecordSet rs;
//                SqlWhereFormatter where = new SqlWhereFormatter();
//                where.append(SurveyFactory.PK_SURVEY), media.getSubmissionid());
//
//                rs = db.getTable("SURVEY").getRecordSet(where.getWhereClause());
//                if (rs.next()) {
//                    SqlInsertFormatter formatter;
//                    formatter = new SqlInsertFormatter();
//                    formatter.append("SURVEY_ID_OID"), rs.getInteger("SURVEY_ID"));
//                    formatter.append("ID_EID"), media.getId());
//                    formatter.append("IDX"), 0);
//                    db.getTable("SURVEY_MEDIA").insertRow(formatter);
//                }
//            }

            if (logger.isDebugEnabled()) {
                logger.debug("Returning media submission id [" + media.getId() + "]");
            }

            return media.getId();
        }

//        PersistenceManager pm = myShepherd.getPM();
////        MediaSubmission ms;
////        if (media.getId() != null) {
////            ms = ((MediaSubmission) (pm.getObjectById(pm.newObjectIdInstance(MediaSubmission.class, media.getId()), true)));
////            //
////            // TODO: Switch to regular SQL!!!
////            // Now I would have to go through all of the properties on the media and set
////            // them to the current one?! WTF.
////            //
////        } else {
////            ms = media;
////        }
//
//        if (media.getSubmissionid() != null) {
////            survey = ((Survey) (pm.getObjectById(pm.newObjectIdInstance(Survey.class, media.getSubmissionid()), true)));
//            Query query = pm.newQuery(
//                    "SELECT FROM \"SURVEY\" WHERE \"SURVEYID\" == '" + media.getSubmissionid() + "'");
//                @SuppressWarnings("unchecked")
//                List<Survey> results = (List<Survey>)query.execute();
//                if (results.size() > 0) {
//                    Survey survey = results.get(0);
//                    survey.getMedia().add(media);
//                    pm.makePersistent(survey);
//                } else {
//                    pm.makePersistent(media);
//                }
//        } else {
//            pm.makePersistent(media);
//        }
////        myShepherd.beginDBTransaction();
////        myShepherd.getPM().makePersistent(media);
////        myShepherd.commitDBTransaction();
    }


    @RequestMapping(value = "/delfile/{msid}", method = RequestMethod.POST)
    public void delFile(final HttpServletRequest request,
                        @PathVariable("msid")
                        final int msid,
                        @RequestBody final String filename) throws DatabaseException
    {
        MediaUploadServlet.deleteFileFromSet(request, msid, filename);
    }


    @RequestMapping(value = "/getexif/{msid}", method = RequestMethod.GET)
    public ExifData getExif(final HttpServletRequest request,
                            @PathVariable("msid")
                            final long msid)
        throws DatabaseException
    {
        List<MediaAsset> media = getMediaSubmissionMedia(request, msid);

        ExifData data = new ExifData();
        ExifAvg avg = data.avg;

        double latitude = 0;
        int latCount = 0;
        double longitude = 0;
        int longCount = 0;

        for (MediaAsset ma : media) {
            if (! (ma.getStore() instanceof LocalAssetStore)) {
                continue;
            }

            LocalAssetStore store = (LocalAssetStore) ma.getStore();

            File file = store.getFile(ma.getPath());
            int index = file.getAbsolutePath().lastIndexOf('.');
            if (index < 0) {
                continue;
            }

            switch (file.getAbsolutePath().substring(index+1).toLowerCase()) {
                case "jpg":
                case "jpeg":
                case "png":
                case "tiff":
                case "arw":
                case "nef":
                case "cr2":
                case "orf":
                case "rw2":
                case "rwl":
                case "srw":
                {
                    if (file.exists()) {
                        Metadata metadata;
                        try {
                            ExifItem item = new ExifItem();
                            item.mediaid = ma.getID();
                            data.items.add(item);

                            metadata = ImageMetadataReader.readMetadata(file);
                         // obtain the Exif directory
                            ExifSubIFDDirectory directory = null;
                            directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                            // query the tag's value
                            Date date = null;
                            if (directory != null) date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                            if (date != null) {
                                item.time = date.getTime();

                                if (avg.minTime == null) {
                                    avg.minTime = date.getTime();
                                } else {
                                    if (date.getTime() < avg.minTime) {
                                        avg.minTime = date.getTime();
                                    }
                                }
                                if (avg.maxTime == null) {
                                    avg.maxTime = date.getTime();
                                } else {
                                    if (date.getTime() > avg.maxTime) {
                                        avg.maxTime = date.getTime();
                                    }
                                }
                            }

                            // See whether it has GPS data
                            Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
                            if (gpsDirectories == null) {
                                continue;
                            }
                            for (GpsDirectory gpsDirectory : gpsDirectories) {
                                // Try to read out the location, making sure it's non-zero
                                GeoLocation geoLocation = gpsDirectory.getGeoLocation();
                                if (geoLocation != null && !geoLocation.isZero()) {
                                    item.latitude = geoLocation.getLatitude();
                                    item.longitude = geoLocation.getLongitude();

                                    latitude += geoLocation.getLatitude();
                                    latCount += 1;
                                    longitude += geoLocation.getLongitude();
                                    longCount += 1;
                                }
                            }

//                            // iterate through metadata directories
//                            List<Tag> list = new ArrayList<Tag>();
//                            for (Directory dir : metadata.getDirectories()) {
//                                if ("exif".equals(dir.getName().toLowerCase()) {
//                                    dir.
//                                }
//
//                                for (Tag tag : dir.getTags()) {
//                                    if (!tag.getTagName().toUpperCase(Locale.US).startsWith("GPS"))
//                                        list.add(tag);
//                                }
//                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        if (latCount > 0) {
            avg.latitude = latitude / latCount;
            avg.longitude = longitude / longCount;
        }

        return data;
    }


    public static class MSMEntry
    {
        public int submissionid;
        public int mediaid;
    }

    public static class ExifItem
    {
        public Long time;
        public Double latitude;
        public Double longitude;
        public int mediaid;
    }

    public static class ExifAvg
    {
        public Long minTime;
        public Long maxTime;
        public Double latitude;
        public Double longitude;
    }

    public static class ExifData
    {
        public List<ExifItem> items = new ArrayList<ExifItem>();
        public ExifAvg avg = new ExifAvg();
    }
}