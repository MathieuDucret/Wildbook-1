<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=utf-8" language="java" import="org.joda.time.LocalDateTime,
org.joda.time.format.DateTimeFormatter,
org.joda.time.format.ISODateTimeFormat,java.net.*,java.io.*,java.util.*, java.io.FileInputStream, java.io.File, java.io.FileNotFoundException, org.ecocean.*,org.ecocean.servlet.*,javax.jdo.*, java.lang.StringBuffer, java.util.Vector, java.util.Iterator, java.lang.NumberFormatException"%>

<%

String context="context0";
context=ServletUtilities.getContext(request);

	Shepherd myShepherd=new Shepherd(context);

// pg_dump -Ft sharks > sharks.out

//pg_restore -d sharks2 /home/webadmin/sharks.out


%>

<html>
<head>
<title>Find Pattern Problems</title>

</head>


<body>
<ol>
<%

myShepherd.beginDBTransaction();

//build queries

Extent encClass=myShepherd.getPM().getExtent(Encounter.class, true);
Query encQuery=myShepherd.getPM().newQuery(encClass);
Iterator<Encounter> allEncs;






try{



	
allEncs=myShepherd.getAllEncounters(encQuery);

int numIssues=0;
int numEncounters=0;

DateTimeFormatter fmt = ISODateTimeFormat.date();
DateTimeFormatter parser1 = ISODateTimeFormat.dateOptionalTimeParser();



while(allEncs.hasNext()){
	

	Encounter sharky=allEncs.next();
	

	if((sharky.getSpots()!=null)&&(sharky.getNumSpots()>0)){
		numEncounters++;
		
		%>
		<li>Encounter <%=sharky.getCatalogNumber() %> has <%=sharky.getSpots().size() %> left-side spots.</li>
		<%
	}
	else{
		sharky.setNumLeftSpots(0);
		myShepherd.commitDBTransaction();
		myShepherd.beginDBTransaction();
	}
	
	

	

}




%>


<p>Done successfully!</p>
<p><%=numIssues %> issues found.</p>
<p><%=numEncounters %> left-side patterns found. Check these against the functional estimate of <%=myShepherd.getNumEncountersWithSpotData(false) %>.


<%
} 
catch(Exception ex) {

	System.out.println("!!!An error occurred on page fixSomeFields.jsp. The error was:");
	ex.printStackTrace();
	//System.out.println("fixSomeFields.jsp page is attempting to rollback a transaction because of an exception...");
	encQuery.closeAll();
	encQuery=null;
	//sharkQuery.closeAll();
	//sharkQuery=null;


}
finally{
	myShepherd.rollbackDBTransaction();
	myShepherd.closeDBTransaction();
	myShepherd=null;
}
%>

</ol>
</body>
</html>