package org.openrepose.filters.versioning;

import org.openrepose.core.spring.ReposeSpringProperties;
import org.openrepose.core.systemmodel.SystemModel;
import org.openrepose.core.filter.FilterConfigHelper;
import org.openrepose.core.filter.logic.impl.FilterLogicHandlerDelegate;
import org.openrepose.core.services.config.ConfigurationService;
import org.openrepose.core.services.reporting.metrics.MetricsService;
import org.openrepose.filters.versioning.config.ServiceVersionMappingList;
import org.openrepose.services.healthcheck.HealthCheckService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import java.io.IOException;
import java.net.URL;

@Named
public class VersioningFilter implements Filter {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(VersioningFilter.class);
    private static final String DEFAULT_CONFIG = "versioning.cfg.xml";
    private String config;
    private VersioningHandlerFactory handlerFactory;
    private final ConfigurationService configurationService;
    private final MetricsService metricsService;
    private final HealthCheckService healthCheckService;
    private final String clusterId;
    private final String nodeId;

    @Inject
    public VersioningFilter(ConfigurationService configurationService,
                            MetricsService metricsService,
                            HealthCheckService healthCheckService,
                            @Value(ReposeSpringProperties.NODE.CLUSTER_ID)String clusterId,
                            @Value(ReposeSpringProperties.NODE.NODE_ID)String nodeId) {
        this.configurationService = configurationService;
        this.metricsService = metricsService;
        this.healthCheckService = healthCheckService;
        this.clusterId = clusterId;
        this.nodeId = nodeId;
    }

    @Override
    public void destroy() {
        configurationService.unsubscribeFrom("system-model.cfg.xml", handlerFactory);
        configurationService.unsubscribeFrom(config, handlerFactory);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        new FilterLogicHandlerDelegate(request, response, chain).doFilter(handlerFactory.newHandler());
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = new FilterConfigHelper(filterConfig).getFilterConfig(DEFAULT_CONFIG);
        LOG.info("Initializing filter using config " + config);
        handlerFactory = new VersioningHandlerFactory(clusterId, nodeId, metricsService, healthCheckService);
        configurationService.subscribeTo(filterConfig.getFilterName(),"system-model.cfg.xml", handlerFactory, SystemModel.class);
        URL xsdURL = getClass().getResource("/META-INF/schema/config/versioning-configuration.xsd");
        configurationService.subscribeTo(filterConfig.getFilterName(), config, xsdURL, handlerFactory, ServiceVersionMappingList.class);
    }
}
