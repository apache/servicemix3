Welcome to the ServiceMix Bridge Example
========================================

This example shows a bridge between the HTTP and JMS protocols,
with an XSLT transformation in between.

First start a ServiceMix server (if not already started) by running
  bin/servicemix
in the root dir of this ditribution.

To start this sample, run:
  mvn install
  cd bridge-sa
  mvn jbi:projectDeploy

For more information on running this example please see:
  http://incubator.apache.org/servicemix/creating-a-protocol-bridge.html

