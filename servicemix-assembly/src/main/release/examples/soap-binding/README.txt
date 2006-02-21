Welcome to the soap-binding Example
=============================================

This example shows how to expose a simple web service with ServiceMix.
The components are built and run from source using Ant, http://ant.apache.org.

To start the servicemix server using the sample configuration, you must enter the following commands:

ant setup
../../bin/servicemix servicemix.xml

This will start the soap-binding demo.

A simple client is available with the client.html web page.
Just launched it in your favorite web browser and modify the content of the left pane.
You should see the response on the right pane.

For more information on this example please see

http://servicemix.org/soap+binding+example



NOTES:
  * currently, the demo does not work with FireFox
  * if you experience problems, try rebuilding the demo:
       remove all the directories but the src one,
       type 'ant setup'
       run '../../bin/servicemix servicemix.xml'
