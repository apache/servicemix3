package org.apache.servicemix.samples.wsdl_first;

import org.apache.servicemix.samples.wsdl_first.types.GetPersonRequest;
import org.apache.servicemix.samples.wsdl_first.types.GetPersonResponse;

public class PersonImpl implements Person {

    public GetPersonResponse getPerson(GetPersonRequest payload) throws UnknownPersonFault {
        String personId = payload.getPersonId();
        if (personId == null || personId.length() == 0) {
            org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault fault = new org.apache.servicemix.samples.wsdl_first.types.UnknownPersonFault();
            fault.setPersonId(personId);
            throw new UnknownPersonFault(null, fault);
        }
        GetPersonResponse response = new GetPersonResponse();
        response.setPersonId(payload.getPersonId());
        response.setName("Guillaume");
        response.setSsn("000-000-0000");
        return null;
    }

}
