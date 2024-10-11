package model.request;

import java.util.List;

public class Request {
    public List<RequestTransport> transports;

    public Request setTransports(List<RequestTransport> transports) {
        this.transports = transports;
        return this;
    }
}
