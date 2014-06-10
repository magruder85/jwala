package com.siemens.cto.aem.service.configuration.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jndi.JndiLocatorDelegate;

@Configuration
public class AemJmsConfig {

    @Bean
    public ConnectionFactory getConnectionFactory() {
        return lookup("jms/toc-cf", ConnectionFactory.class);
    }

    @Bean
    public Destination getJvmStateDestination() {
        //TODO change to a queue specific to JVM states maybe?
        //TODO change the JNDI name to something better
        return lookup("jms/toc-status", Destination.class);
    }

    @Bean
    public Destination getJvmStateNotificationDestination() {
        return lookup("jms/toc-jvm-state-notification", Destination.class);
    }

    @Bean
    public JmsTemplate getJmsTemplate() {
        return new JmsTemplate(getConnectionFactory());
    }

    protected <T> T lookup(final String aJndiReference,
                           final Class<T> aT) {
        try {
            return JndiLocatorDelegate.createDefaultResourceRefLocator().lookup(aJndiReference,
                                                                                aT);
        } catch (final NamingException ne) {
            throw new RuntimeException("Naming Exception while looking up JMS resources", ne);
        }
    }
}
