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

package org.ecocean.servlet;

import org.ecocean.ActionResult;
import org.ecocean.CommonConfiguration;
import org.ecocean.MarkedIndividual;
import org.ecocean.Shepherd;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Locale;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//Set alternateID for the individual
public class IndividualSetYearOfBirth extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(IndividualSetYearOfBirth.class);
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }


  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }


  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String context = ServletUtilities.getContext(request);
    String langCode = ServletUtilities.getLanguageCode(request);
    Locale locale = new Locale(langCode);
    Shepherd myShepherd = new Shepherd(context);
    //set up for response
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    boolean locked = false;

    // Prepare for user response.
    String link = "#";
    try {
      link = CommonConfiguration.getServerURL(request, request.getContextPath()) + String.format("/individuals.jsp?number=%s", request.getParameter("individual"));
    }
    catch (URISyntaxException ex) {
    }
    ActionResult actionResult = new ActionResult(locale, "individual.editField", true, link)
            .setParams(request.getParameter("individual"), request.getParameter("timeOfBirth"));

    String sharky = "None";
    sharky = request.getParameter("individual");
    
    String timeOfBirth="";
    long longTime=-1;
    boolean badFormat = false;
    if((request.getParameter("timeOfBirth")!=null)&&(!request.getParameter("timeOfBirth").equals(""))){
      timeOfBirth=request.getParameter("timeOfBirth");
      try {
        longTime = (new DateTime(timeOfBirth)).getMillis();
      }
      catch (IllegalArgumentException ex) {
        badFormat = true;
      }
    }

    myShepherd.beginDBTransaction();

    if (myShepherd.isMarkedIndividual(sharky)) {
      MarkedIndividual myShark = myShepherd.getMarkedIndividual(sharky);

      try {
        //Long myTime=new Long(longTime);
        myShark.setTimeOfBirth(longTime);
        myShark.addComments("<p><em>" + request.getRemoteUser() + " on " + (new java.util.Date()).toString() + "</em><br>" + "Set time of birth to " + timeOfBirth + ".");

      } catch (Exception le) {
        locked = true;
        myShepherd.rollbackDBTransaction();
        myShepherd.closeDBTransaction();
      }

      if (!locked) {
        myShepherd.commitDBTransaction();
        myShepherd.closeDBTransaction();
        actionResult.setMessageOverrideKey("dateOfBirth");
        if (badFormat)
          actionResult.setSucceeded(false);
      } else {
        actionResult.setSucceeded(false).setMessageOverrideKey("locked");
      }
    } else {
      myShepherd.rollbackDBTransaction();
      actionResult.setSucceeded(false);
    }

    // Reply to user.
    request.getSession().setAttribute(ActionResult.SESSION_KEY, actionResult);
    getServletConfig().getServletContext().getRequestDispatcher(ActionResult.JSP_PAGE).forward(request, response);

    out.close();
    myShepherd.closeDBTransaction();
  }
}
