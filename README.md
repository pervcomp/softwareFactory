# Sonar Scanner Webapp  


This project is onty the frontend docker image of the application. 
The backend can be found here https://github.com/pervcomp/sonar-scanner-webservice 

Set the connection details for the MongoDB here:
src/main/resources/application.properties:

For the Dockerzied version use:

spring.data.mongodb.database=<your_sensible_database_name_>
spring.data.mongodb.host=mongodb
spring.data.mongodb.port=27017