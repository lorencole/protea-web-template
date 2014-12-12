package cc.protea.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.math.NumberUtils;

import cc.protea.foundation.integrations.RedisUtil;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/redis")
@Api(value = "/redis", description = "Redis Sample")
@Produces(MediaType.APPLICATION_JSON)
public class RedisExample {

	public static class Response {
		public String id;
		public long newValue;
	}

	@GET
	@Path("/{id}")
	@ApiOperation(value = "View the value stored at a key", response = Response.class)
	public Response view(@PathParam("id") final String id) {
		Response response = new Response();
		response.id = id;
		response.newValue = NumberUtils.toLong(RedisUtil.get(id));
		return response;
	}

	@POST
	@Path("/{id}/increment")
	@ApiOperation(value = "Increase the value stored at a key by 1", response = String.class)
	public Response increment(@PathParam("id") final String id) {
		Response response = new Response();
		response.id = id;
		response.newValue = RedisUtil.execute(jedis -> jedis.incr(id));
		return response;
	}

	@POST
	@Path("/{id}/decrement")
	@ApiOperation(value = "Decrease the value stored at a key by 1", response = String.class)
	public Response decrement(@PathParam("id") final String id) {
		Response response = new Response();
		response.id = id;
		response.newValue = RedisUtil.execute(jedis -> jedis.incrBy(id, -1));
		return response;
	}

}