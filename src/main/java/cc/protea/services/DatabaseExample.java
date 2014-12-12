package cc.protea.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;

import cc.protea.foundation.integrations.DatabaseUtil;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/database")
@Api(value = "/database", description = "Database Sample")
@Produces(MediaType.APPLICATION_JSON)
public class DatabaseExample {

	public static class Request {
		public String table;
		public String column;
	}

	public static class Response {
		public String id;
		public long newValue;
	}

	@GET
	@Path("/view")
	@ApiOperation(value = "View the value stored at a key", response = Response.class)
	public Response view(@ApiParam(value = "Column to adjust", required = true) final Request request) {
		Response response = new Response();
		response.id = request.table + "." + request.column;
		response.newValue = DatabaseUtil.single(h -> getValue(h, request.table, request.column));
		return response;
	}

	@POST
	@Path("/increment")
	@ApiOperation(value = "Increase the value stored at a key by 1", response = Response.class)
	public Response increment(@ApiParam(value = "Column to adjust", required = true) final Request request) {
		return adjust(request.table, request.column, 1);
	}

	@POST
	@Path("/decrement")
	@ApiOperation(value = "Decrease the value stored at a key by 1", response = Response.class)
	public Response decrement(@ApiParam(value = "Column to adjust", required = true) final Request request) {
		return adjust(request.table, request.column, -1);
	}

	Response adjust(final String table, final String column, final int by) {
		Response response = new Response();
		response.id = table + "." + column;
		response.newValue = DatabaseUtil.single(h -> {
			h.createStatement("UPDATE :table SET :column = :column + (" + by + ")")
				.bind("table", table)
				.bind("column", column)
				.execute();
			return getValue(h, table, column);
		});
		return response;
	}

	long getValue(final Handle h, final String table, final String column) {
		return h.createQuery("SELECT :column FROM :table")
			.bind("table", table)
			.bind("column", column)
			.map(LongMapper.FIRST)
			.first();
	}

}