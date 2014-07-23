package fr.titan.tichu.ws;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.websocket.server.ServerEndpointConfig;

public class WebSocketConfigurator extends ServerEndpointConfig.Configurator {
    private Injector injector = Guice.createInjector();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return injector.getInstance(endpointClass);
    }
}
