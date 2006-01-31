package soap;

public class SimpleService {

    public PingResponse ping(PingRequest request) {
        PingResponse response = new PingResponse();
        response.setMessage("Ping: " + request.getMessage());
        return response;
    }

}
