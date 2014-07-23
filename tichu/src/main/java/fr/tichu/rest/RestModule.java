package tichu.rest;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
*
 */
public class RestModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(GameRest.class);
    }
}
