package com.company.service.rest;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.company.service.business.UserManager;
import com.company.service.model.User;
import com.company.service.model.UserDetailsDTO;
import com.company.service.security.AuthManager;

@Path("user")
public class UserDetailsResource {
    private final static Logger log = LogManager.getLogger(UserDetailsResource.class);

    public final static String PARAM_ID = "id";

    @Autowired
    private AuthManager authManager;
    @Autowired
    private UserManager userManager;

    @GET
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDetails(@QueryParam(PARAM_ID) Long userId) {
        log.debug("User details for {} have been requested.", userId);

        if (userId == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // TODO maybe use @NameBinding and a filter to do this the null check could be integrated, too
        // However every endpoint would have to get the userId the same way
        if (!authManager.checkAuthenticationStatus(userId)) {
            log.debug("Current user is not authorised to change data of user {}", userId);
            return Response.status(Status.UNAUTHORIZED).build();
        }

        Optional<User> userOpt = userManager.getUser(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDetailsDTO dto = new UserDetailsDTO(user);
            return Response.ok(dto).build();
        } else {
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("public")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPublicUserDetails(@QueryParam(PARAM_ID) Long userId) {
        log.debug("Public user details for {} have been requested.", userId);

        if (userId == null) {
            log.error("Public user details have been requested with a null user-ID.");
            return Response.status(Status.BAD_REQUEST).build();
        }

        Optional<User> userOpt = userManager.getUser(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserDetailsDTO dto = new UserDetailsDTO();
            dto.setId(user.getId());
            dto.setNickname(user.getNickname());

            return Response.ok(dto).build();
        } else {
            log.error("Request for public user details of user {} could not be served, because the user "
                      + "could not be found", userId);
            return Response.status(Status.BAD_REQUEST).build();
        }
    }
}