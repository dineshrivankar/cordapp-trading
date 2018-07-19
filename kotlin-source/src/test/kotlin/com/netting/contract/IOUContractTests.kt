package com.netting.contract

import com.netting.contract.TradeContract.Companion.TRADE_CONTRACT_ID
import com.netting.state.TradeState
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class IOUContractTests {
    private val ledgerServices = MockServices()
    private val megaCorp = TestIdentity(CordaX500Name("MegaCorp", "London", "GB"))
    private val miniCorp = TestIdentity(CordaX500Name("MiniCorp", "New York", "US"))
/*
    @Test
    fun `transaction must include Create command`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                fails()
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun `transaction must have no inputs`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                input(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("No inputs should be consumed when issuing an IOU.")
            }
        }
    }

    @Test
    fun `transaction must have one output`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("Only one output state should be created.")
            }
        }
    }

    @Test
    fun `lender must sign transaction`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                command(miniCorp.publicKey, TradeContract.Commands.Create())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `borrower must sign transaction`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                command(megaCorp.publicKey, TradeContract.Commands.Create())
                `fails with`("All of the participants must be signers.")
            }
        }
    }

    @Test
    fun `lender is not borrower`() {
        val iou = 1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, megaCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The lender and the borrower cannot be the same entity.")
            }
        }
    }

    @Test
    fun `cannot create negative-value IOUs`() {
        val iou = -1
        ledgerServices.ledger {
            transaction {
                output(TRADE_CONTRACT_ID, TradeState(iou, miniCorp.party, megaCorp.party))
                command(listOf(megaCorp.publicKey, miniCorp.publicKey), TradeContract.Commands.Create())
                `fails with`("The IOU's value must be non-negative.")
            }
        }
    }*/
}