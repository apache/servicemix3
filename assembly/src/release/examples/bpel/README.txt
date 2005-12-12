Welcome to the ServiceMixBPEL Example
=============================================

This example uses shows how to connect the JMS bindings to a BPEL service component provided by the PXE AsyncProcess example - see www.fivesight.com

To start the servicemix server using the sample configuration, just run:

../../bin/servicemix servicemix.xml

This will start a component which sends a soap message to a JmsServiceComponent which forwards the request to the PXE BPEL engine and waits for a response.

A simple JMS client is provided so that messages can sent and received from the JMSService.  The client is built
and run from source using Ant, http://ant.apache.org. Just execute 'ant' from the current directory to
run the JMS client.


For more information on this example please see

http://servicemix.org/BPEL+example