package model.request;

import api.IntPointSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import model.IntPoint;
import model.Point;

public class RequestTransport {
    public Point acceleration;
    public boolean activateShield;
    @JsonSerialize(using = IntPointSerializer.class)
    public Point attack;
    public String id;

    public RequestTransport setAcceleration(Point acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public RequestTransport setActivateShield(boolean activateShield) {
        this.activateShield = activateShield;
        return this;
    }

    public RequestTransport setAttack(Point attack) {
        this.attack = attack;
        return this;
    }

    public RequestTransport setId(String id) {
        this.id = id;
        return this;
    }
}
