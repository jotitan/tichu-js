package fr.titan.tichu.rest;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * User: Titan
 * Date: 04/05/14
 * Time: 11:50
 */
public class RestModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(GameRest.class);
    }
}
