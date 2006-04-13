Welcome to the ServiceMix Web Services - Notification Example
=============================================================

This example demonstrates the ability to use the Web Services - 
Notification implementation in ServiceMix. To run this example, open 
three terminals - one for each instance directory. In each terminal, start
ServiceMix and feed the configuration like so: 

In terminal one: 
$ cd ./instance1 
$ ../../../bin/servicemix ./servicemix1.xml

In terminal two: 
$ cd ./instance2
$ ../../../bin/servicemix ./servicemix2.xml

In terminal three: 
$ cd ./instance3
$ ../../../bin/servicemix ./servicemix3.xml

ServiceMix instance3 will publish messages to the topic named MyTopic and 
ServiceMix instance2 will receive these messages because it is subscribed to 
the topic named MyTopic.

WS-Notification demand publishing and filters will be added soon. 
