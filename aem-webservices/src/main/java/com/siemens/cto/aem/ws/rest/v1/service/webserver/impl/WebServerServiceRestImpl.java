package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerControlService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.exception.TemplateNotFoundException;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.WebServerIdsParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.WebServerServiceRest;

public class WebServerServiceRestImpl implements WebServerServiceRest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerServiceRestImpl.class);

    private final WebServerService webServerService;
    private final WebServerControlService webServerControlService;
    private final WebServerCommandService webServerCommandService;
    private final StateService<WebServer, WebServerReachableState> webServerStateService;

    public WebServerServiceRestImpl(final WebServerService theWebServerService,
                                    final WebServerControlService theWebServerControlService,
                                    final WebServerCommandService theWebServerCommandService,
                                    final StateService<WebServer, WebServerReachableState> theWebServerStateService) {
        webServerService = theWebServerService;
        webServerControlService = theWebServerControlService;
        webServerCommandService = theWebServerCommandService;
        webServerStateService = theWebServerStateService;
    }

    @Override
    public Response getWebServers(final Identifier<Group> aGroupId,
                                  final PaginationParamProvider paginationParamProvider) {
        final List<WebServer> webServers;
        if (aGroupId == null) {
            webServers = webServerService.getWebServers(paginationParamProvider.getPaginationParameter());
            return ResponseBuilder.ok(webServers);
        }
        webServers = webServerService.findWebServers(aGroupId,
                paginationParamProvider.getPaginationParameter());
        return ResponseBuilder.ok(webServers);
    }

    @Override
    public Response getWebServer(final Identifier<WebServer> aWsId) {
        LOGGER.debug("Get WS requested: {}", aWsId);
        return ResponseBuilder.ok(webServerService.getWebServer(aWsId));
    }

    @Override
    public Response createWebServer(final JsonCreateWebServer aWebServerToCreate,
                                    final AuthenticatedUser aUser) {
        LOGGER.debug("Create WS requested: {}", aWebServerToCreate);
        return ResponseBuilder.created(webServerService.createWebServer(aWebServerToCreate.toCreateWebServerCommand(),
                                                                        aUser.getUser()));
    }

    @Override
    public Response updateWebServer(final JsonUpdateWebServer aWebServerToCreate,
                                    final AuthenticatedUser aUser) {
        LOGGER.debug("Update WS requested: {}", aWebServerToCreate);
        return ResponseBuilder.ok(webServerService.updateWebServer(aWebServerToCreate.toUpdateWebServerCommand(),
                                                                   aUser.getUser()));
    }

    @Override
    public Response removeWebServer(final Identifier<WebServer> aWsId) {
        LOGGER.debug("Delete WS requested: {}", aWsId);
        webServerService.removeWebServer(aWsId);
        return ResponseBuilder.ok();
    }

    @Override
    public Response controlWebServer(final Identifier<WebServer> aWebServerId,
                                     final JsonControlWebServer aWebServerToControl,
                                     final AuthenticatedUser aUser) {
        LOGGER.debug("Control Web Server requested: {} {}", aWebServerId, aWebServerToControl);
        final WebServerControlHistory controlHistory = webServerControlService.controlWebServer(
                new ControlWebServerCommand(aWebServerId, aWebServerToControl.toControlOperation()),
                aUser.getUser());
        final ExecData execData = controlHistory.getExecData();
        if (execData.getReturnCode().wasSuccessful()) {
            return ResponseBuilder.ok(controlHistory);
        } else {
            throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL,
                                             execData.getStandardError());
        }
    }

    @Override
    public Response generateConfig(final String aWebServerName, final Boolean withSsl) {

        try {
            String httpdConfStr = webServerService.generateHttpdConfig(aWebServerName, withSsl);
            return Response.ok(httpdConfStr).build();
        } catch (TemplateNotFoundException e) {
            throw new InternalErrorException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND,
                                             e.getMessage(),
                                             e);
        }
    }

    @Override
    public Response generateLoadBalancerConfig(final String aWebServerName) {
        return Response.ok(webServerService.generateWorkerProperties(aWebServerName)).build();
    }

    @Override
    public Response getCurrentWebServerStates(final WebServerIdsParameterProvider webServerIdsParameterProvider) {
        LOGGER.debug("Current WebServer states requested : {}", webServerIdsParameterProvider);
        final Set<Identifier<WebServer>> webServerIds = webServerIdsParameterProvider.valueOf();
        final Set<CurrentState<WebServer, WebServerReachableState>> currentWebServerStates;

        if (webServerIds.isEmpty()) {
            currentWebServerStates = webServerStateService.getCurrentStates(PaginationParameter.all());
        } else {
            currentWebServerStates = webServerStateService.getCurrentStates(webServerIds);
        }

        return ResponseBuilder.ok(currentWebServerStates);
    }

    @Override
    public Response getHttpdConfig(Identifier<WebServer> aWebServerId) {
        try {
            return Response.ok(webServerCommandService.getHttpdConf(aWebServerId)).build();
        } catch (CommandFailureException cmdFailEx) {
            LOGGER.warn("Command Failure Occurred", cmdFailEx);
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         new FaultCodeException(AemFaultType.REMOTE_COMMAND_FAILURE,
                                                                cmdFailEx.getMessage()));
        }
    }
}
