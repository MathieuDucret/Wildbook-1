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

package org.ecocean;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.HashMap;
import java.util.GregorianCalendar;
import java.io.*;

import javax.servlet.http.HttpServletRequest;

import org.ecocean.genetics.*;
import org.ecocean.tag.AcousticTag;
import org.ecocean.tag.MetalTag;
import org.ecocean.tag.SatelliteTag;
import org.ecocean.Util;
import org.ecocean.servlet.ServletUtilities;

import javax.servlet.http.HttpServletRequest;



import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;
import org.ecocean.security.Collaboration;
import org.ecocean.servlet.ServletUtilities;

import javax.servlet.http.HttpServletRequest;



/**
 * An <code>encounter</code> object stores the complete data for a single sighting/capture report.
 * <code>Encounters</code> are added to MarkedIndividual objects as multiple encounters are associated with
 * known individuals.
 * <p/>
 *
 * @author Jason Holmberg
 * @version 2.0
 */
public class Encounter implements java.io.Serializable {
  static final long serialVersionUID = -146404246317385604L;

  /**
   * The following attributes are described in the Darwin Core quick reference at:
   * http://rs.tdwg.org/dwc/terms/#dcterms:type
   * <p/>
   * Wherever possible, this class will be extended with Darwin Core attributes for greater adoption of the standard.
   */
  private String sex = "unknown";
  private String locationID = "None";
  private Double maximumDepthInMeters;
  private Double maximumElevationInMeters;
  private String catalogNumber = "";
  private String individualID;
  private int day = 0;
  private int month = -1;
  private int year = 0;
  private Double decimalLatitude;
  private Double decimalLongitude;
  private String verbatimLocality;
  private String occurrenceRemarks = "";
  private String modified;
  private String occurrenceID;
  private String recordedBy;
  private String otherCatalogNumbers;
  private String behavior;
  private String eventID;
  private String measurementUnit;
  private String verbatimEventDate;
  private String dynamicProperties;
  public String identificationRemarks = "";
  public String genus = "";
  public String specificEpithet;
  public String lifeStage;
  public String country;


  /*
    * The following fields are specific to this mark-recapture project and do not have an easy to map Darwin Core equivalent.
    */

  //An URL to a thumbnail image representing the encounter.
  //This is
  private String dwcImageURL;

  //Defines whether the sighting represents a living or deceased individual.
  //Currently supported values are: "alive" and "dead".
  private String livingStatus;

  //Date the encounter was added to the library.
  private String dwcDateAdded;
  private Long dwcDateAddedLong;
  
  // If Encounter spanned more than one day, date of release
  private Date releaseDate;
  
  private Long releaseDateLong;

  //Size of the individual in meters
  private Double size;

  //Additional comments added by library users
  private String researcherComments = "None";

  //username of the logged in researcher assigned to the encounter
  //this STring is matched to an org.ecocean.User object to obtain more information
  private String submitterID;
  
  //name, email, phone, address of the encounter reporter
  private String submitterEmail, submitterPhone, submitterAddress;
  private String hashedSubmitterEmail;
  private String hashedPhotographerEmail;
  private String hashedInformOthers;
  private String informothers;
  //name, email, phone, address of the encounter photographer
  private String photographerName, photographerEmail, photographerPhone, photographerAddress;
  //a Vector of Strings defining the relative path to each photo. The path is relative to the servlet base directory
  public Vector additionalImageNames = new Vector();
  //a Vector of Strings of email addresses to notify when this encounter is modified
  private Vector interestedResearchers = new Vector();
  //time metrics of the report
  private int hour = 0;
  private String minutes = "00";
  
  private String state="";
  
  //the globally unique identifier (GUID) for this Encounter
  private String guid;

  private Long dateInMilliseconds;
  //describes how the shark was measured
  private String size_guess = "none provided";
  //String reported GPS values for lat and long of the encounter
  private String gpsLongitude = "", gpsLatitude = "";
  //whether this encounter has been rejected and should be hidden from public display
  //unidentifiable encounters generally contain some data worth saving but not enough for accurate photo-identification
  private boolean unidentifiable = false;
  //whether this encounter has a left-side spot image extracted
  public boolean hasSpotImage = false;
  //whether this encounter has a right-side spot image extracted
  public boolean hasRightSpotImage = false;
  //Indicates whether this record can be exposed via TapirLink
  private boolean okExposeViaTapirLink = false;
  //whether this encounter has been approved for public display
  private boolean approved = true;
  //integers of the latitude and longitude degrees
  //private int lat=-1000, longitude=-1000;
  //name of the stored file from which the left-side spots were extracted
  public String spotImageFileName = "";
  //name of the stored file from which the right-side spots were extracted
  public String rightSpotImageFileName = "";
  //string descriptor of the most obvious scar (if any) as reported by the original submitter
  //we also use keywords to be more specific
  public String distinguishingScar = "None";
  //describes how this encounter was matched to an existing shark - by eye, by pattern recognition algorithm etc.

  private int numSpotsLeft = 0;
  private int numSpotsRight = 0;


  //SPOTS
  //an array of the extracted left-side superSpots
  //private superSpot[] spots;
  private ArrayList<SuperSpot> spots;

  //an array of the extracted right-side superSpots
  //private superSpot[] rightSpots;
  private ArrayList<SuperSpot> rightSpots;

  //an array of the three extracted left-side superSpots used for the affine transform of the I3S algorithm
  //private superSpot[] leftReferenceSpots;
  private ArrayList<SuperSpot> leftReferenceSpots;

  //an array of the three extracted right-side superSpots used for the affine transform of the I3S algorithm
  //private superSpot[] rightReferenceSpots;
  private ArrayList<SuperSpot> rightReferenceSpots;

  //an open ended string that allows a type of patterning to be identified.
  //as an example, see the use of color codes at splashcatalog.org, allowing pre-defined fluke patterning types
  //to be used to help narrow the search for a marked individual
  private String patterningCode;

  //submitting organization and project further detail the scope of who submitted this project
  private String submitterOrganization;
  private String submitterProject;

  //hold submittedData
  //private List<DataCollectionEvent> collectedData;
  private List<TissueSample> tissueSamples;
  private List<SinglePhotoVideo> images;
  private List<Measurement> measurements;
  private List<MetalTag> metalTags;
  private AcousticTag acousticTag;
  private SatelliteTag satelliteTag;

  private Boolean mmaCompatible = false;
  
  //start constructors

  /**
   * empty constructor required by the JDO Enhancer
   */
  public Encounter() {
  }

  /**
   * Use this constructor to add the minimum level of information for a new encounter
   * The Vector <code>additionalImages</code> must be a Vector of Blob objects
   *
   */
  public Encounter(int day, int month, int year, int hour, String minutes, String size_guess, String location, String submitterName, String submitterEmail, List<SinglePhotoVideo> images) {
    this.verbatimLocality = location;
    this.recordedBy = submitterName;
    this.submitterEmail = submitterEmail;

    //now we need to set the hashed form of the email addresses
    this.hashedSubmitterEmail = Encounter.getHashOfEmailString(submitterEmail);

    this.images = images;
    this.day = day;
    this.month = month;
    this.year = year;
    this.hour = hour;
    this.minutes = minutes;
    this.size_guess = size_guess;
    this.individualID = "Unassigned";

    resetDateInMilliseconds();
  }


