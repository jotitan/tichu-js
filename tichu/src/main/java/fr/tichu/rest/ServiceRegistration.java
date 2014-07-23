package tichu.rest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import fr.titan.tichu.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Titan
 * Date: 26/03/14
 * Time: 17:04
 */
public class ServiceRegistration extends Application {
    private Set<Object> singletons = new HashSet<Object>();

    final private Logger logger = LoggerFactory.getLogger(ServiceRegistration.class);

    public ServiceRegistration(){
        Injector injector = Guice.createInjector();
        logger.info("Get instance gameservice " + injector.getInstance(GameService.class));
        singletons.add(new GameRest());
    }

    @Override
    public Set<Object> getSingletons() {
        logger.info("Get singletons");
        System.out.println("Titi");
        return this.singletons;
    }
}
