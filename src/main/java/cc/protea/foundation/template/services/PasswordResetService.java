package cc.protea.foundation.template.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.platform.services.GenericResponse;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.passwordReset.PasswordResetController;
import cc.protea.platform.services.passwordReset.PasswordResetRequest;
import cc.protea.platform.services.passwordReset.PasswordResetValidateResponse;

@Api(value = "/passwords", description = "Password Services")
@Path("/passwords")
@Produces(MediaType.APPLICATION_JSON)
public class PasswordResetService extends ProteaService<TemplateUser> {

	@POST
	@Path("/sendResetEmail")
	@ApiOperation(value = "Sends an email containing a time-limited reset link")
	public GenericResponse sendResetPasswordEmail(@ApiParam(required = true) final PasswordResetRequest request) {
		request.url = StringUtils.defaultIfBlank(request.url, getReferrer());
		return PasswordResetController.reset(request);
	}

	@GET
	@Path("/reset/{token}")
	@ApiOperation(value = "Validates a token and - if valid - returns the user's name to display")
	public PasswordResetValidateResponse getRequest(@PathParam("token") final String token) {
		return PasswordResetController.validate(token);
	}

	@PUT
	@Path("/reset/{token}")
	@ApiOperation(value = "Creates a new password and invalidates the token")
	public GenericResponse setPassword(@PathParam("token") final String token, @ApiParam("password") final String password) {
		return PasswordResetController.updatePassword(token, password);
	}

}