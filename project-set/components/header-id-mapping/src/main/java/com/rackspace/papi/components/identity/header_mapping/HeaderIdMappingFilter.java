package com.rackspace.papi.components.identity.header_mapping;

import com.rackspace.papi.components.identity.header_mapping.config.HeaderIdMappingConfig;
import com.rackspace.papi.filter.logic.impl.FilterLogicHandlerDelegate;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.context.jndi.ServletContextHelper;
import java.io.IOException;
import javax.servlet.*;

public class HeaderIdMappingFilter implements Filter {

    private HeaderIdMappingHandlerFactory handlerFactory;
    private ConfigurationService configurationManager;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        new FilterLogicHandlerDelegate(request, response, chain).doFilter(handlerFactory.newHandler());
    }

    @Override
    public void destroy() {
        configurationManager.unsubscribeFrom("header-id-mapping.cfg.xml", handlerFactory);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        configurationManager = ServletContextHelper.getPowerApiContext(filterConfig.getServletContext()).configurationService();
        handlerFactory = new HeaderIdMappingHandlerFactory();

        configurationManager.subscribeTo("header-id-mapping.cfg.xml", handlerFactory, HeaderIdMappingConfig.class);
    }
}
