UMA Delegation PoC: delegate control over UMA resources to other users
======================================================================

Requirements
------------
Keycloak 4.8.3.Final

Build, Deploy and Run
---------------------

1. Build:

   ````
   mvn clean install
   ````

2. Run Keycloak:

   ````
   $KEYCLOAK_HOME/bin/standalone.sh \
       -Dkeycloak.profile=preview \
       -Djboss.socket.binding.port-offset=100 \
       -Dkeycloak.migration.action=import \
       -Dkeycloak.migration.provider=singleFile \
       -Dkeycloak.migration.file=/path/to/uma-poc-realm.json \
       -Dkeycloak.migration.strategy=IGNORE_EXISTING
   ````

3. Run the PoC:

   ````
   mvn -DskipTests=true thorntail:run
   ````

4. Run tests:

   ````
   mvn test
   ````
   
