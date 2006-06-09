Welcome to the ServiceMix cluster Example
=============================================

This example uses shows how to connect two ServiceMix containers in a cluster.

To start the servicemix servers using the sample configurations, just run:

../../bin/servicemix servicemix-poller.xml

and

../../bin/servicemix servicemix-writer.xml

This will start a the two ServiceMix containers.

One server have a file poller component, and the other one a file writer component.
Just drop the test-file.xml in the inbox directory.
It should be moved to the outbox directory.

