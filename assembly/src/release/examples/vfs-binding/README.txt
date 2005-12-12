Welcome to the ServiceMix VFS Binding Example
=============================================

This example uses shows how to use the VFS bindings to do a simple 
file transfer.

To start the servicemix server using the sample configuration, 
just run:

..\..\bin\servicemix servicemix-win.xml

or on unix systems,

../../bin/servicemix servicemix-unix.xml


This will start a component which waits for files to arrive in the 
/tmp/servicemix-inbox directory.  It then does a file transfer to 
the /tmp/servicemix-outbox directory.

Just copy the test-file.xml file into the /tmp/servicemix-inbox 
directory and you should see it apear in the outbox directory in a 
few seconds.

For more information see:
http://servicemix.org/VFS