  /**
   * Returns an array of all of the superSpots for this encounter.
   *
   * @return the array of superSpots, taken from the croppedImage, that make up the digital fingerprint for this encounter
   */
  public ArrayList<SuperSpot> getSpots() {
    return spots;
  }

  public ArrayList<SuperSpot> getRightSpots() {
    return rightSpots;
  }

  /**
   * Returns an array of all of the superSpots for this encounter.
   *
   * @return the array of superSpots, taken from the croppedImage, that make up the digital fingerprint for this encounter
   */
  public void setSpots(ArrayList<SuperSpot> newSpots) {
    spots = newSpots;
  }

  public void setRightSpots(ArrayList<SuperSpot> newSpots) {
    rightSpots = newSpots;
  }

  /**
   * Removes any spot data
   */
  public void removeSpots() {
    spots = null;
  }

  public void removeRightSpots() {
    rightSpots = null;
  }

  public void nukeAllSpots() {
    leftReferenceSpots = null;
    rightReferenceSpots = null;
    spots = null;
    rightSpots = null;
  }

  /**
   * Returns the number of spots in the cropped image stored for this encounter.
   *
   * @return the number of superSpots that make up the digital fingerprint for this encounter
   */
  public int getNumSpots() {
    return spots.size();
  }

  public int getNumRightSpots() {
    return rightSpots.size();
  }

  public boolean hasLeftSpotImage() {
    return hasSpotImage;
  }

  public boolean hasRightSpotImage() {
    return hasRightSpotImage;
  }


  /**
   * Sets the recorded length of the shark for this encounter.
   */
  public void setSize(Double mysize) {
	  if(mysize!=null){size = mysize;}
	  else{size=null;}

  }

  /**
   * Returns the recorded length of the shark for this encounter.
   *
   * @return the length of the shark
   */
  public double getSize() {
    return size.doubleValue();
  }

  public Double getSizeAsDouble() {
    return size;
  }

  /**
   * Sets the units of the recorded size and depth of the shark for this encounter.
   * Acceptable entries are either "Feet" or "Meters"
   */
  public void setMeasureUnits(String measure) {
    measurementUnit = measure;
  }

  /**
   * Returns the units of the recorded size and depth of the shark for this encounter.
   *
   * @return the units of measure used by the recorded of this encounter, either "feet" or "meters"
   */
  public String getMeasureUnits() {
    return measurementUnit;
  }

  public String getMeasurementUnit() {
    return measurementUnit;
  }

  /**
   * Returns the recorded location of this encounter.
   *
   * @return the location of this encounter
   */
  public String getLocation() {
    return verbatimLocality;
  }

  public void setLocation(String location) {
    this.verbatimLocality = location;
  }

  /**
   * Sets the recorded sex of the shark in this encounter.
   * Acceptable values are "Male" or "Female"
   */
  public void setSex(String thesex) {
    if(thesex!=null){sex = thesex;}
    else{sex=null;}
  }

  /**
   * Returns the recorded sex of the shark in this encounter.
   *
   * @return the sex of the shark, either "male" or "female"
   */
  public String getSex() {
    return sex;
  }

  /**
   * Returns any submitted comments about scarring on the shark.
   *
   * @return any comments regarding observed scarring on the shark's body
   */

	public boolean getMmaCompatible() {
		return mmaCompatible;
	}
	public void setMmaCompatible(boolean b) {
		mmaCompatible = b;
	}

  public String getComments() {
    return occurrenceRemarks;
  }

  /**
   * Sets the initially submitted comments about markings and additional details on the shark.
   */
  public void setComments(String newComments) {
    occurrenceRemarks = newComments;
  }

  /**
   * Returns any comments added by researchers
   *
   * @return any comments added by authroized researchers
   */

  public String getRComments() {
    return researcherComments;
  }

  /**
   * Adds additional comments about the encounter
   *
   * @param newComments any additional comments to be added to the encounter
   */
  public void addComments(String newComments) {
    if ((researcherComments != null) && (!(researcherComments.equals("None")))) {
      researcherComments += newComments;
    } else {
      researcherComments = newComments;
    }
  }

  /**
   * Returns the name of the person who submitted this encounter data.
   *
   * @return the name of the person who submitted this encounter to the database
   */
  public String getSubmitterName() {
    return recordedBy;
  }

  public void setSubmitterName(String newname) {
    recordedBy = newname;
  }

  /**
   * Returns the e-mail address of the person who submitted this encounter data
   *
   * @return the e-mail address of the person who submitted this encounter data
   */
  public String getSubmitterEmail() {
    return submitterEmail;
  }

  public void setSubmitterEmail(String newemail) {
    submitterEmail = newemail;
    this.hashedSubmitterEmail = Encounter.getHashOfEmailString(newemail);
  }

  /**
   * Returns the phone number of the person who submitted this encounter data.
   *
   * @return the phone number of the person who submitted this encounter data
   */
  public String getSubmitterPhone() {
    return submitterPhone;
  }

  /**
   * Sets the phone number of the person who submitted this encounter data.
   */
  public void setSubmitterPhone(String newphone) {
    submitterPhone = newphone;
  }

  /**
   * Returns the mailing address of the person who submitted this encounter data.
   *
   * @return the mailing address of the person who submitted this encounter data
   */
  public String getSubmitterAddress() {
    return submitterAddress;
  }

  /**
   * Sets the mailing address of the person who submitted this encounter data.
   */
  public void setSubmitterAddress(String address) {
    submitterAddress = address;
  }

  /**
   * Returns the name of the person who took the primaryImage this encounter.
   *
   * @return the name of the photographer who took the primary image for this encounter
   */
  public String getPhotographerName() {
    return photographerName;
  }

  /**
   * Sets the name of the person who took the primaryImage this encounter.
   */
  public void setPhotographerName(String name) {
    photographerName = name;
  }

  /**
   * Returns the e-mail address of the person who took the primaryImage this encounter.
   *
   * @return  @return the e-mail address of the photographer who took the primary image for this encounter
   */
  public String getPhotographerEmail() {
    return photographerEmail;
  }

  /**
   * Sets the e-mail address of the person who took the primaryImage this encounter.
   */
  public void setPhotographerEmail(String email) {
    photographerEmail = email;
    this.hashedPhotographerEmail = Encounter.getHashOfEmailString(email);
  }

  /**
   * Returns the phone number of the person who took the primaryImage this encounter.
   *
   * @return the phone number of the photographer who took the primary image for this encounter
   */
  public String getPhotographerPhone() {
    return photographerPhone;
  }

  /**
   * Sets the phone number of the person who took the primaryImage this encounter.
   */
  public void setPhotographerPhone(String phone) {
    photographerPhone = phone;
  }

  /**
   * Returns the mailing address of the person who took the primaryImage this encounter.
   *
   * @return the mailing address of the photographer who took the primary image for this encounter
   */
  public String getPhotographerAddress() {
    return photographerAddress;
  }

  /**
   * Sets the mailing address of the person who took the primaryImage this encounter.
   */
  public void setPhotographerAddress(String address) {
    photographerAddress = address;
  }

  /**
   * Sets the recorded depth of this encounter.
   */
  public void setDepth(Double myDepth) {
	  if(myDepth!=null){maximumDepthInMeters = myDepth;}
	  else{maximumDepthInMeters = null;}
  }

