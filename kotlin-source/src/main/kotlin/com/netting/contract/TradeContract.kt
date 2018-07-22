package com.netting.contract

import com.netting.state.TradeState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

/**
 * This contract enforces rules regarding the creation of a valid [TradeState].
 *
 * For a new [Trade] to be created onto the ledger, a transaction is required which takes:
 * - Zero input states.
 * - One output state: the new [Trade].
 * - An Create() command with the public keys of both the party.
 *
 *  For a counter [Trade] to be created onto the ledger, a transaction is required which takes:
 * - One input states: the old [Trade].
 * - One output state: the new [Trade].
 * - An CounterTrade() command with the public keys of both the lender and the borrower.
 *
 * All contracts must sub-class the [Contract] interface.
 */
class TradeContract : Contract {
    companion object {
        @JvmStatic
        val TRADE_CONTRACT_ID = "com.netting.contract.TradeContract"
    }

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is TradeContract.Commands.Create -> {
                requireThat {
                    // Generic constraints around the Trade transaction.
                    "No inputs should be consumed when issuing an Trade." using (tx.inputs.isEmpty())
                    "Only one output state should be created." using (tx.outputs.size == 1)
                    val out = tx.outputsOfType<TradeState>().single()
                    "The creating party and the counter party cannot be the same entity." using (out.initiatingParty != out.counterParty)
                    "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                     // Trade-specific constraints.
                    "The sell currency and the buy currency cannot be the same entity." using (out.sellCurrency != out.buyCurrency)
                    "The Trade's sell value must be non-negative." using (out.sellValue > 0)
                    "The Trade's buy value must be non-negative." using (out.buyValue > 0)
                    "The Trade's sell currency can't be empty." using (out.sellCurrency.isNotEmpty())
                    "The Trade's buy currency can't be empty." using (out.buyCurrency.isNotEmpty())
                    "The Trade's buy value must be non-negative." using (out.buyValue > 0)
                }
            }

            is TradeContract.Commands.CounterTrade -> {
                requireThat {
                    // Generic constraints around the Trade transaction.
                    "Only one output state should be created." using (tx.outputs.size == 1)
                    val out = tx.outputsOfType<TradeState>().single()
                    "The creating party and the counter party cannot be the same entity." using (out.initiatingParty != out.counterParty)
                    "All of the participants must be signers." using (command.signers.containsAll(out.participants.map { it.owningKey }))

                    // Trade-specific constraints.
                    "The sell currency and the buy currency cannot be the same entity." using (out.sellCurrency != out.buyCurrency)
                    "The Trade's sell value must be non-negative." using (out.sellValue > 0)
                    "The Trade's buy value must be non-negative." using (out.buyValue > 0)
                    "The Trade's sell currency can't be empty." using (out.sellCurrency.isNotEmpty())
                    "The Trade's buy currency can't be empty." using (out.buyCurrency.isNotEmpty())
                    "The Trade's buy value must be non-negative." using (out.buyValue > 0)
                }
            }
        }
    }

    /**
     * This contract implements two commanda.
     */
    interface Commands : CommandData {
        class Create : Commands
        class CounterTrade : Commands
    }
}
