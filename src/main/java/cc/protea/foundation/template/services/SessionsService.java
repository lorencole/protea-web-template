package cc.protea.foundation.template.services;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import cc.protea.platform.services.GenericResponse;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.login.AuthenticationRequest;
import cc.protea.platform.services.login.AuthenticationResponse;
import cc.protea.platform.services.login.SessionController;

@Path("/sessions")
@Api(value = "/sessions", description = "Session Services")
@Produces(MediaType.APPLICATION_JSON)
public class SessionsService extends ProteaService {

	@POST
	@Path("/login")
	@ApiOperation(value = "Start a new session", response = AuthenticationResponse.class)
	public AuthenticationResponse login(@ApiParam(required = true) final AuthenticationRequest request) {
		return SessionController.login(request, getUserId(), getSessionToken());
	}

	@POST
	@Path("/logout")
	@ApiOperation(value = "Ends a session")
	public GenericResponse logout() {
		return GenericResponse.success();
	}
}