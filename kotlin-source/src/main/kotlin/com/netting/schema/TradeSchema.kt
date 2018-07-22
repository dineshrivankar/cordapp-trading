package com.netting.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for TradeState.
 */
object TradeSchema

/**
 * An TradeState schema.
 */
object TradeSchemaV1 : MappedSchema(
        schemaFamily = TradeSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentTrade::class.java)) {
    @Entity
    @Table(name = "trade_states")
    class PersistentTrade(
            @Column(name = "initiatingParty")
            var initiatingPartyName: String,

            @Column(name = "counterParty")
            var counterParty: String,

            @Column(name = "sellValue")
            var sellValue: Int,

            @Column(name = "sellCurrency")
            var sellCurrency: String,

            @Column(name = "buyValue")
            var buyValue: Int,

            @Column(name = "buyCurrency")
            var buyCurrency: String,

            @Column(name = "tradeStatus")
            var tradeStatus: String,

            @Column(name = "linear_id")
            var linearId: UUID

    ) : PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this("", "", 0,"",0,"","", UUID.randomUUID())
    }
}