  /**
   * Returns the recorded depth of this encounter.
   *
   * @return the recorded depth for this encounter
   */
  public double getDepth() {
    return maximumDepthInMeters.doubleValue();
  }

  public Double getDepthAsDouble(){
	  return maximumDepthInMeters;
  }


  //public Vector getAdditionalImages() {return additionalImages;}

  /**
   * Returns the file names of all images taken for this encounter.
   *
   * @return a vector of image name Strings
   */
  public Vector getAdditionalImageNames() {
    Vector imageNamesOnly=new Vector();
    
    //List<SinglePhotoVideo> images=getCollectedDataOfClass(SinglePhotoVideo.class);
    if((images!=null)&&(images.size()>0)){
      int imagesSize=images.size();
      for(int i=0;i<imagesSize;i++){
        SinglePhotoVideo dce=(SinglePhotoVideo)images.get(i);
        imageNamesOnly.add(dce.getFilename());
      }
    }
    return imageNamesOnly;
  }

  /**
   * Adds another image to the collection of images for this encounter.
   * These images should be the additional or non-side shots.
   *

  public void addAdditionalImageName(SinglePhotoVideo file) {
    images.add(file);

  }
*/
/*
  public void approve() {
    approved = true;
    okExposeViaTapirLink = true;
  }
*/
  /**
  public void resetAdditionalImageName(int position, String fileName) {
    additionalImageNames.set(position, fileName);
    //additionalImageNames.add(fileName);
  }
*/
  
  
  /**
   * Removes the specified additional image from this encounter.
   *
   * @param  imageFile  the image to be removed from the additional images stored for this encounter
   */
  /*
  public void removeAdditionalImageName(String imageFile) {

    for (int i = 0; i < collectedData.size(); i++) {
   
        
      String thisName = images.get(i).getFilename();
      if ((thisName.equals(imageFile)) || (thisName.indexOf("#") != -1)) {
        images.remove(i);
        i--;
      }
    
    }


  }
  */
  
  /*
  public void removeDataCollectionEvent(DataCollectionEvent dce) {
   collectedData.remove(dce);
  }
*/
  /**
   * Returns the unique encounter identifier number for this encounter.
   *
   * @return a unique integer String used to identify this encounter in the database
   */
  public String getEncounterNumber() {
    return catalogNumber;
  }


	public String generateEncounterNumber() {
		return Util.generateUUID();
	}


	public String dir(String baseDir) {
		return baseDir + File.separator + "encounters" + File.separator + this.subdir();
	}


	//like above, but class method so you pass the encID
	public static String dir(String baseDir, String id) {
		return baseDir + File.separator + "encounters" + File.separator + subdir(id);
	}


	//like above, but can pass a File in for base
	public static String dir(File baseDir, String id) {
		return baseDir.getAbsolutePath() + File.separator + "encounters" + File.separator + subdir(id);
	}


	//subdir() is kind of a utility function, which can be called as enc.subdir() or Encounter.subdir(IDSTRING) as needed
	public String subdir() {
		return subdir(this.getEncounterNumber());
	}

	public static String subdir(String id) {
		String d = id;  //old-world
		if (Util.isUUID(id)) {  //new-world
			d = id.charAt(0) + File.separator + id.charAt(1) + File.separator + id;
		}
		return d;
	}


  /**
   * Returns the date of this encounter.
   *
   * @return a Date object
   * @see java.util.Date
   */
  public String getDate() {
    String date = "";
    String time = "";
    if (year <= 0) {
      return "Unknown";
    } else if (month == -1) {
      return Integer.toString(year);
    }

    if (hour != -1) {
      String localMinutes=minutes;
      if(localMinutes.length()==1){localMinutes="0"+localMinutes;}
      time = String.format("%02d:%s", hour, localMinutes);
    }

    if (day > 0) {
      date = String.format("%04d-%02d-%02d %s", year, month, day, time);
    } 
    else if(month>-1) {
      date = String.format("%04d-%02d %s", year, month, time);
    }
    else {
      date = String.format("%04d %s", year, month, time);
    }

    return date;
  }

  public String getShortDate() {
    String date = "";
    if (year <= 0) {
      return "Unknown";
    } else if (month == -1) {
      return Integer.toString(year);
    }
    if (day > 0) {
      date = String.format("%02d/%02d/%04d", day, month, year);
    } else {
      date = String.format("%02d/%04d", month, year);
    }

    return date;
  }

  /**
   * Returns the String discussing how the size of this animal was approximated.
   *
   * @return a String with text about how the size of this animal was estimated/measured
   */
  public String getSizeGuess() {
    return size_guess;
  }

  public void setDay(int day) {
    this.day=day;
    resetDateInMilliseconds();
  }

  public void setHour(int hour) {
    this.hour=hour;
    resetDateInMilliseconds();
  }

  public void setMinutes(String minutes) {
    this.minutes=minutes;
    resetDateInMilliseconds();
  }

  public String getMinutes() {
    return minutes;
  }

  public int getHour() {
    return hour;
  }

  public void setMonth(int month) {
    this.month=month;
    resetDateInMilliseconds();
  }
  public void setYear(int year) {
    this.year=year;
    resetDateInMilliseconds();
  }


  public int getDay() {
    return day;
  }

  public int getMonth() {
    return month;
  }

  public int getYear() {
    return year;
  }


  /**
   * Returns the String holding specific location data used for searching
   *
   * @return the String holding specific location data used for searching
   */
  public String getLocationCode() {
    return locationID;
  }

  /**
   * A legacy method replaced by setLocationID(...).
   * 
   * 
   */
  public void setLocationCode(String newLoc) {
    setLocationID(newLoc);
  }

  /**
   * Returns the String holding specific location data used for searching
   *
   * @return the String holding specific location data used for searching
   */
  public String getDistinguishingScar() {
    return distinguishingScar;
  }

  /**
   * Sets the String holding scarring information for the encounter
   */
  public void setDistinguishingScar(String scar) {
    distinguishingScar = scar;
  }

  /**
   * Sets the String documenting how the size of this animal was approximated.
   */
  public void setSizeGuess(String newGuess) {
    size_guess = newGuess;
  }

  public String getMatchedBy() {
    if ((identificationRemarks == null) || (identificationRemarks.equals(""))) {
      return "Unknown";
    }
    return identificationRemarks;
  }

  public void setMatchedBy(String matchType) {
    setIdentificationRemarks(matchType);
  }
  
  public void setIdentificationRemarks(String matchType) {
    if(matchType!=null){identificationRemarks = matchType;}
    else{identificationRemarks=null;}
  }
  


  /**
   * Sets the unique encounter identifier to be usd with this encounter.
   * Once this is set, it cannot be changed without possible impact to the
   * database structure.
   *
   * @param num the unique integer to be used to uniquely identify this encoun ter in the database
   */
  public void setEncounterNumber(String num) {
    catalogNumber = num;
  }

  public String isAssignedToMarkedIndividual() {

    return individualID;

  }

  public void assignToMarkedIndividual(String sharky) {
    individualID = sharky;
  }

  /*
  public boolean wasRejected() {

    return unidentifiable;
  }

  public void reject() {
    unidentifiable = true;
    //okExposeViaTapirLink=false;
  }

  public void reaccept() {
    unidentifiable = false;
    //okExposeViaTapirLink=true;
  }
*/
  public String getGPSLongitude() {
    if (gpsLongitude == null) {
      return "";
    } else {
      return gpsLongitude;
    }
  }

