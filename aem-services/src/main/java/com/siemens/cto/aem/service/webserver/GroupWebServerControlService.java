package com.siemens.cto.aem.service.webserver;

import java.util.List;

import com.siemens.cto.aem.domain.model.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;

public interface GroupWebServerControlService {

    GroupControlHistory controlGroup(final ControlGroupWebServerCommand aCommand, final User aUser);
    
    GroupControlHistory dispatchCommandComplete(List<WebServerDispatchCommandResult> results);

}
