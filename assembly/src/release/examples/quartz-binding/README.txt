Welcome to the ServiceMix Quartz Binding Example
================================================

This example shows how to use the Quartz scheduler to setup 
periodic events into the ServiceMix container.

To start the servicemix server using the sample configuration, just run:

../../bin/servicemix servicemix.xml

This will send a periodic event every 5 seconds into the container via 
the Quartz scheduler and then the event is picked up by a trace JBI component
which just outputs the event to the console.

For more information see:
http://servicemix.org/Quartz