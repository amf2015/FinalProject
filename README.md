# FinalProject

## Required:
- Project now package to a .war file, and run on Tomcat Sever.
- Tomcat version: 8.0.47.
- Download link: https://tomcat.apache.org/download-80.cgi

## Add Tomcat runtime to Eclipse:
https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftwtomprf.html


## Run tomcat from terminal:
1. Download the FinalProject-Team1.war file from Git repo.
2. Move .war file into \apache-tomcat-8.0.47\webapps, and rename it to FinalProject-Team1.war
3. Download the data dump (stackoverflow), move the data folder to apache-tomcat-8.0.47\bin
4. Server startup:
	- Go to apache-tomcat-8.0.47\bin
        - windows: 	> startup.bat
        - Linux:	$ ./startup.sh
5. Web page will be available at: http://localhost:8080/FinalProject-Team1/index.html
6. Server shutdown:
	- Windows:	        > shutdown.bat
	- LInux:		$ ./shutdown.sh
	
	
## API
Dummy API for testing:
GET: http://localhost:8080/FinalProject-Team1/rest/test/ + [Parameter]
Will return: Paramter

Query API:
GET: http://localhost:8080/FinalProject-Team1/rest/query/ + [Parameter]
Will return: Query Result as JSON string
