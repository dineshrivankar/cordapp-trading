
# The Trading CorDapp

This CorDapp is an example of how trades can be created and settled confidentially. For example, a party can create a trade 
with a counterparty stating that they want to sell 10 USD and buy 100 EUR in return. The counterparty can then create a 
counter-trade accepting the trade.

The CorDapp includes:

* A trade state definition that records a trade between two parties.
* A contract that facilitates the verification of the trade and counter trade.
* Two flows for creating trades and counter trades for trade settlement.

The CorDapp allows you to create trades and counter trades with buying and selling values. A party can select the counter party when creating the trade and specify the sell / buy value with currency details. The CorDapp also 
comes with an API and website that allows you to do all of the aforementioned things.

# Instructions for setting up

1. `git clone https://github.com/dineshrivankar/cordapp-trading.git`
2. `cd cordapp-trading`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries)./r  
4. `cd kotlin-source/build/nodes`
5. `./runnodes`

At this point you will have a notary node running as well as three other nodes and their corresponding webservers. There should be 7 console windows in total. One for the notary and two for each of the three nodes. The nodes take about 20-30 seconds to finish booting up.

# Using the CorDapp via the web front-end, navigate to:

1. PartyA: `http://localhost:10009`
2. PartyB: `http://localhost:10012`
3. PartyC: `http://localhost:10015`

You'll see a basic page, listing all the API end-points and static web content. Click on the "tradingWeb" link under
"static web content". The dashboard shows you a number of things:

1. All trades to date
2. A button to create a new trade


## Create New Trade

1. Click on the "Create Trade" button.
2. Select the counterparty, enter in the sell value, sell currency, buy value, and buy currency
3. Click "Create Trade"
4. Wait for the transaction confirmation
5. After the transaction message pops up, click anywere
6. The UI should update to reflect the new trade
7. Navigate to the counterparty's dashboard. You should see the same trade there

## Counter Trade

1. Navigate to the counterparty's dashboard
2. Select the trade for creating the counter trade
3. Click on the "Counter Trade" button
4. Counter Trade popup will apper where the current trade is reversed as counter trade
5. Click "Counter Trade"
6. Wait for the transaction confirmation
7. After transaction message popup, click anywere
8. The UI should update to reflect the new trade

## Transaction Details

1. Select the trade to view the transaction details
2. Click on the "Transaction Details" button
3. A transaction details pop-up will appear where the trade state is displayed in JSON format
4. If the trade is already approved then we will have two states for the same trade

That's  it!

Feel free to submit a PR.
