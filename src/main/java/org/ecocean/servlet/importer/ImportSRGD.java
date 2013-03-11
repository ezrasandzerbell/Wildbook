/*
 * The Shepherd Project - A Mark-Recapture Framework
 * Copyright (C) 2011 Jason Holmberg
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ecocean.servlet.importer;

import com.oreilly.servlet.multipart.*;
import org.ecocean.*;
import org.ecocean.servlet.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import au.com.bytecode.opencsv.CSVReader;
import java.util.List;
import org.joda.time.*;
import org.joda.time.format.*;
import java.lang.IllegalArgumentException;

/**
 * Uploads an SRGD CSV file for data import
 *
 * @author jholmber
 */
public class ImportSRGD extends HttpServlet {

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Shepherd myShepherd = new Shepherd();

    System.out.println("\n\nStarting ImportSRGD servlet...");
    
    //setup data dir
    String rootWebappPath = getServletContext().getRealPath("/");
    File webappsDir = new File(rootWebappPath).getParentFile();
    File shepherdDataDir = new File(webappsDir, CommonConfiguration.getDataDirectoryName());
    if(!shepherdDataDir.exists()){shepherdDataDir.mkdir();}
    File tempSubdir = new File(webappsDir, "temp");
    if(!tempSubdir.exists()){tempSubdir.mkdir();}
    System.out.println("\n\n     Finished directory creation...");
    
    
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

    String fileName = "None";
    
    StringBuffer messages=new StringBuffer();
    
    boolean successfullyWroteFile=false;

    File finalFile=new File(tempSubdir,"temp.csv");
    
