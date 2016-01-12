package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl webServerControlService;

    @Mock
    private WebServerService webServerService;

    @Mock
    private RemoteCommandExecutor<WebServerControlOperation> commandExecutor;

    @Mock
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    @Mock
    private Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;

    @Captor
    private ArgumentCaptor<SetStateRequest<WebServer, WebServerReachableState>> setStateCommandCaptor;

    @Mock
    private HistoryService mockHistoryService;

    private User user;

    @Before
    public void setup() {
        webServerControlService = new WebServerControlServiceImpl(webServerService,
                commandExecutor,
                webServerStateService,
                webServerReachableStateMap,
                mockHistoryService);

        user = new User("unused");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        String wsName = "mockWebServerName";
        String wsHostName = "mockWebServerHost";
        final ControlWebServerRequest controlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer webServer = mock(WebServer.class);

        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(webServer.getName()).thenReturn(wsName);
        when(webServer.getHost()).thenReturn(wsHostName);
        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;
        final ClientHttpResponse mockClientHttpResponse = mock(ClientHttpResponse.class);

        when(controlWebServerRequest.getWebServerId()).thenReturn(webServerId);
        when(controlWebServerRequest.getControlOperation()).thenReturn(controlOperation);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(webServerStateService.getCurrentState(webServerId)).thenReturn(new CurrentState<WebServer, WebServerReachableState>(webServerId, WebServerReachableState.WS_UNREACHABLE, DateTime.now(), StateType.WEB_SERVER));

        webServerControlService.controlWebServer(controlWebServerRequest, user);

        verify(controlWebServerRequest, times(1)).validate();

        verify(commandExecutor, times(1)).executeRemoteCommand(eq(wsName),
                eq(wsHostName),
                eq(WebServerControlOperation.START),
                any(WindowsWebServerPlatformCommandProvider.class)
        );
        verify(webServerStateService, times(1)).setCurrentState(setStateCommandCaptor.capture(),
                eq(user));

        final SetStateRequest<WebServer, WebServerReachableState> actualSetStateCommand = setStateCommandCaptor.getValue();
        assertEquals(webServerId,
                actualSetStateCommand.getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                actualSetStateCommand.getNewState().getState());

        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }
}
