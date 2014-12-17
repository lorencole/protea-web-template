package cc.protea.foundation.example;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.math.NumberUtils;

import cc.protea.foundation.integrations.RedisUtil;
import cc.protea.foundation.utility.ProteaUser;
import cc.protea.foundation.utility.UserUtil;
import cc.protea.foundation.utility.services.GenericResponse;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/example/users")
@Api(value = "/example/users", description = "User Example")
@Produces(MediaType.APPLICATION_JSON)
public class UserExample {

	@GET
	@ApiOperation(value = "List all user records")
	public List<ProteaUser> list() {
		return RedisUtil.execute(redis -> {
			List<ProteaUser> users = new ArrayList<ProteaUser>();
			redis.smembers("users").forEach(userId -> users.add(UserUtil.getProteaUser(userId)));
			return users;
		});
	}

	@POST
	@Path("/create")
	@ApiOperation(value = "Create a new user")
	public GenericResponse create(
			@QueryParam("emailAddress") final String emailAddress,
			@QueryParam("password") final String password) {
		Integer userId = UserUtil.add();
		UserUtil.addEmail(userId, emailAddress);
		UserUtil.setPassword(userId, password);
		return GenericResponse.success();
	}

	@POST
	@Path("/reset")
	@ApiOperation(value = "Reset all users")
	public GenericResponse reset() {
		RedisUtil.jedis(jedis ->
			jedis.smembers("users").forEach(userId ->
				UserUtil.remove(NumberUtils.createInteger(userId))));
		return GenericResponse.success();
	}

}