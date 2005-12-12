Welcome to the ServiceMix File Binding Example
==============================================

This example shows how to use the File bindings to do a simple file 
transfer.

To start the servicemix server using the sample configuration, just run:

../../bin/servicemix servicemix.xml

This will start a component which waits for files to arrive in the inbox 
directory.  It then does a file transfer to the outbox directory.

Just copy the test-file.xml file into the inbox directory and you should 
see it apear in the outbox directory in a few seconds.

For more information see:
http://servicemix.org/File