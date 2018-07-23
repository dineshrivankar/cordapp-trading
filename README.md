
# The Netting and Settlement  CorDapp

This CorDapp is an example of how Trades can be created and settled confidentially. The CorDapp includes:

* An trade state definition that records an trade from one party to another.
* A contract that facilitates the verification of trade and counter trade.
* Two flows for creating trade and counter trade for trade settlement.

The CorDapp allows you to create trade and counter trade with buying and selling value. An party can select the counter party whoile creating the trade and specify the sell / buy value with currency details.It also 
comes with an API and website that allows you to do all of the aforementioned things.

# Instructions for setting up

1. `git clone https://github.com/dineshrivankar/cordapp-netting-and-settlement.git`
2. `cd cordapp-netting-and-settlement`
3. `./gradlew deployNodes` - building may take upto a minute (it's much quicker if you already have the Corda binaries)./r  
4. `cd kotlin-source/build/nodes`
5. `./runnodes`

At this point you will have notary/network map node running as well as three other nodes and their corresponding webservers. There should be 7 console windows in total. One for the networkmap/notary and two for each of the three nodes. The nodes take about 20-30 seconds to finish booting up.


# Using the CorDapp via the web front-end, navigate to:

1. PartyA: `http://localhost:10009`
2. PartyB: `http://localhost:10012`
3. PartyC: `http://localhost:10015`

You'll see a basic page, listing all the API end-points and static web content. Click on the "nettingWeb" link under 
"static web content". The dashboard shows you a number of things:

1. All trads to date
2. A button to create new trade


## Create New Trade

1. Click on the "Create Trade" button.
2. Select the counterparty, enter in the sell value, sell currency, buy value, buy currency
3. Click "Create Trade"
4. Wait for the transaction confirmation
5. After transaction message popup, click anywere
6. The UI should update to reflect the new trade
7. Navigate to the counterparties dashboard. You should see the same trade there

## Counter Trade

1. Navigate to the counterparties dashboard
2. Select the trade for creating the counter trade
3. Click on the "Counter Trade" button
4. Counter Trade popup will apper where the current trade is reversed as counter trade
5. Click "Counter Trade"
6. Wait for the transaction confirmation
7. After transaction message popup, click anywere
8. The UI should update to reflect the new trade


## Transaction Details

2. Select the trade for viewing the transaction details
3. Click on the "Transaction Details" button
4. Transaction details popup will apper where the trade state is displayed in JSON format
5. If the trade is already approved then we will have two states for the same trade


That's  it!

Feel free to submit a PR.
