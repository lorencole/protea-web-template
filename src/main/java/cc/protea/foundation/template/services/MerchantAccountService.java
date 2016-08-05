package cc.protea.foundation.template.services;

import java.util.List;

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
import cc.protea.foundation.integrations.SpreedlyUtil;
import cc.protea.foundation.model.ProteaException;
import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.foundation.util.KeyUtil;
import cc.protea.platform.MerchantAccountUtil;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.creditcardtransaction.MerchantAccount;
import cc.protea.spreedly.model.SpreedlyGatewayAccount;


/**
 * {
    "initial": "boolean",
    -- Read only, if true no spreedly token is associated with this account
    
    "name": "string",
    -- Friendly name for the merchant account
    
    "thirdPartyVault": "boolean",
    -- If true retained payment methods will be vaulted with the provider as well as spreedly
    
    "hidden": "boolean",
    -- If true account will not be included in /merchantAccounts/list
    
    "provider": "string",
    -- The key for the gateway provider. Gateway provider definitions can be retrieved through /spreedly/providers/list
    -- Use this value to look up the definition, including the human readable provider name, and the list of possible credentials
    
    "credentials": "Map[string,string]",
    -- Provider specific credentials. 
    -- Privileged credentials, like passwords are never retrieved from the server,
    -- but may be including in PUT /merchantAccounts/{merchantAccountKey} to be saved in Spreedly

    "redacted": "boolean",
    -- Readonly. A redacted gateway has been 'deleted' in Spreedly
   }   
 *
 */

@Path("/merchantAccounts")
@Api(value = "/merchantAccounts", description = "Merchant Account Services")
@Produces(MediaType.APPLICATION_JSON)
public class MerchantAccountService extends ProteaService<TemplateUser>{
	
	@GET
	@Path("/list")
	@ApiOperation(value = "List all merchant accounts", response = MerchantAccount.class, responseContainer = "List")
	@RolesAllowed("loggedIn")
	public List<MerchantAccount> listMerchantAccounts() {
		return DatabaseUtil.get(h -> {
			return h.createQuery("SELECT * FROM merchant_account WHERE hidden != true")
				.map(MerchantAccount.mapper)
				.list();
		});
	}
	
	@GET
	@Path("/{merchantAccountKey}")
	@ApiOperation(value = "Fetch merchant account details")
	@RolesAllowed("loggedIn")
	public MerchantAccount fetchMerchantAccount(@PathParam("merchantAccountKey") String merchantAccountKey) {
		MerchantAccount merchantAccount = MerchantAccount.select(merchantAccountKey);
		if(StringUtils.isBlank(merchantAccount.token)) {
			// Account is still initialized, return with no details
			return merchantAccount;
		}
		SpreedlyGatewayAccount sga = SpreedlyUtil.getSpreedly().getGatewayAccount(merchantAccount.token);
		return merchantAccount = MerchantAccountUtil.fillMerchantAccount(sga, merchantAccount);
	}
	
	@POST
	@Path("/create")
	@ApiOperation(value = "Create a new merchant account, no push to spreedly")
	@RolesAllowed("loggedIn")
	public MerchantAccount create(@ApiParam(required = true) MerchantAccount merchantAccount) {
		merchantAccount.id = null;
		merchantAccount.name = StringUtils.trimToNull(merchantAccount.name);
		if (merchantAccount.name == null) {
			throw new ProteaException(Status.NOT_ACCEPTABLE, "Name should not be blank");
		}
		merchantAccount.insert();
		return merchantAccount;
	}
	
	@PUT
	@Path("/{merchantAccountKey}")
	@ApiOperation(value = "Update a gateway, pushes changes to spreedly as well")
	@RolesAllowed("loggedIn")
	public MerchantAccount update(@PathParam("merchantAccountKey") String merchantAccountKey, @ApiParam(required = true) MerchantAccount merchantAccount) {
		final MerchantAccount current = MerchantAccount.select(merchantAccountKey);
		if (current == null) {
			throw new ProteaException(Status.NOT_FOUND, "Payment credentials " + merchantAccountKey + " does not exist");
		}
		
		merchantAccount.id = KeyUtil.toKey(merchantAccountKey); // Never edit some other gateway
		merchantAccount.token = current.token;	// Don't overwrite the token
		SpreedlyGatewayAccount sga = MerchantAccountUtil.convert(merchantAccount);

		// Update Spreedly
		if (merchantAccount.token == null) {
			// if we don't already have a token it's a new gateway and we need to create it with spreedly
			sga = SpreedlyUtil.getSpreedly().create(sga);
		} else {  
			// otherwise it's an existing gateway and we will update it, but only if the new credentials aren't empty
			if ( merchantAccount.credentials != null && ! merchantAccount.credentials.isEmpty() ) {
				sga = SpreedlyUtil.getSpreedly().update(sga);
			}
		}
		// Also update the database
		MerchantAccountUtil.fillMerchantAccount(sga, merchantAccount);
		merchantAccount.update();
		
		return merchantAccount;
	}

	// Add by token
	@POST
	@Path("/import/{token}")
	@ApiOperation(value = "Import a merchant account from spreedly")
	@RolesAllowed("loggedIn")
	public MerchantAccount create(@PathParam("token") String token, @ApiParam(required = true) String name) {
		SpreedlyGatewayAccount sga = SpreedlyUtil.getSpreedly().getGatewayAccount(token);
		MerchantAccount merchantAccount = MerchantAccountUtil.convert(sga);
		merchantAccount.name = StringUtils.trimToNull(name);
		merchantAccount.insert();
		return merchantAccount;
	}
	
	// TODO: Sync spreedly account list
	
}
