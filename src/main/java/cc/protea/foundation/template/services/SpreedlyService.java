package cc.protea.foundation.template.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import cc.protea.foundation.integrations.SpreedlyUtil;
import cc.protea.spreedly.model.SpreedlyGatewayAccount;
import cc.protea.spreedly.model.SpreedlyGatewayProvider;

/**
 * 
 * API to communicate directly with spreedly - used in adminko.js to get a list of all spreedly gateways
 *
 */

@Path("/spreedly")
@Api(value = "/spreedly", description = "Spreedly")
@Produces(MediaType.APPLICATION_JSON)
public class SpreedlyService {

	@GET
	@Path("/providers/list")
	@ApiOperation(value = "List all Spreedly provider definitions", response = SpreedlyGatewayProvider.class, responseContainer = "Map")
	public Map<String, SpreedlyGatewayProvider> listGateways() {
		List<SpreedlyGatewayProvider> providers = SpreedlyUtil.getSpreedly().listGatewayProviders();
		Map<String, SpreedlyGatewayProvider> providerMap = new HashMap<>();
		for(SpreedlyGatewayProvider provider : providers) {
			providerMap.put(provider.gatewayType, provider);
		}
		return providerMap;
	}

	@GET
	@Path("/gateways/{token}")
	@ApiOperation(value = "Get basic information about a gateway account", response = SpreedlyGatewayAccount.class)
	@RolesAllowed("loggedIn")
	public Object get(@PathParam("token") final String token) {
		return SpreedlyUtil.getSpreedly().getGatewayAccount(token);
	}

	@GET
	@Path("/gateways/all")
	@ApiOperation(value = "Fetch a page of gateways", response = SpreedlyGatewayAccount.class, responseContainer="List")
	@RolesAllowed("loggedIn")
	public List<SpreedlyGatewayAccount> getAll(@QueryParam("sinceToken") String sinceToken) {
		sinceToken = StringUtils.trimToNull(sinceToken);
		return SpreedlyUtil.getSpreedly().listGatewayAccounts(sinceToken);
	}

}
