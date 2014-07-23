package fr.titan.tichu.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * User: Titan Date: 26/03/14 Time: 17:04
 */
public class ServiceRegistration extends Application {
    private Set<Object> singletons = new HashSet<Object>();

    public ServiceRegistration() {
        singletons.add(new GameRest());
    }

    @Override
    public Set<Object> getSingletons() {
        return this.singletons;
    }
}
