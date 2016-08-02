package com.siemens.cto.aem.ws.rest.v1.service.balancermanager;

import org.springframework.beans.factory.InitializingBean;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/balancermanager")
@Produces(MediaType.APPLICATION_JSON)
public interface BalancermanagerServiceRest extends InitializingBean {

    @POST
    @Path("/{groupName}")
    Response drainUserGroup(@PathParam("groupName") final String groupName);

    @POST
    @Path("/{groupName}/{webserverName}")
    Response drainUserWebServer(@PathParam("groupName") final String groupName,
                                @PathParam("webserverName") final String webserverName);

    @GET
    @Path("/{groupName}")
    Response getGroup(@PathParam("groupName") final String groupName);

}