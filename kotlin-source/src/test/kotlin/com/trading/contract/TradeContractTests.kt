package com.trading.contract

import com.trading.contract.TradeContract.Companion.TRADE_CONTRACT_ID
import com.trading.state.TradeState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class TradeContractTests {
    private val ledgerServices = MockServices()
    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val miniCorp = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))
    val sellValue = 100
    val sellCurrency = "USD"
    val buyValue = 100
    val buyCurrency = "EUR"
    val tradeStatus = "PENDING"

    @Test
    fun `Transaction must include Create command`() {
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                fails()
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                verifies()
            }
        }
    }

   @Test
    fun `Create transaction must have no inputs`() {
        ledgerServices.ledger {
            transaction {
                input(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("No inputs should be consumed when issuing an Trade.")
            }
        }
    }

   @Test
    fun `Transaction must have one output`() {
       ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("Only one output state should be created.")
            }
        }
    }

  @Test
    fun `Participants must be signers`() {
      ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(miniCorp.publicKey, TradeContract.Commands.Create())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `Trade creating party is not the counter party `() {
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, miniCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The creating party and the counter party cannot be the same entity.")
            }
        }
    }

    @Test
    fun `The sell currency and the buy currency cannot be the same entity`() {
        val buySameCurrency = "USD"
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,buySameCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The sell currency and the buy currency cannot be the same entity.")
            }
        }
    }

    @Test
    fun `The Trade's sell value must be non-negative`() {
        val negativeSellValue = -1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(negativeSellValue,sellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The Trade's sell value must be non-negative.")
            }
        }
    }

    @Test
    fun `The Trade's buy value must be non-negative`() {
        val negativeBuyValue = -1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,negativeBuyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The Trade's buy value must be non-negative.")
            }
        }
    }

    @Test
    fun `The Trade's sell currency can't be empty`() {
        val newSellCurrency = ""
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,newSellCurrency,buyValue,buyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The Trade's sell currency can't be empty.")
            }
        }
    }

    @Test
    fun `The Trade's buy currency can't be empty`() {
        val newBuyCurrency = ""
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(sellValue,sellCurrency,buyValue,newBuyCurrency, miniCorp.party, megaCorp.party,tradeStatus))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The Trade's buy currency can't be empty.")
            }
        }
    }

}