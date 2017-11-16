# FinalProject

## Required:
- Project now package to a .war file, and run on Tomcat Sever.
- Tomcat version: 8.0.47.
- Download link: https://tomcat.apache.org/download-80.cgi

## Add Tomcat runtime to Eclipse:
https://help.eclipse.org/mars/index.jsp?topic=%2Forg.eclipse.jst.server.ui.doc.user%2Ftopics%2Ftwtomprf.html


## Run tomcat from terminal:
1. Package project to .war file: mvn clean install    
> .war file will be in \.m2\repository\edu\unh\cs753853\team1\FinalProject-Team1\0.0.1-SNAPSHOT
2. Move .war file into \apache-tomcat-8.0.47\webapps
3. Server startup:
	- Go to apache-tomcat-8.0.47\bin
        - windows: 	> startup.bat
        - Linux:	$ ./startup.sh
4. Web page will be available at: http://localhost:8080/FinalProject-Team1/index.html
5. Server shutdown:
	- Windows:	        > shutdown.bat
	- LInux:		$ ./shutdown.sh
	
	
## API
Dummy API for testing:
Get: http://localhost:8080/FinalProject-Team1/rest/hello/ + [Parameter]
Will return: Paramter
