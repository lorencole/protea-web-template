package cc.protea.foundation.template.services;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.foundation.model.ProteaException;
import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.foundation.util.KeyUtil;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransaction;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransactionUtil;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransactionUtil.Purchase;

@Path("/transactions")
@Api(value = "/transactions", description = "Transaction Services")
@Produces(MediaType.APPLICATION_JSON)
public class CreditCardTransactionService extends ProteaService<TemplateUser> {

	// List Transactions
	@GET
	@Path("/list")
	@ApiOperation(value = "View transaction details")
	public List<CreditCardTransaction> list() {
		return DatabaseUtil.get(h -> {
			return h.createQuery("SELECT * FROM credit_card_transaction")
				.map(CreditCardTransaction.mapper)
				.list();
		});
	}
	
	// Search
	
	// Display individual transaction
	@GET
	@Path("/{transactionKey}")
	@ApiOperation(value = "View transaction details")
	public CreditCardTransaction get(@PathParam("transactionKey") String transactionKey) {
		CreditCardTransaction txn = CreditCardTransaction.select(KeyUtil.toKey(transactionKey));
		if(txn == null) {
			throw new ProteaException("Transaction not found");
		}
		return txn;
	}
	
	// Process transaction
	@POST
	@Path("/purchase")
	@ApiOperation(value = "Charge a card")
	//@RolesAllowed("loggedIn")
	public CreditCardTransaction purchase(@ApiParam(required=true) Purchase purchase) {
		CreditCardTransaction result = CreditCardTransactionUtil.purchase(purchase);
		return result;
	}
	
	// Refund transaction
	@POST
	@Path("/refund/{transactionToRefundKey}")
	@ApiOperation(value = "Charge a card")
	//@RolesAllowed("loggedIn")
	public CreditCardTransaction refund(@PathParam("transactionToRefundKey") String transactionToRefundKey, @ApiParam(required=true) Integer amount) {
		CreditCardTransaction result = CreditCardTransactionUtil.refund(transactionToRefundKey, amount);
		return result;
	}
}