  public void setGPSLongitude(String newLong) {

    gpsLongitude = newLong;
  }

  public String getGPSLatitude() {
    if (gpsLatitude == null) {
      return "";
    } else {
      return gpsLatitude;
    }
  }

  public void setGPSLatitude(String newLat) {
    gpsLatitude = newLat;
  }


  public Encounter getClone() {
    Encounter tempEnc = new Encounter();
    try {
      tempEnc = (Encounter) this.clone();
    } catch (java.lang.CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return tempEnc;
  }

  public String getSpotImageFileName() {
    return spotImageFileName;
  }

  public void setSpotImageFileName(String name) {
    spotImageFileName = name;
  }

  //-------------
  //for the right side spot image

  public String getRightSpotImageFileName() {
    return rightSpotImageFileName;
  }

  public void setRightSpotImageFileName(String name) {
    rightSpotImageFileName = name;
  }

  //----------------


  public void setSubmitterID(String username) {
    if(username!=null){submitterID = username;}
    else{submitterID=null;}
  }
  


  //old method. use getAssignedUser() instead
  public String getSubmitterID() {
    return getAssignedUsername();
  }
  
  public String getAssignedUsername() {
    return submitterID;
  }

  public Vector getInterestedResearchers() {
    return interestedResearchers;
  }

  public void addInterestedResearcher(String email) {
    interestedResearchers.add(email);
  }

  
  public boolean isApproved() {
    return approved;
  }

  public void removeInterestedResearcher(String email) {
    for (int i = 0; i < interestedResearchers.size(); i++) {
      String rName = (String) interestedResearchers.get(i);
      if (rName.equals(email)) {
        interestedResearchers.remove(i);
      }
    }
  }


  public double getRightmostSpot() {
    double rightest = 0;
    for (int iter = 0; iter < spots.size(); iter++) {
      if (spots.get(iter).getTheSpot().getCentroidX() > rightest) {
        rightest = spots.get(iter).getTheSpot().getCentroidX();
      }
    }
    return rightest;
  }

  public double getLeftmostSpot() {
    double leftest = getRightmostSpot();
    for (int iter = 0; iter < spots.size(); iter++) {
      if (spots.get(iter).getTheSpot().getCentroidX() < leftest) {
        leftest = spots.get(iter).getTheSpot().getCentroidX();
      }
    }
    return leftest;
  }

  public double getHighestSpot() {
    double highest = getLowestSpot();
    for (int iter = 0; iter < spots.size(); iter++) {
      if (spots.get(iter).getTheSpot().getCentroidY() < highest) {
        highest = spots.get(iter).getTheSpot().getCentroidY();
      }
    }
    return highest;
  }

  public double getLowestSpot() {
    double lowest = 0;
    for (int iter = 0; iter < spots.size(); iter++) {
      if (spots.get(iter).getTheSpot().getCentroidY() > lowest) {
        lowest = spots.get(iter).getTheSpot().getCentroidY();
      }
    }
    return lowest;
  }

  public com.reijns.I3S.Point2D[] getThreeLeftFiducialPoints() {
    com.reijns.I3S.Point2D[] Rray = new com.reijns.I3S.Point2D[3];
    if (getLeftReferenceSpots() != null) {

      ArrayList<SuperSpot> refsLeft = getLeftReferenceSpots();

      Rray[0] = new com.reijns.I3S.Point2D(refsLeft.get(0).getTheSpot().getCentroidX(), refsLeft.get(0).getTheSpot().getCentroidY());
      Rray[1] = new com.reijns.I3S.Point2D(refsLeft.get(1).getTheSpot().getCentroidX(), refsLeft.get(1).getTheSpot().getCentroidY());
      Rray[2] = new com.reijns.I3S.Point2D(refsLeft.get(2).getTheSpot().getCentroidX(), refsLeft.get(2).getTheSpot().getCentroidY());
      System.out.println("	I found three left reference points!");

    } else {
      com.reijns.I3S.Point2D topLeft = new com.reijns.I3S.Point2D(getLeftmostSpot(), getHighestSpot());
      com.reijns.I3S.Point2D bottomLeft = new com.reijns.I3S.Point2D(getLeftmostSpot(), getLowestSpot());
      com.reijns.I3S.Point2D bottomRight = new com.reijns.I3S.Point2D(getRightmostSpot(), getLowestSpot());
      Rray[0] = topLeft;
      Rray[1] = bottomLeft;
      Rray[2] = bottomRight;
    }

    return Rray;
  }

  public com.reijns.I3S.Point2D[] getThreeRightFiducialPoints() {
    com.reijns.I3S.Point2D[] Rray = new com.reijns.I3S.Point2D[3];
    if (getRightReferenceSpots() != null) {
      ArrayList<SuperSpot> refsRight = getRightReferenceSpots();
      Rray[0] = new com.reijns.I3S.Point2D(refsRight.get(0).getTheSpot().getCentroidX(), refsRight.get(0).getTheSpot().getCentroidY());
      Rray[1] = new com.reijns.I3S.Point2D(refsRight.get(1).getTheSpot().getCentroidX(), refsRight.get(1).getTheSpot().getCentroidY());
      Rray[2] = new com.reijns.I3S.Point2D(refsRight.get(2).getTheSpot().getCentroidX(), refsRight.get(2).getTheSpot().getCentroidY());

    } else {

      com.reijns.I3S.Point2D topRight = new com.reijns.I3S.Point2D(getRightmostRightSpot(), getHighestRightSpot());
      com.reijns.I3S.Point2D bottomRight = new com.reijns.I3S.Point2D(getRightmostRightSpot(), getLowestRightSpot());
      com.reijns.I3S.Point2D bottomLeft = new com.reijns.I3S.Point2D(getLeftmostRightSpot(), getLowestRightSpot());

      Rray[0] = topRight;
      Rray[1] = bottomRight;
      Rray[2] = bottomLeft;
    }
    return Rray;
  }

  public double getRightmostRightSpot() {
    double rightest = 0;
    for (int iter = 0; iter < rightSpots.size(); iter++) {
      if (rightSpots.get(iter).getTheSpot().getCentroidX() > rightest) {
        rightest = rightSpots.get(iter).getTheSpot().getCentroidX();
      }
    }
    return rightest;
  }


  public double getLeftmostRightSpot() {
    double leftest = getRightmostRightSpot();
    for (int iter = 0; iter < rightSpots.size(); iter++) {
      if (rightSpots.get(iter).getTheSpot().getCentroidX() < leftest) {
        leftest = rightSpots.get(iter).getTheSpot().getCentroidX();
      }
    }
    return leftest;
  }

  public double getHighestRightSpot() {
    double highest = getLowestRightSpot();
    for (int iter = 0; iter < rightSpots.size(); iter++) {
      if (rightSpots.get(iter).getTheSpot().getCentroidY() < highest) {
        highest = rightSpots.get(iter).getTheSpot().getCentroidY();
      }
    }
    return highest;
  }

  public double getLowestRightSpot() {
    double lowest = 0;
    for (int iter = 0; iter < rightSpots.size(); iter++) {
      if (rightSpots.get(iter).getTheSpot().getCentroidY() > lowest) {
        lowest = rightSpots.get(iter).getTheSpot().getCentroidY();
      }
    }
    return lowest;
  }


  public ArrayList<SuperSpot> getLeftReferenceSpots() {
    return leftReferenceSpots;
  }

  public ArrayList<SuperSpot> getRightReferenceSpots() {
    return rightReferenceSpots;
  }

  public void setLeftReferenceSpots(ArrayList<SuperSpot> leftReferenceSpots) {
    this.leftReferenceSpots = leftReferenceSpots;
  }

  public void setRightReferenceSpots(ArrayList<SuperSpot> rightReferenceSpots) {
    this.rightReferenceSpots = rightReferenceSpots;
  }


  /**
   * @param population array values to get the variance for
   * @return the variance
   */
  public double variance(double[] population) {
    long n = 0;
    double mean = 0;
    double s = 0.0;

    for (double x : population) {
      n++;
      double delta = x - mean;
      mean += delta / n;
      s += delta * (x - mean);
    }
    // if you want to calculate std deviation
    // of a sample change this to (s/(n-1))
    //return (s / n);
    return (s / (n - 1));
  }

  /**
   * @param population array values to get the standard deviation for
   * @return the standard deviation
   */
  public double standard_deviation(double[] population) {
    return Math.sqrt(variance(population));
  }


  public void setNumLeftSpots(int numspots) {
    numSpotsLeft = numspots;
  }

  public void setNumRightSpots(int numspots) {
    numSpotsRight = numspots;
  }

  
  public void setDWCGlobalUniqueIdentifier(String guid) {
    this.guid = guid;
  }

  public String getDWCGlobalUniqueIdentifier() {
    return guid;
  }

  public void setDWCImageURL(String link) {
    dwcImageURL = link;
  }

  public String getDWCDateLastModified() {
    return modified;
  }

  public void setDWCDateLastModified(String lastModified) {
    modified = lastModified;
  }

  public String getDWCDateAdded() {
    return dwcDateAdded;
  }
  
  public Long getDWCDateAddedLong(){
    return dwcDateAddedLong;
  }

  public void setDWCDateAdded(String m_dateAdded) {
    dwcDateAdded = m_dateAdded;
  }
  
  
 public void setDWCDateAdded(Long m_dateAdded) {
    dwcDateAddedLong = m_dateAdded;
    //org.joda.time.DateTime dt=new org.joda.time.DateTime(dwcDateAddedLong.longValue());
    //DateTimeFormatter parser1 = ISODateTimeFormat.dateOptionalTimeParser();
    //setDWCDateAdded(dt.toString(parser1));
    //System.out.println("     Encounter.detDWCDateAded(Long): "+dt.toString(parser1)+" which is also "+m_dateAdded.longValue());
  }
  //public void setDateAdded(long date){dateAdded=date;}
  //public long getDateAdded(){return dateAdded;}

  public Date getReleaseDateDONOTUSE() {
    return releaseDate;
  }
  
   public Date getReleaseDate() {
    if((releaseDateLong!=null)&&(releaseDateLong>0)){
      Date mDate=new Date(releaseDateLong);
      return mDate;
    }
    return null;
  }
   
   public Long getReleaseDateLong(){return releaseDateLong;}

  public void setReleaseDate(Long releaseDate) {
    this.releaseDateLong = releaseDate;
  }

  public void setDWCDecimalLatitude(double lat) {
    if (lat == -9999.0) {
      decimalLatitude = null;
    } else {
      decimalLatitude = (new Double(lat));
    }
  }



  public void setDWCDecimalLatitude(Double lat){
    if((lat!=null)&&(lat<=90)&&(lat>=-90)){
      this.decimalLatitude=lat;
    }
    else{this.decimalLatitude=null;}
  }
  public String getDWCDecimalLatitude(){
   if(decimalLatitude!=null){return Double.toString(decimalLatitude);}
     return null;
   }
  public void setDWCDecimalLongitude(double longit){
    if((longit>=-180)&&(longit<=180)){
      this.decimalLongitude=longit;
    }
  }

  public String getDWCDecimalLongitude(){
    if(decimalLongitude!=null){
      return Double.toString(decimalLongitude);
    }
    return null;
  }

  public boolean getOKExposeViaTapirLink() {
    return okExposeViaTapirLink;
  }

  public void setOKExposeViaTapirLink(boolean ok) {
    okExposeViaTapirLink = ok;
  }

  public void setAlternateID(String newID) {
    this.otherCatalogNumbers = newID;
  }

  public String getAlternateID() {
    if (otherCatalogNumbers == null) {
      return null;
    }
    return otherCatalogNumbers;
  }

  public String getInformOthers() {
    if (informothers == null) {
      return "";
    }
    return informothers;
  }

  public void setInformOthers(String others) {
    this.informothers = others;
    this.hashedInformOthers = Encounter.getHashOfEmailString(others);
  }

  public String getLocationID() {
    return locationID;
  }

  public void setLocationID(String newLocationID) {
    if(newLocationID!=null){this.locationID = newLocationID.trim();}
    else{locationID=null;}
  }
  
  

  public Double getMaximumDepthInMeters() {
    return maximumDepthInMeters;
  }

  public void setMaximumDepthInMeters(Double newDepth) {
    this.maximumDepthInMeters = newDepth;
  }

  public Double getMaximumElevationInMeters() {
    return maximumElevationInMeters;
  }

  public void setMaximumElevationInMeters(Double newElev) {
    this.maximumElevationInMeters = newElev;
  }


  public String getCatalogNumber() {
    return catalogNumber;
  }

  public void setCatalogNumber(String newNumber) {
    this.catalogNumber = newNumber;
  }

  public String getVerbatimLocality() {
    return verbatimLocality;
  }

  public void setVerbatimLocality(String vlcl) {
    this.verbatimLocality = vlcl;
  }

  public String getIndividualID() {
    return individualID;
  }

  public void setIndividualID(String indy) {
    this.individualID = indy;
  }

  public double getDecimalLatitudeAsDouble(){return decimalLatitude.doubleValue();}
  public void setDecimalLatitude(Double lat){this.decimalLatitude=lat;}

  public double getDecimalLongitudeAsDouble(){return decimalLongitude.doubleValue();}
  public void setDecimalLongitude(Double longy){this.decimalLongitude=longy;}


  public String getOccurrenceRemarks() {
    return occurrenceRemarks;
  }

  public void setOccurrenceRemarks(String remarks) {
    this.occurrenceRemarks = remarks;
  }

  public String getRecordedBy() {
    return recordedBy;
  }

  public void setRecordedBy(String submitterName) {
    this.recordedBy = submitterName;
  }

  public String getOtherCatalogNumbers() {
    return otherCatalogNumbers;
  }

  public void setOtherCatalogNumbers(String otherNums) {
    this.otherCatalogNumbers = otherNums;
  }

  public String getLivingStatus() {
    return livingStatus;
  }

  public void setLivingStatus(String status) {
    this.livingStatus = status;
  }


  public String getBehavior() {
    return behavior;
  }

  public void setBehavior(String beh) {
    this.behavior = beh;
  }

  public String getEventID() {
    return eventID;
  }

  public void setEventID(String id) {
    this.eventID = id;
  }

  public String getVerbatimEventDate() {
    return verbatimEventDate;
  }


  public void setVerbatimEventDate(String vet) {
      if(vet!=null){this.verbatimEventDate = vet;}
  	  else{this.verbatimEventDate=null;}
  }

  public String getDynamicProperties() {
    return dynamicProperties;
  }

  public void setDynamicProperty(String name, String value){
    name=name.replaceAll(";", "_").trim().replaceAll("%20", " ");
    value=value.replaceAll(";", "_").trim();

    if(dynamicProperties==null){dynamicProperties=name+"="+value+";";}
    else{

      //let's create a TreeMap of the properties
      TreeMap<String,String> tm=new TreeMap<String,String>();
      StringTokenizer st=new StringTokenizer(dynamicProperties, ";");
      while(st.hasMoreTokens()){
        String token = st.nextToken();
        int equalPlace=token.indexOf("=");
        try{
          tm.put(token.substring(0,equalPlace), token.substring(equalPlace+1));
       }
       catch(java.lang.StringIndexOutOfBoundsException soe){
       //this is a badly formatted pair that should be ignored
     }
      }
      if(tm.containsKey(name)){
        tm.remove(name);
        tm.put(name, value);

        //now let's recreate the dynamicProperties String
        String newProps=tm.toString();
        int stringSize=newProps.length();
        dynamicProperties=newProps.substring(1,(stringSize-1)).replaceAll(", ", ";")+";";
      }
      else{
        dynamicProperties=dynamicProperties+name+"="+value+";";
      }
    }
  }

  public String getDynamicPropertyValue(String name) {
    if (dynamicProperties != null) {
      name = name.replaceAll("%20", " ");
      //let's create a TreeMap of the properties
      TreeMap<String, String> tm = new TreeMap<String, String>();
      StringTokenizer st = new StringTokenizer(dynamicProperties, ";");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        int equalPlace = token.indexOf("=");
        tm.put(token.substring(0, equalPlace), token.substring(equalPlace + 1));
      }
      if (tm.containsKey(name)) {
        return tm.get(name);
      }
    }
    return null;
  }

