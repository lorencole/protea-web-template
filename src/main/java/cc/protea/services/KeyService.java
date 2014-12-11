package cc.protea.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import cc.protea.foundation.util.KeyUtil;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/key")
@Api(value = "/key", description = "Key")
@Produces(MediaType.TEXT_PLAIN)
public class KeyService {

	@GET
	@Path("/{id}")
	@ApiOperation(value = "Translate a key", response = String.class)
	public String transalate(@PathParam("id") final String id) {
		if (StringUtils.isNumeric(id)) {
			return KeyUtil.toString(Long.valueOf(id)) + "\n" + KeyUtil.toAlphaString(Long.valueOf(id));
		}
		Long key = KeyUtil.toKey(id);
		return key == null ? null : key.toString();
	}

}