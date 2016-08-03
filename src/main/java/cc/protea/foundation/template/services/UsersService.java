package cc.protea.foundation.template.services;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.foundation.model.ProteaException;
import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.foundation.util.GravatarUtil;
import cc.protea.foundation.util.KeyUtil;
import cc.protea.platform.ProteaUser;
import cc.protea.platform.SessionUtil;
import cc.protea.platform.UserUtil;
import cc.protea.platform.services.GenericResponse;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.login.AuthenticationController;
import cc.protea.platform.services.login.AuthenticationRequest;
import cc.protea.platform.services.login.AuthenticationResponse;

@Path("/users")
@Api(value = "/users", description = "User Services")
@Produces(MediaType.APPLICATION_JSON)
public class UsersService extends ProteaService<TemplateUser> {

	@GET
	@Path("/current")
	@ApiOperation(value = "Get the current user", response = TemplateUser.class)
	@RolesAllowed("loggedIn")
	public TemplateUser current() {
		TemplateUser user = DatabaseUtil.get(h -> {
			return UserUtil.getProteaUser(h, getUserId());
		});
		user.profilePictureUrl = GravatarUtil.getImageUrl(user.correspondenceEmailAddress, 200);
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
			ProteaUser user = DatabaseUtil.get( h -> {
				Long id = UserUtil.getUserIdByEmail(h, request.emailAddress);
				return UserUtil.getProteaUser(h, id);
			});
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
		Long userId = UserUtil.add(request, response);
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
		if (user == null || StringUtils.isBlank(user.correspondenceEmailAddress)) {
			throw new ProteaException(Status.BAD_REQUEST, "Email address is required");
		}
		if (StringUtils.isBlank(password)) {
			throw new ProteaException(Status.BAD_REQUEST, "Password is required");
		}
		UserUtil.setPassword(getUserId(), password);
		return GenericResponse.success();
	}
	
	@GET
	@Path("/{key}")
	@ApiOperation(value = "Get a user by their id", response = TemplateUser.class)
	public TemplateUser get(@PathParam("key") String key) {
		Long id = KeyUtil.toKey(key);
		TemplateUser user = DatabaseUtil.get(h -> {
			TemplateUser u = TemplateUser.select(id);
			if(u != null) {
				u.fetchEmailAddresses();
			}
			return u;
		});
		
		if(user == null) {
			throw new ProteaException("User not found");
		}
		
		return user;
	}
	
	@GET
	@Path("/{key}/roles")
	@ApiOperation(value = "List all roles assigned to a user")
	public Set<String> getUserRoles(@PathParam("key") String key) {
		Long id = KeyUtil.toKey(key);
		return UserUtil.getUserRoles(id);
	}
	
	@GET
	@Path("/list")
	@ApiOperation(value = "Get all users")
	public List<TemplateUser> list() {
		return TemplateUser.selectAll();
	}
	
	// TODO: Search
	
	@PUT
	@Path("/{key}")
	public GenericResponse update(@PathParam("key") String key, @ApiParam(required=true) TemplateUser user) {
		user.id = KeyUtil.toKey(key);
		user.update();
		return GenericResponse.success();
	}
	
	@PUT
	@Path("/{key}/roles")
	@ApiOperation(value = "Update user roles")
	public GenericResponse update(@PathParam("key") String key, @ApiParam(required=true) Set<String> roles) {
		Long userId = KeyUtil.toKey(key);
		UserUtil.removeAllRoles(userId);
		for(String role : roles) {
			UserUtil.addRole(userId, role);
		}
		return GenericResponse.success();
	}

	
}