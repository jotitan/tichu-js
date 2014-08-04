package fr.titan.tichu.ws;

import org.atmosphere.config.service.ManagedService;
import org.atmosphere.cpr.AtmosphereResponse;

/**
 * User: Titan
 * Date: 03/08/14
 * Time: 18:36
 */
// http://cdnjs.cloudflare.com/ajax/libs/atmosphere/2.1.2/atmosphere.min.js
@ManagedService(path = "/test_atmo")
public class TestAtmosphere {

    @on
    public void onMessage(AtmosphereResponse response, String message){

    }

}