    try {
      MultipartParser mp = new MultipartParser(request, (CommonConfiguration.getMaxMediaSizeInMegabytes() * 1048576));
      Part part;
      while ((part = mp.readNextPart()) != null) {
        String name = part.getName();
        if (part.isParam()) {


          // it's a parameter part
          ParamPart paramPart = (ParamPart) part;
          String value = paramPart.getStringValue();


        }


        if (part.isFile()) {
          FilePart filePart = (FilePart) part;
          fileName = ServletUtilities.cleanFileName(filePart.getFileName());
          if (fileName != null) {
            System.out.println("     Trying to upload file: "+fileName);
            //File thisSharkDir = new File(encountersDir.getAbsolutePath() +"/"+ encounterNumber);
            //if(!thisSharkDir.exists()){thisSharkDir.mkdir();}
            finalFile=new File(tempSubdir, fileName);
            filePart.writeTo(finalFile);
            successfullyWroteFile=true;
            System.out.println("\n\n     I successfully uploaded the file!");
          }
        }
      }


      

        try {
          if(successfullyWroteFile){
            
            System.out.println("\n\n     Starting CSV content import...");
            
            //OK, we have our CSV file
            //let's import
            CSVReader reader = new CSVReader(new FileReader(finalFile));
            List<String[]> allLines = reader.readAll();
            System.out.println("\n\n     Read in the CSV file!");
            
            //let's detect the size of this array by reading the number of header columns in row 0
            String[] headerNames=allLines.get(0);
            int numColumns = headerNames.length;
            int numRows = allLines.size();
           
            
            for(int i=1;i<numRows;i++){
              
              System.out.println("\n\n     Processing row "+i);
              boolean newEncounter=true;
              boolean newShark=true;
              String[] line=allLines.get(i);
              
              boolean ok2import=true;
              
              Encounter enc=new Encounter();
              
              myShepherd.beginDBTransaction();
              
              //line[0] is the sample_ID
              String encNumber=line[0].trim();
              if(!encNumber.equals("")){
                if(myShepherd.isEncounter(encNumber)){
                  enc=myShepherd.getEncounter(encNumber);
                  newEncounter=false;
                }
                else{
                  enc.setCatalogNumber(encNumber);
                  enc.setState("approved");
                }
              }
              else{
                ok2import=false;
                messages.append("<li>Could not find sample/encounter ID in the first column of row "+i+".</li>");
                System.out.println("          Could not find sample/encounter ID in the first column of row "+i+".");
              }
              
              //line[1] is the IndividualID
              String individualID=line[1].trim();
              if(!individualID.equals("")){
                enc.setIndividualID(individualID);
                System.out.println("          Setting Individual ID for row "+i+". Value: "+individualID);
                
              }
              
              //line[2] is the latitude
              String latitude=line[2].trim();
              if(!latitude.equals("")){
                try{
                  Double lat=new Double(latitude);
                  enc.setDecimalLatitude(lat);
                  System.out.println("          Setting latitude for row "+i+". Value: "+latitude);
                  
                }
                catch(NumberFormatException nfe){
                  messages.append("<li>"+enc.getCatalogNumber()+": Latitude hit a NumberFormatException in row "+i+" and could not be imported. The listed value was: "+latitude+"</li>");
                }
              }
              
              //line[3] is the latitude
              String longitude=line[3].trim();
              if(!longitude.equals("")){
                try{
                  Double longie=new Double(longitude);
                  enc.setDecimalLatitude(longie);
                  System.out.println("          Setting longitude for row "+i+". Value: "+longitude);
                  
                }
                catch(NumberFormatException nfe){
                  nfe.printStackTrace();
                  messages.append("<li>"+enc.getCatalogNumber()+": Longitude hit a NumberFormatException in row "+i+" and could not be imported. The listed value was: "+longitude+"</li>");
                }
              }
              
              //line[4] is the date_time
              String isoDate=line[4].trim();
              if(!isoDate.equals("")){
                DateTimeFormatter parser2 = ISODateTimeFormat.dateTimeParser();
                
                try{
                  DateTime time = parser2.parseDateTime(isoDate);
                  enc.setYear(time.getYear());
                  enc.setMonth(time.getMonthOfYear());
                  enc.setDay(time.getDayOfMonth());
                  
                  
                  int minutes=time.getMinuteOfHour();
                  
                  
                  String minutes2=(new Integer(minutes)).toString();
                  
                  if((time.getHourOfDay()!=0)&&(minutes!=0)){
                    enc.setHour(time.getHourOfDay());
                    enc.setMinutes(minutes2);
                  }
                  
                  
                  System.out.println("          Set date for encounter: "+enc.getDate());
                  
                }
                catch(IllegalArgumentException iae){
                  iae.printStackTrace();
                  messages.append("<li>Could not import the date and time for row: "+i+". Cancelling the import for this row.</li>");
                  ok2import=false;
                  
                }
              }
              
              //line[5] get locationID
              String locationID=line[5].trim();
              if(!locationID.equals("")){
                enc.setLocationID(locationID);
                System.out.println("          Setting location ID for row "+i+". Value: "+locationID);
              }
              
              //line[6] get sex
              String sex=line[6].trim();
              if(!sex.equals("")){
                
                if(sex.equals("M")){enc.setSex("male");}
                else if(sex.equals("F")){enc.setSex("female");}
                else if(sex.equals("U")){enc.setSex("unknown");}
                
                System.out.println("          Setting sex for row "+i+". Value: "+sex);
                
              }
              
              
              if(ok2import){  
                myShepherd.commitDBTransaction();
                if(newEncounter){myShepherd.storeNewEncounter(enc, enc.getCatalogNumber());}
              
                if(!individualID.equals("")){
                  MarkedIndividual indie=new MarkedIndividual();
                  myShepherd.beginDBTransaction();
                
                  Encounter enc2=myShepherd.getEncounter(encNumber);
                
                  if(myShepherd.isMarkedIndividual(individualID)){
                    indie=myShepherd.getMarkedIndividual(individualID);
                    newShark=false;
                  }
                  else{
                    indie.setIndividualID(individualID);
                  }
                  indie.addEncounter(enc2);
                
                  myShepherd.commitDBTransaction();
                  if(newShark){myShepherd.storeNewMarkedIndividual(indie);}
                }
                
            }
            else{myShepherd.rollbackDBTransaction();}
              
              out.println("Imported row: "+line);
              
            }
            
            
          }
          else{
            locked=true;
            System.out.println("ImportSRGD: For some reason the import failed without exception.");
          }


        } 
        catch (Exception le) {
          locked = true;
          myShepherd.rollbackDBTransaction();
          myShepherd.closeDBTransaction();
        }


        if (!locked) {
          myShepherd.commitDBTransaction();
          myShepherd.closeDBTransaction();
          out.println(ServletUtilities.getHeader(request));
          out.println("<strong>Success!</strong> I have successfully uploaded and imported your SRGD CSV file.");
          
          if(messages.equals("")){messages.toString().equals("None");}
          out.println("The following error messages were reported during the import process:<br /><ul>"+messages+"</ul>" );

          out.println(ServletUtilities.getFooter());
          } 
      
    } 
    catch (IOException lEx) {
      lEx.printStackTrace();
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I was unable to upload your SRGD CSV. Please contact the webmaster about this message.");
      out.println(ServletUtilities.getFooter());
    } 
    catch (NullPointerException npe) {
      npe.printStackTrace();
      out.println(ServletUtilities.getHeader(request));
      out.println("<strong>Error:</strong> I was unable to import SRGD data as no file was specified.");
      out.println(ServletUtilities.getFooter());
    }
    out.close();
  }


}


