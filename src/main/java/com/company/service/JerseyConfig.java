package com.company.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.company.service.rest.UserDetailsResource;

@Component
@ApplicationPath("service")
public class JerseyConfig extends ResourceConfig {

    @Autowired
    public JerseyConfig(Environment env) {
        // fast JSON
	    register(JacksonFeature.class);

	    // request logging
	    Logger jerseyLogger = Logger.getLogger(env.getRequiredProperty("logging.jersey.loggername"));
	    register(new LoggingFeature(jerseyLogger, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY,
	        env.getRequiredProperty("logging.jersey.maxentitysize", Integer.class)));

        /* From the Spring Boot documentation:
         * "Jersey’s support for scanning executable archives is rather limited. For example,
         * it cannot scan for endpoints in a package found in WEB-INF/classes when running an
         * executable war file. To avoid this limitation, the 'packages'-method should not be used,
         * and endpoints should be registered individually by using the 'register'-method, as shown
         * in the preceding example."
         * The same is true for executable jar files. This is why here all resources are registered one
         * after another. */
	    register(UserDetailsResource.class);
    }
}
