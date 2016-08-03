package cc.protea.foundation.template.services;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.platform.UserUtil;
import cc.protea.platform.services.ProteaService;

@Path("/roles")
@Api(value = "/roles", description = "Role Services")
@Produces(MediaType.APPLICATION_JSON)
public class RolesService extends ProteaService<TemplateUser> {
	
	@GET
	@Path("/{role}/users")
	@ApiOperation(value = "List all users in a role")
	public Set<TemplateUser> listUsersInRole(@PathParam("role") String role) {
		Set<Long> userIds = UserUtil.getUserIdsInRole(role);
		return UserUtil.getProteaUsers(userIds);
	}

}
