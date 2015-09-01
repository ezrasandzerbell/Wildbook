package org.ecocean.rest;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.ShepherdPMF;
import org.ecocean.util.LogBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.samsix.database.Database;
import com.samsix.database.DatabaseException;

@RestController
@RequestMapping(value = "/data")
public class MainController
{
    private static Logger logger = LoggerFactory.getLogger(MainController.class);

    @RequestMapping(value = "/individual/get/{id}", method = RequestMethod.GET)
    public IndividualInfo getIndividual(@PathVariable("id")
                                        final int id) throws DatabaseException
    {
        try (Database db = ShepherdPMF.getDb()) {
            IndividualInfo indinfo = new IndividualInfo();

            indinfo.individual = SimpleFactory.getIndividual(db, id);
            if (indinfo.individual == null) {
                return null;
            }

            indinfo.photos = SimpleFactory.getIndividualPhotos(db, id);
            indinfo.encounters = SimpleFactory.getIndividualEncounters(db, indinfo.individual);
            indinfo.submitters = SimpleFactory.getIndividualSubmitters(db, id);

            return indinfo;
        }
    }


    @RequestMapping(value = "/user/get/{username}", method = RequestMethod.GET)
    public SimpleBeing getUser(@PathVariable("username")
                               final String username) throws DatabaseException
    {
        return SimpleFactory.getUser(username);
    }


    @RequestMapping(value = "/userinfo/get/{username}", method = RequestMethod.GET)
    public UserInfo getUserInfo(@PathVariable("username")
                                final String username) throws DatabaseException
    {
        return SimpleFactory.getUserInfo(username);
    }


    @RequestMapping(value = "/config/value/{var}", method = RequestMethod.GET)
    public String getConfigValue(final HttpServletRequest request,
            @PathVariable("var")
            final String var)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(LogBuilder.get()
                    .appendVar("var", var)
                    .appendVar("value", request.getServletContext().getInitParameter(var)).toString());
        }
        return request.getServletContext().getInitParameter(var);
    }

    public class IndividualInfo
    {
        public SimpleIndividual individual;
        public List<SimpleEncounter> encounters;
        public List<SimplePhoto> photos;
        public List<SimpleUser> submitters;
    }
}