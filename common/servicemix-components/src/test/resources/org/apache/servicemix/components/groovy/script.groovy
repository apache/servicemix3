println "Called with inbound message $inMessage"

// lets output some message properties
def me1 = deliveryChannel.createExchangeFactoryForService(new javax.xml.namespace.QName("http://servicemix.org/cheese/", "service1")).createInOutExchange()
def in1 = me1.createMessage()
in1.bodyText = inMessage.bodyText
me1.setMessage(in1, "in")
deliveryChannel.sendSync(me1)
println "Received: " + me1.getMessage("out").bodyText

// lets output some message properties
def me2 = deliveryChannel.createExchangeFactoryForService(new javax.xml.namespace.QName("http://servicemix.org/cheese/", "service2")).createInOutExchange()
def in2 = me2.createMessage()
in2.bodyText = me1.getMessage("out").bodyText
me2.setMessage(in2, "in")
deliveryChannel.sendSync(me2)
println "Received: " + me2.getMessage("out").bodyText

// lets output some non-xml body
outMessage.bodyText = me2.getMessage("out").bodyText

