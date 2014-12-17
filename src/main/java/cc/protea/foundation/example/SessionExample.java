package cc.protea.foundation.example;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import cc.protea.foundation.integrations.RedisUtil;
import cc.protea.foundation.utility.SessionUtil;
import cc.protea.foundation.utility.services.GenericResponse;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/example/sessions")
@Api(value = "/example/sessions", description = "Session Example")
@Produces(MediaType.APPLICATION_JSON)
public class SessionExample {

	@GET
	@ApiOperation(value = "List all sessions")
	public Map<String, String> list() {
		return RedisUtil.execute(redis -> {
			final Map<String, String> map = new HashMap<String, String>();
			redis.zrange("sessions", Integer.MIN_VALUE, Integer.MAX_VALUE).forEach(key -> map.put(key, SessionUtil.getUserId(key).toString()));
			return map;
		});
	}

	@Path("/reset")
	@ApiOperation(value = "Reset all sessions")
	public GenericResponse reset() {
		RedisUtil.jedis(jedis -> jedis.smembers("sessions").forEach(token -> SessionUtil.remove(token)));
		return GenericResponse.success();
	}

}