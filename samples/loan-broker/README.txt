Welcome to the Servicemix Loan Broker example
=============================================

This example is based on the great EIP book 
(http://www.enterpriseintegrationpatterns.com/ComposedMessagingExample.html).
It leverages the BPEL component and some lightweight components provided
by ServiceMix.

First start a ServiceMix server (if not already started) by running
  bin/servicemix
in the root dir of this ditribution.

To run this sample, launch the following commands:
  mvn install
  cd loan-broker-sa
  mvn jbi:projectDeploy

For more information on this example please see
  http://incubator.apache.org/servicemix/loan-broker-example.html
