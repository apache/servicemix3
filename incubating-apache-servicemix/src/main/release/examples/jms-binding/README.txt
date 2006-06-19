Welcome to the ServiceMix JMS Binding Example
=============================================

This example uses shows how to connect the JMS bindings to a simple service component.

To start the servicemix server using the sample configuration, just run:

ant setup
../../bin/servicemix servicemix.xml

This will start a component which listens for a JMS message on topic 'demo.org.servicemix.source' and then
publishes processed messages to topic 'demo.org.servicemix.result'.

A simple JMS client is provided so that messages can be sent and received from those JMS topics.  The client is built
and run from source using Ant, http://ant.apache.org. Just execute 'ant' from the current directory to 
run the JMS client.

For more information see

http://incubator.apache.org/servicemix/JMS
