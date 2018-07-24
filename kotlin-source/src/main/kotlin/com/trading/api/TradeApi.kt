package com.trading.api

import com.trading.flow.CounterTradeFlow
import com.trading.flow.TradeFlow.Initiator
import com.trading.flow.CounterTradeFlow.CounterInitiator
import com.trading.flow.TradeFlow
import com.trading.schema.TradeSchemaV1
import com.trading.state.TradeState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.internal.DEPLOYED_CORDAPP_UPLOADER
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.IdentityService
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.*

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

// This API is accessible from /api/trading. All paths specified below are relative to it.
@Path("trading")
class TradeApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    companion object {
        private val logger: Logger = loggerFor<TradeApi>()
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
     * Initiates Create Trade Flow.
     */
    @PUT
    @Path("create-trade")
    fun createTrade(@QueryParam("sellValue") sellValue: Int,@QueryParam("sellCurrency") sellCurrency: String,@QueryParam("buyValue") buyValue: Int,@QueryParam("buyCurrency") buyCurrency: String, @QueryParam("counterParty") counterParty: CordaX500Name?,@QueryParam("tradeStatus") tradeStatus: String): Response {
        if (counterParty == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'Counter partyName' missing or has wrong format.\n").build()
        }
        if (sellValue <= 0 ) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'Sell Value' must be non-negative.\n").build()
        }
        if (buyValue <= 0 ) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'Buy Value' must be non-negative.\n").build()
        }
        val counterParty = rpcOps.wellKnownPartyFromX500Name(counterParty) ?:
                return Response.status(BAD_REQUEST).entity("Counter Party named $counterParty cannot be found.\n").build()

        return try {
            val signedTx = rpcOps.startTrackedFlow(::Initiator, sellValue,sellCurrency,buyValue,buyCurrency,tradeStatus, counterParty).returnValue.getOrThrow()
            Response.status(CREATED).entity("Transaction id ${signedTx.id} committed to ledger.\n").build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Initiates Counter Trade Flow.
     */
    @PUT
    @Path("counter-trade")
    fun counterTrade(@QueryParam("tradeId") tradeId: String,@QueryParam("sellValue") sellValue: Int,@QueryParam("sellCurrency") sellCurrency: String,@QueryParam("buyValue") buyValue: Int,@QueryParam("buyCurrency") buyCurrency: String, @QueryParam("counterParty") counterParty: CordaX500Name?,@QueryParam("tradeStatus") tradeStatus: String): Response {
        if (counterParty == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'Counter partyName' missing or has wrong format.\n").build()
        }
        val counterParty = rpcOps.wellKnownPartyFromX500Name(counterParty) ?:
                return Response.status(BAD_REQUEST).entity("Counter Party named $counterParty cannot be found.\n").build()
        return try {
            val signedTx: SignedTransaction = rpcOps.startFlowDynamic(CounterTradeFlow.CounterInitiator::class.java, sellValue,sellCurrency,buyValue,buyCurrency,tradeStatus,tradeId,counterParty).returnValue.getOrThrow()
            Response.status(CREATED).entity("Transaction id ${signedTx.id} committed to ledger.\n").build()

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }

    /**
     * Get full Trade details.
     */
    @GET
    @Path("getTrade")
    @Produces(MediaType.APPLICATION_JSON)
    fun gettrades(@QueryParam("linearID") linearID: String): Response {
        if (linearID == null) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'linearID' missing or has wrong format.\n").build()
        }
        val idParts = linearID.split('_')
        val uuid = idParts[idParts.size - 1]
        val criteria = QueryCriteria.LinearStateQueryCriteria(linearId = listOf(UniqueIdentifier.fromString(uuid)),status = Vault.StateStatus.ALL)
        return try {
            Response.ok(rpcOps.vaultQueryBy<TradeState>(criteria=criteria).states).build()
        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            Response.status(BAD_REQUEST).entity(ex.message!!).build()
        }
    }
}