package api;

import model.WorldInfo;
import model.request.Request;

public interface Controller {
    WorldInfo getInfo(Request request);
}
