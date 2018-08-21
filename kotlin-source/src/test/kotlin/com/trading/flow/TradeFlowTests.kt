package com.trading.flow

import com.trading.state.TradeState
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TradeFlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    val sellValue = 100
    val sellCurrency = "USD"
    val buyValue = 100
    val buyCurrency = "EUR"
    val tradeStatus = "PENDING"
    @Before
    fun setup() {
        network = MockNetwork(listOf("com.trading.contract"))
        a = network.createPartyNode()
        b = network.createPartyNode()
        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b).forEach { it.registerInitiatedFlow(TradeFlow.Acceptor::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Flow rejects invalid Trade`() {
        val flow = TradeFlow.Initiator(-1, "", -1, "", "", b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the Initiator`() {
        val flow = TradeFlow.Initiator(100, "USD", 100, "EUR", "PENDING", b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(b.info.singleIdentity().owningKey)
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the Acceptor`() {
        val flow = TradeFlow.Initiator(100, "USD", 100, "EUR", "PENDING", b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Flow records a transaction in both parties' transaction storages`() {
        val flow = TradeFlow.Initiator(100, "USD", 100, "EUR", "PENDING", b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()
        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `Recorded transaction has no inputs and a single output, the input Trade`() {
        val flow = TradeFlow.Initiator(sellValue,sellCurrency,buyValue,buyCurrency,tradeStatus,b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both vaults.
        for (node in listOf(a, b)) {
            val recordedTx = node.services.validatedTransactions.getTransaction(signedTx.id)
            val txOutputs = recordedTx!!.tx.outputs
            assert(txOutputs.size == 1)
            val recordedState = txOutputs[0].data as TradeState
            assertEquals(recordedState.sellValue, sellValue)
            assertEquals(recordedState.sellCurrency, sellCurrency)
            assertEquals(recordedState.buyValue, buyValue)
            assertEquals(recordedState.buyCurrency, buyCurrency)
            assertEquals(recordedState.tradeStatus, tradeStatus)
            assertEquals(recordedState.counterParty, b.info.singleIdentity())
        }
    }

    @Test
    fun `Flow records the correct Trade in both parties' vaults`() {
        val flow = TradeFlow.Initiator(sellValue,sellCurrency,buyValue,buyCurrency,tradeStatus,b.info.singleIdentity())
        val future = a.startFlow(flow)
        network.runNetwork()
        future.getOrThrow()

        // We check the recorded Trade in both vaults.
        for (node in listOf(a, b)) {
            node.transaction {
                val trade = node.services.vaultService.queryBy<TradeState>().states
                assertEquals(1, trade.size)
                val recordedState = trade.single().state.data
                assertEquals(recordedState.sellValue, sellValue)
                assertEquals(recordedState.sellCurrency, sellCurrency)
                assertEquals(recordedState.buyValue, buyValue)
                assertEquals(recordedState.buyCurrency, buyCurrency)
                assertEquals(recordedState.tradeStatus, tradeStatus)
                assertEquals(recordedState.counterParty, b.info.singleIdentity())
            }
        }
    }
}