  public void removeDynamicProperty(String name) {
    name = name.replaceAll(";", "_").trim().replaceAll("%20", " ");
    if (dynamicProperties != null) {

      //let's create a TreeMap of the properties
      TreeMap<String, String> tm = new TreeMap<String, String>();
      StringTokenizer st = new StringTokenizer(dynamicProperties, ";");
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        int equalPlace = token.indexOf("=");
        tm.put(token.substring(0, (equalPlace)), token.substring(equalPlace + 1));
      }
      if (tm.containsKey(name)) {
        tm.remove(name);

        //now let's recreate the dynamicProperties String
        String newProps = tm.toString();
        int stringSize = newProps.length();
        dynamicProperties = newProps.substring(1, (stringSize - 1)).replaceAll(", ", ";") + ";";
      }
    }
  }


  public String getIdentificationRemarks() {
    return identificationRemarks;
  }

  public String getHashedSubmitterEmail() {
    return hashedSubmitterEmail;
  }

  public String getHashedPhotographerEmail() {
    return hashedPhotographerEmail;
  }

  public String getHashedInformOthers() {
    return hashedInformOthers;
  }

  public static String getHashOfEmailString(String hashMe) {
    String returnString = "";
    StringTokenizer tokenizer = new StringTokenizer(hashMe, ",");
    while (tokenizer.hasMoreTokens()) {
      String emailAddress = tokenizer.nextToken().trim().toLowerCase();
      if (!emailAddress.equals("")) {
        String md5 = DigestUtils.md5Hex(emailAddress);
        if (returnString.equals("")) {
          returnString += md5;
        } else {
          returnString += "," + md5;
        }
      }
    }
    return returnString;
  }

  public String getGenus() {
    return genus;
  }

  public void setGenus(String newGenus) {
    if(newGenus!=null){genus = newGenus;}
	else{genus=null;}
  }

  public String getSpecificEpithet() {
    return specificEpithet;
  }

  public void setSpecificEpithet(String newEpithet) {
    if(newEpithet!=null){specificEpithet = newEpithet;}
	else{specificEpithet=null;}
  }

  public String getPatterningCode(){ return patterningCode;}
  public void setPatterningCode(String newCode){this.patterningCode=newCode;}

  public void resetDateInMilliseconds(){
    if(year>0){
      int localMonth=0;
      if(month>0){localMonth=month-1;}
      int localDay=1;
      if(day>0){localDay=day;}
      int localHour=0;
      if(hour>-1){localHour=hour;}
      int myMinutes=0;
      try{myMinutes = Integer.parseInt(minutes);}catch(Exception e){}
      GregorianCalendar gc=new GregorianCalendar(year, localMonth, localDay,localHour,myMinutes);

      dateInMilliseconds = new Long(gc.getTimeInMillis());
    }
    else{dateInMilliseconds=null;}
  }

  public java.lang.Long getDateInMilliseconds(){return dateInMilliseconds;}
  

  public String getDecimalLatitude(){
    if(decimalLatitude!=null){return Double.toString(decimalLatitude);}
    return null;
  }
  //public void setDecimalLatitude(String lat){this.decimalLatitude=Double.parseDouble(lat);}

  public String getDecimalLongitude(){
    if(decimalLongitude!=null){return Double.toString(decimalLongitude);}
    return null;
  }



  public String getSubmitterProject() {
      return submitterProject;
  }
  public void setSubmitterProject(String newProject) {
      if(newProject!=null){submitterProject = newProject;}
  	else{submitterProject=null;}
  }

    public String getSubmitterOrganization() {
        return submitterOrganization;
    }
    public void setSubmitterOrganization(String newOrg) {
        if(newOrg!=null){submitterOrganization = newOrg;}
    	else{submitterOrganization=null;}
    }

   // public List<DataCollectionEvent> getCollectedData(){return collectedData;}

    /*
    public ArrayList<DataCollectionEvent> getCollectedDataOfType(String type){
      ArrayList<DataCollectionEvent> filteredList=new ArrayList<DataCollectionEvent>();
      int cdSize=collectedData.size();
      System.out.println("cdSize="+cdSize);
      for(int i=0;i<cdSize;i++){
        System.out.println("i="+i);
        DataCollectionEvent tempDCE=collectedData.get(i);
        if(tempDCE.getType().equals(type)){filteredList.add(tempDCE);}
      }
      return filteredList;
    }
    */
    /*
    public <T extends DataCollectionEvent> List<T> getCollectedDataOfClass(Class<T> clazz) {
      List<DataCollectionEvent> collectedData = getCollectedData();
      List<T> result = new ArrayList<T>();
      for (DataCollectionEvent dataCollectionEvent : collectedData) {
        if (dataCollectionEvent.getClass().isAssignableFrom(clazz)) {
          result.add((T) dataCollectionEvent);
        }
      }
      return result;
    }
    
    public <T extends DataCollectionEvent> List<T> getCollectedDataOfClassAndType(Class<T> clazz, String type) {
      List<T> collectedDataOfClass = getCollectedDataOfClass(clazz);
      List<T> result = new ArrayList<T>();
      for (T t : collectedDataOfClass) {
        if (type.equals(t.getType())) {
          result.add(t);
        }
      }
      return result;
    }
    
    public void addCollectedDataPoint(DataCollectionEvent dce){
      if(collectedData==null){collectedData=new ArrayList<DataCollectionEvent>();}
      if(!collectedData.contains(dce)){collectedData.add(dce);}
    }
    public void removeCollectedDataPoint(int num){collectedData.remove(num);}
    */
    
    public void addTissueSample(TissueSample dce){
      if(tissueSamples==null){tissueSamples=new ArrayList<TissueSample>();}
      if(!tissueSamples.contains(dce)){tissueSamples.add(dce);}
    }
    public void removeTissueSample(int num){tissueSamples.remove(num);}
    public List<TissueSample> getTissueSamples(){return tissueSamples;}
    public void removeTissueSample(TissueSample num){tissueSamples.remove(num);}

    public void addSinglePhotoVideo(SinglePhotoVideo dce){
      if(images==null){images=new ArrayList<SinglePhotoVideo>();}
      if(!images.contains(dce)){images.add(dce);}
    }
    public void removeSinglePhotoVideo(int num){images.remove(num);}
    public List<SinglePhotoVideo> getSinglePhotoVideo(){return images;}
    public void removeSinglePhotoVideo(SinglePhotoVideo num){images.remove(num);}
    

    public void setMeasurement(Measurement measurement, Shepherd myShepherd){
      
      //if measurements are null, set the empty list
      if(measurements==null){measurements=new ArrayList<Measurement>();}
      
      //now start checking for existence of a previous measurement
      
      //if we have it but the new value is null, remove the measurement
      if((this.hasMeasurement(measurement.getType()))&&(measurement.getValue()==null)){
        Measurement m=this.getMeasurement(measurement.getType());
        measurements.remove(m);
        myShepherd.getPM().deletePersistent(m);
        myShepherd.commitDBTransaction();
        myShepherd.beginDBTransaction();
      }
      
      //just add the measurement it if we did not have it before
      else if(!this.hasMeasurement(measurement.getType())){
        measurements.add(measurement);
        myShepherd.commitDBTransaction();
        myShepherd.beginDBTransaction();
      }
      
      //if we had it before then just update the value
      else if((this.hasMeasurement(measurement.getType()))&&(measurement!=null)){
        Measurement m=this.getMeasurement(measurement.getType());
        m.setValue(measurement.getValue());
        m.setSamplingProtocol(measurement.getSamplingProtocol());
        myShepherd.commitDBTransaction();
        myShepherd.beginDBTransaction();
      }
      
    }
    public void removeMeasurement(int num){measurements.remove(num);}
    public List<Measurement> getMeasurements(){return measurements;}
    public void removeMeasurement(Measurement num){measurements.remove(num);}
    public Measurement findMeasurementOfType(String type) {
      List<Measurement> measurements = getMeasurements();
      if (measurements != null) {
        for (Measurement measurement : measurements) {
          if (type.equals(measurement.getType())) {
            return measurement;
          }
        }
      }
      return null;
    }
    
    public void addMetalTag(MetalTag metalTag) {
      if (metalTags == null) {
        metalTags = new ArrayList<MetalTag>();
      }
      metalTags.add(metalTag);
    }
    
    public void removeMetalTag(MetalTag metalTag) {
      metalTags.remove(metalTag);
    }
    
    public List<MetalTag> getMetalTags() {
      return metalTags;
    }
    
    public MetalTag findMetalTagForLocation(String location) {
      List<MetalTag> metalTags = getMetalTags();
      if (metalTags != null) {
        for (MetalTag metalTag : metalTags) {
          if (location.equals(metalTag.getLocation())) {
            return metalTag;
          }
        }
      }
      return null;
    }
    
    public AcousticTag getAcousticTag() {
      return acousticTag;
    }

    public void setAcousticTag(AcousticTag acousticTag) {
      this.acousticTag = acousticTag;
    }

    public SatelliteTag getSatelliteTag() {
      return satelliteTag;
    }

    public void setSatelliteTag(SatelliteTag satelliteTag) {
      this.satelliteTag = satelliteTag;
    }

    public String getLifeStage(){return lifeStage;}
    public void setLifeStage(String newStage) {
      if(newStage!=null){lifeStage = newStage;}
      else{lifeStage=null;}
    }
    
    
    /**
     * A convenience method that returns the first haplotype found in the TissueSamples for this Encounter. 
     * 
     *@return a String if found or null if no haplotype is found
     */
    public String getHaplotype(){
      //List<TissueSample> tissueSamples=getCollectedDataOfClass(TissueSample.class);
      int numTissueSamples=tissueSamples.size();
      if(numTissueSamples>0){
        for(int j=0;j<numTissueSamples;j++){
          TissueSample thisSample=tissueSamples.get(j);
          int numAnalyses=thisSample.getNumAnalyses();
          if(numAnalyses>0){
            List<GeneticAnalysis> gAnalyses = thisSample.getGeneticAnalyses();
            for(int g=0;g<numAnalyses;g++){
              GeneticAnalysis ga = gAnalyses.get(g);
              if(ga.getAnalysisType().equals("MitochondrialDNA")){
                MitochondrialDNAAnalysis mito=(MitochondrialDNAAnalysis)ga;
                if(mito.getHaplotype()!=null){return mito.getHaplotype();}
              }
            }
          }
        }
      }
      return null;
    }
    
    /**
     * A convenience method that returns the first genetic sex found in the TissueSamples for this Encounter. 
     * 
     *@return a String if found or null if no genetic sex is found
     */
    public String getGeneticSex(){
      if(tissueSamples!=null){
      int numTissueSamples=tissueSamples.size();
      if(numTissueSamples>0){
        for(int j=0;j<numTissueSamples;j++){
          TissueSample thisSample=tissueSamples.get(j);
          int numAnalyses=thisSample.getNumAnalyses();
          if(numAnalyses>0){
            List<GeneticAnalysis> gAnalyses = thisSample.getGeneticAnalyses();
            for(int g=0;g<numAnalyses;g++){
              GeneticAnalysis ga = gAnalyses.get(g);
              if(ga.getAnalysisType().equals("SexAnalysis")){
                SexAnalysis mito=(SexAnalysis)ga;
                if(mito.getSex()!=null){return mito.getSex();}
              }
            }
          }
        }
      }
      }
      return null;
    }
    
    public List<SinglePhotoVideo> getImages(){return images;}
    
    public boolean hasKeyword(Keyword word){
     int imagesSize=images.size();
     for(int i=0;i<imagesSize;i++){
       SinglePhotoVideo image=images.get(i);
       if(image.getKeywords().contains(word)){return true;}
     }
     return false; 
    }

    public String getState(){return state;}
    
    public void setState(String newState){this.state=newState;}
    
    //DO NOT USE - LEGACY MIGRATION ONLY
    public boolean getApproved(){return approved;}
    public boolean getUnidentifiable(){return unidentifiable;}
    
    public Vector getOldAdditionalImageNames(){return additionalImageNames;}
    
    public Double getLatitudeAsDouble(){return decimalLatitude;}
    public Double getLongitudeAsDouble(){return decimalLongitude;}
    
    public boolean hasMeasurements(){
      if((measurements!=null)&&(measurements.size()>0)){
        int numMeasurements=measurements.size();
        for(int i=0;i<numMeasurements;i++){
          Measurement m=measurements.get(i);
          if(m.getValue()!=null){return true;}
        }
      }
      return false;
    }
    
    public boolean hasMeasurement(String type){
      if((measurements!=null)&&(measurements.size()>0)){
        int numMeasurements=measurements.size();
        for(int i=0;i<numMeasurements;i++){
          Measurement m=measurements.get(i);
          if((m.getValue()!=null)&&(m.getType().equals(type))){return true;}
        }
      }
      return false;
    }
    
    public boolean hasBiologicalMeasurement(String type){
      if((tissueSamples!=null)&&(tissueSamples.size()>0)){  
        int numTissueSamples=tissueSamples.size();
        for(int i=0;i<numTissueSamples;i++){
          TissueSample ts=tissueSamples.get(i);
          if(ts.getBiologicalMeasurement(type)!=null){
            BiologicalMeasurement bm=ts.getBiologicalMeasurement(type);
            if(bm.getValue()!=null){return true;}
          }
        }
      }
      return false;
    }
    
    
    
    /**
     * Returns the first measurement of the specified type
     * @param type
     * @return
     */
    public Measurement getMeasurement(String type){
      if((measurements!=null)&&(measurements.size()>0)){
        int numMeasurements=measurements.size();
        for(int i=0;i<numMeasurements;i++){
          Measurement m=measurements.get(i);
          if((m.getValue()!=null)&&(m.getType().equals(type))){return m;}
        }
      }
      return null;
    }
    
    public BiologicalMeasurement getBiologicalMeasurement(String type){
      
      if(tissueSamples!=null){int numTissueSamples=tissueSamples.size();
      for(int y=0;y<numTissueSamples;y++){
        TissueSample ts=tissueSamples.get(y);
        if((ts.getGeneticAnalyses()!=null)&&(ts.getGeneticAnalyses().size()>0)){
          int numMeasurements=ts.getGeneticAnalyses().size();
          for(int i=0;i<numMeasurements;i++){
            GeneticAnalysis m=ts.getGeneticAnalyses().get(i);
            if(m.getAnalysisType().equals("BiologicalMeasurement")){
              BiologicalMeasurement f=(BiologicalMeasurement)m;
              if((f.getMeasurementType().equals(type))&&(f.getValue()!=null)){return f;}
            }
          }
        }
      }
      }

      return null;
    }
    
    public String getCountry(){return country;}
    
    public void setCountry(String newCountry) {
      if(newCountry!=null){country = newCountry;}
      else{country=null;}
    }
    
    public void setOccurrenceID(String vet) {
      if(vet!=null){this.occurrenceID = vet;}
      else{this.occurrenceID=null;}
  }
    
    public String getOccurrenceID(){return occurrenceID;}
    
    public boolean hasSinglePhotoVideoByFileName(String filename){
        int numImages=images.size();
        for(int i=0;i<numImages;i++){
          SinglePhotoVideo single=images.get(i);
          if(single.getFilename().trim().toLowerCase().equals(filename.trim().toLowerCase())){return true;}
        }
        return false;
    }


	//convenience function to Collaboration permissions
	public boolean canUserAccess(HttpServletRequest request) {
		return Collaboration.canUserAccessEncounter(this, request);
	}


	//this simple version makes some assumptions: you already have list of collabs, and it is not visible
	public String collaborationLockHtml(ArrayList<Collaboration> collabs) {
		Collaboration c = Collaboration.findCollaborationWithUser(this.getAssignedUsername(), collabs);
		String collabClass = "pending";
		if ((c == null) || (c.getState() == null)) {
			collabClass = "new";
		} else if (c.getState().equals(Collaboration.STATE_REJECTED)) {
			collabClass = "blocked";
		}
		return "<div class=\"row-lock " + collabClass + " collaboration-button\" data-collabowner=\"" + this.getAssignedUsername() + "\" data-collabownername=\"" + this.getSubmitterName() + "\">&nbsp;</div>";
	}


	//pass in a Vector of Encounters, get out a list that the user can NOT see
	public static Vector blocked(Vector encs, HttpServletRequest request) {
		Vector blk = new Vector();
		for (int i = 0; i < encs.size() ; i++) {
			Encounter e = (Encounter) encs.get(i);
			if (!e.canUserAccess(request)) blk.add(e);
		}
		return blk;
	}


