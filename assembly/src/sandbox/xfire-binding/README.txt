Welcome to the ServiceMix XFire Binding Example
===============================================

This example uses shows how to connect the XFire bindings expose the
ServiceMix components via SOAP.

This example uses Maven, http://maven.apache.org, to download the needed
dependencies, build, and run the example.  Please download and install
maven before using this example.

To build and run a webapp that deploys XFire and ServiceMix just run:

maven webserver

This will start an embedded webserver on port 8080 with the example 
web application deployed.

The exposed services are located at:

http://localhost:8080/Echo11Service?wsdl
http://localhost:8080/Echo12Service?wsdl

For more information, see:
http://servicemix.org/XFire