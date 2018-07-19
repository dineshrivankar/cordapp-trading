package com.netting.api

import com.netting.flow.TradeFlow.Initiator
import com.netting.schema.TradeSchemaV1
import com.netting.state.TradeState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.IdentityService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.CREATED

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

// This API is accessible from /api/netting. All paths specified below are relative to it.
@Path("netting")
class ExampleApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<ExampleApi>()
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Trade states that exist in the node's vault.
     */
    @GET
    @Path("trades")
    @Produces(MediaType.APPLICATION_JSON)
    fun getTrades() = rpcOps.vaultQueryBy<TradeState>().states

    /**
     * Initiates a flow to agree an Trade between two parties.
     *
     * Once the flow finishes it will have written the Trade to ledger. Both the lender and the borrower will be able to
     * see it when calling /api/netting/trades on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */
    @PUT
    @Path("create-trade")
    fun createTrade(@QueryParam("sellValue") sellValue: Int,@QueryParam("sellCurrency") sellCurrency: String,@QueryParam("buyValue") buyValue: Int,@QueryParam("buyCurrency") buyCurrency: String, @QueryParam("counterParty") counterParty: CordaX500Name?,@QueryParam("tradeStatus") tradeStatus: String): Response {
        if (sellValue <= 0 ) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'tradeValue' must be non-negative.\n").build()
        }
        if (counterParty == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'partyName' missing or has wrong format.\n").build()
        }
        val counterParty = rpcOps.wellKnownPartyFromX500Name(counterParty) ?:
                return Response.status(BAD_REQUEST).entity("Party named $counterParty cannot be found.\n").build()

        return try {
            val signedTx = rpcOps.startTrackedFlow(::Initiator, sellValue,sellCurrency,buyValue,buyCurrency,tradeStatus, counterParty).returnValue.getOrThrow()
            Response.status(CREATED).entity("Transaction id ${signedTx.id} committed to ledger.\n").build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }
	
	/**
     * Displays all Trade states that are created by Party.
     */
    @GET
    @Path("my-trades")
    @Produces(MediaType.APPLICATION_JSON)
    fun mytrades(): Response {
        val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val results = builder {
                var partyType = TradeSchemaV1.PersistentTrade::initiatingPartyName.equal(rpcOps.nodeInfo().legalIdentities.first().name.toString())
                val customCriteria = QueryCriteria.VaultCustomQueryCriteria(partyType)
                val criteria = generalCriteria.and(customCriteria)
                val results = rpcOps.vaultQueryBy<TradeState>(criteria).states
                return Response.ok(results).build()
        }
    }
}