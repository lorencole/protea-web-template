package cc.protea.foundation.template.services;

import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import cc.protea.foundation.model.ProteaException;
import cc.protea.foundation.utility.ProteaUser;
import cc.protea.foundation.utility.SessionUtil;
import cc.protea.foundation.utility.UserUtil;
import cc.protea.foundation.utility.services.GenericResponse;
import cc.protea.foundation.utility.services.ProteaService;
import cc.protea.foundation.utility.services.login.AuthenticationController;
import cc.protea.foundation.utility.services.login.AuthenticationRequest;
import cc.protea.foundation.utility.services.login.AuthenticationResponse;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/users")
@Api(value = "/users", description = "User Services")
@Produces(MediaType.APPLICATION_JSON)
public class UsersService extends ProteaService {

	@GET
	@Path("/current")
	@ApiOperation(value = "Get the current user", response = ProteaUser.class)
	@RolesAllowed("loggedIn")
	public ProteaUser current() {
		ProteaUser user = UserUtil.getProteaUser(getUserId());
		return user;
	}

	@GET
	@Path("/current/sessions")
	@ApiOperation(value = "Get active sessions for the current user", response = String.class, responseContainer="Set")
	@RolesAllowed("loggedIn")
	public Set<String> currentSessions() {
		return SessionUtil.getAllForUser(getUserId());
	}

	@POST
	@Path("/create")
	@ApiOperation(value = "Create a new user", response = AuthenticationResponse.class)
	public AuthenticationResponse create(@ApiParam(required = true) final AuthenticationRequest request) {
		// First, let's see if there's already a matching account
		AuthenticationResponse response = AuthenticationController.authenticate(request);
		if (response.success) {
			return response;
		}
		// Next, let's check to see if there's an account with that email address
		if (request.emailAddress != null) {
			ProteaUser user = UserUtil.getProteaUser(UserUtil.getUserIdByEmail(request.emailAddress));
			if (user != null) {
				response.message = "The user " + request.emailAddress + " already exists; please login with your ";
				if (user.authentication.facebook) {
					response.message += " Facebook account";
				} else if (user.authentication.google) {
					response.message += " Google account";
				} else if (user.authentication.twitter) {
					response.message += " Twitter account";
				} else if (user.authentication.linkedIn) {
					response.message += " LinkedIn account.";
				} else {
					response.message += " password.";
				}
				return response;
			}
		}
		// NOTE: response is now a failure response
		// Let's make sure that we have enough information
		if (	response.facebookUser == null &&
				response.googleUser == null &&
				response.linkedInUser == null &&
				response.twitterUser == null &&
				(request.emailAddress == null || request.password == null)
				) {
			return response;
		}
		// Now add the account
		if (request.firstName == null) {
			request.firstName = response.getFirstName();
		}
		if (request.lastName == null) {
			request.lastName = response.getLastName();
		}
		if (request.name == null) {
			request.name = response.getName();
		}
		Integer userId = UserUtil.add(request, response);
		if (userId == null) {
			throw new ProteaException("Could not create user");
		}
		return AuthenticationResponse.success(SessionUtil.create(userId));
	}


	@PUT
	@Path("/setPassword")
	@RolesAllowed("loggedIn")
	public GenericResponse updatePassword(@ApiParam(name = "password", required = true) final String password) {
		ProteaUser user = getUser();
		if (user == null || StringUtils.isBlank(user.emailAddress)) {
			throw new ProteaException(Status.BAD_REQUEST, "Email address is required");
		}
		if (StringUtils.isBlank(password)) {
			throw new ProteaException(Status.BAD_REQUEST, "Password is required");
		}
		UserUtil.setPassword(getUserId(), password);
		return GenericResponse.success();
	}

}