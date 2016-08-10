package cc.protea.foundation.template.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.IntegerColumnMapper;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import cc.protea.foundation.integrations.DatabaseUtil;
import cc.protea.foundation.model.ProteaException;
import cc.protea.foundation.template.model.TemplateUser;
import cc.protea.foundation.util.KeyUtil;
import cc.protea.platform.services.Criteria;
import cc.protea.platform.services.ProteaService;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransaction;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransaction.State;
import cc.protea.platform.services.creditcardtransaction.CreditCardTransaction.Type;
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
	
	// Search
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GET
	//@Path("/search")
	@ApiOperation(value = "Search for transactions", response = CreditCardTransaction.class, responseContainer = "List")
	public Response search(
			@QueryParam("afterKey") final String afterKey,
			@QueryParam("from") final Date from,
			@QueryParam("to") final Date to,
			@QueryParam("orderBy") final String orderBy,
			@QueryParam("orderDesc") final Boolean orderDesc,
			@QueryParam("offset") final Integer offset,
			@QueryParam("limit") final Integer limit,
			// CreditCardTransaction specific params
			//@QueryParam("amountMin") final Integer amountMin,
			//@QueryParam("amountMax") final Integer amountMax,
			@QueryParam("merchantAccountKey") final String merchantAccountKey,
			@QueryParam("isRetained") final Boolean isRetained,
			@QueryParam("orderId") final String orderId,
			@QueryParam("type") final Type type,
			@QueryParam("state") final List<State> state) {
		Integer amountMin = null;
		Integer amountMax = null;
		Criteria criteria = new CreditCardTransactionCriteria(afterKey, from, to, orderBy, orderDesc, 
				offset, limit, amountMin, amountMax, merchantAccountKey, isRetained, orderId, type, state);
		ResponseBuilder response = Response.status(200);
		
		List<CreditCardTransaction> list = DatabaseUtil.get(h -> {
			String fromClause = " FROM credit_card_transaction ";
			Query countQuery = h.createQuery("SELECT count(*) " + fromClause + criteria.whereForCount());
			criteria.bind(countQuery);
			Integer count = (Integer) countQuery.map(IntegerColumnMapper.WRAPPER)
				.first();
			response.header("X-Total-Count", count);
			
			Query query = h.createQuery("SELECT * " + fromClause + criteria.where());
			criteria.bind(query);
			return query.map(CreditCardTransaction.mapper).list();
		});
		//TODO: Add link headers
		response.entity(list);
		return response.build();
	}
	
	public static class CreditCardTransactionCriteria extends Criteria {

		Integer amountMin;
		Integer amountMax;
		String merchantAccountKey;
		Boolean isRetained;
		String orderId;
		Type type;
		List<State> state;
		
		
		public CreditCardTransactionCriteria(String afterKey, Date from, Date to, String orderBy, Boolean orderDesc,
				Integer offset, Integer limit, Integer amountMin, Integer amountMax, String merchantAccountKey,
				Boolean isRetained, String orderId, Type type, List<State> state) {
			super(afterKey, from, to, orderBy, orderDesc, offset, limit);
			this.amountMin = amountMin;
			this.amountMax = amountMax;
			this.merchantAccountKey = merchantAccountKey;
			this.isRetained = isRetained;
			this.orderId = orderId;
			this.type = type;
			this.state = state;
			DATE_COLUMN_NAME = "authorize_started";
		}
		
		@Override
		public StringBuilder appendSearch(StringBuilder sql) {
			// TODO amountMin/Max
			// MerchantAccount
			if(StringUtils.isNotBlank(merchantAccountKey)) {
				sql.append(" AND merchant_account_key = :merchantAccountKey ");
			}
			// Retained
			if(isRetained != null) {
				sql.append(" AND retained = :isRetained ");
			}
			// OrderId
			if(StringUtils.isNotBlank(orderId)) {
				sql.append(" AND order_id = :orderId ");
			}
			// Type
			if(type == null ) {
				// By default send one-time and recurring
				sql.append(" AND ref_authorize_returned_id IS NULL ");
			} else {
				switch(type) {
				case REFUND:
					sql.append(" AND ref_authorize_returned_id IS NOT NULL ");
					break;
				case ONE_TIME:
					sql.append(" AND recurring_schedule_key IS NULL AND ref_authorize_returned_id IS NULL ");
					break;
				case RECURRING:
					sql.append(" AND recurring_schedule_key IS NOT NULL ");
					break;
				}
			}
			
			// State
			if(state == null || state.isEmpty()) {
				state = Arrays.asList(State.CONFIRMED, State.SUBMITTED);
			}
			List<String> ssubclauses = new ArrayList<>();
			for (State s : state) {
				ssubclauses.add("state = '" + s.name() + "'");
			}
			sql.append(" AND ( " + StringUtils.join(ssubclauses, " OR ") + ")");
			return sql;
		}
		
		@Override
		@SuppressWarnings("rawtypes")
		public Query bindSearch(Query query) {
			if(StringUtils.isNotBlank(merchantAccountKey)) {
				query.bind("merchantAccountKey", KeyUtil.toKey(merchantAccountKey));
			}
			if(isRetained != null) {
				query.bind("isRetained", isRetained);
			}
			if(StringUtils.isNotBlank(orderId)) {
				query.bind("orderId", orderId);
			}
			
			return query;
		}
		
	}
}
