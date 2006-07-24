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

http://incubator.apache.org/servicemix/soap+binding+example



NOTES:
  * if you experience problems, try rebuilding the demo:
       remove all the directories but the src one,
       type 'ant setup'
       run '../../bin/servicemix servicemix.xml'
  * if you want to add authentication for this service,
       just add an authMethod="basic" attribute on
       the <http:endpoint> tag in the src/binding-su/xbean.xml
       and rebuild the demo.  Before accessing the service,
       you will need to enter a valid username / password
       (smx / smx).
