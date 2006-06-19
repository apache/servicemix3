Welcome to the ServiceMix HTTP Server Binding Example
=====================================================

This example shows how to use the HTTP bindings to handle a simple http
post.

To start the servicemix server using the sample configuration, just run:

ant setup
../../bin/servicemix servicemix.xml

This will start http server on port 8912 and wait for a request to come in
which It then forwards to http://64.124.140.30/soap for processing.

A simple HTTP client is provided so that a simple post can be set to the server.
The client is built and run from source using Ant, http://ant.apache.org. 
Just execute 'ant' from the current directory to run the HTTP client.

For more information see:
http://incubator.apache.org/servicemix/HTTP