/*
in short, this rebuilds (or builds for the first time) ALL *derived* images (etc?) for this encounter.
it is a baby step into the future of MediaAssets that hopefully will provide a smooth(er) transition to that.
right now its primary purpose is to create derived formats upon encounter creation; but that is obviously subject to change.
it should be considered an asyncronous action that happens in the background magickally
*/
/////other possiblity: only pass basedir??? do we need context if we do that?

/*
NOTE on "thumb.jpg" ... we only get one of these per encounter; and we do not have stored (i dont think?) which SPV it came from!
this is a problem, as we cant make a thumb in refreshAssetFormats(req, spv) since we dont know if that is the "right" spv.
thus, we have to treat it as a special case.
*/
		public boolean refreshAssetFormats(String context, String baseDir) {
			boolean ok = true;
			//List<SinglePhotoVideo> allSPV = this.getImages();
			boolean thumb = true;
			for (SinglePhotoVideo spv : this.getImages()) {
				ok &= this.refreshAssetFormats(context, baseDir, spv, thumb);
				thumb = false;
			}
			return ok;
		}

		//as above, but for specific SinglePhotoVideo
		public boolean refreshAssetFormats(String context, String baseDir, SinglePhotoVideo spv, boolean doThumb) {
			if (spv == null) return false;
			String encDir = this.dir(baseDir);

			boolean ok = true;
			if (doThumb) ok &= spv.scaleTo(context, 100, 75, encDir + File.separator + "thumb.jpg");
			//TODO some day this will be a structure/definition that lives in a config file or on MediaAsset, etc.  for now, ya get hard-coded

			//this will first try watermark version, then regular
			ok &= (spv.scaleToWatermark(context, 250, 200, encDir + File.separator + spv.getDataCollectionEventID() + ".jpg", "") || spv.scaleTo(context, 250, 200, encDir + File.separator + spv.getDataCollectionEventID() + ".jpg"));

			ok &= spv.scaleTo(context, 1024, 768, encDir + File.separator + spv.getDataCollectionEventID() + "-mid.jpg");  //for use in VM tool etc. (bandwidth friendly?)
			return ok;
		}


	//see also: future, MediaAssets
	public String getThumbnailUrl(String context) {
		List<SinglePhotoVideo> spvs = this.images;
		if (spvs == null || spvs.size() < 1) return null;
		return "/" + CommonConfiguration.getDataDirectoryName(context) + "/encounters/" + this.subdir() + "/thumb.jpg";
	}

	public boolean restAccess(HttpServletRequest request, JSONObject jsonobj) throws Exception {
		ApiAccess access = new ApiAccess();
System.out.println("hello i am in restAccess() on Encounter");

		String fail = access.checkRequest(this, request, jsonobj);
System.out.println("fail -----> " + fail);
		if (fail != null) throw new Exception(fail);

		//HashMap<String, String> perm = access.permissions(this, request);
//System.out.println(perm);

/*
System.out.println("!!!----------------------------------------");
System.out.println(request.getMethod());
throw new Exception();
*/
		return true;
	}


/*  not really sure we need this now/yet

	public void refreshDependentProperties() {
		this.resetDateInMilliseconds();
//TODO could possibly do integrity check, re: individuals/occurrences linking?
	}

*/



}

