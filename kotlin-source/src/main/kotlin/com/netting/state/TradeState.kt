package com.netting.state

import com.netting.schema.TradeSchemaV1
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

/**
 * The state object recording Trade agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param sellValue the value of the Trade.
 * @param sellCurrency the currency of the Trade.
 * @param buyValue the value of the Trade.
 * @param buyCurrency the currency of the Trade.
 * @param initiatingParty the party initiating the Trade.
 * @param counterParty the party receiving and responding the Trade.
 */
data class TradeState(val sellValue: Int,
                      val sellCurrency: String,
                      val buyValue: Int,
                      val buyCurrency: String,
                      val initiatingParty: Party,
                      val counterParty: Party,
                      val tradeStatus: String,
                      override val linearId: UniqueIdentifier = UniqueIdentifier()):

        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(initiatingParty, counterParty)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is TradeSchemaV1 -> TradeSchemaV1.PersistentTrade(
                    this.initiatingParty.name.toString(),
                    this.counterParty.name.toString(),
                    this.sellValue,
                    this.sellCurrency.toString(),
                    this.buyValue,
                    this.buyCurrency.toString(),
                    this.tradeStatus,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(TradeSchemaV1)
}
