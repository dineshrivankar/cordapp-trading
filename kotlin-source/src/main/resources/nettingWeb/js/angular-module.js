"use strict";

// --------
// WARNING:
// --------

// THIS CODE IS ONLY MADE AVAILABLE FOR DEMONSTRATION PURPOSES AND IS NOT SECURE!
// DO NOT USE IN PRODUCTION!

// FOR SECURITY REASONS, USING A JAVASCRIPT WEB APP HOSTED VIA THE CORDA NODE IS
// NOT THE RECOMMENDED WAY TO INTERFACE WITH CORDA NODES! HOWEVER, FOR THIS
// PRE-ALPHA RELEASE IT'S A USEFUL WAY TO EXPERIMENT WITH THE PLATFORM AS IT ALLOWS
// YOU TO QUICKLY BUILD A UI FOR DEMONSTRATION PURPOSES.

// GOING FORWARD WE RECOMMEND IMPLEMENTING A STANDALONE WEB SERVER THAT AUTHORISES
// VIA THE NODE'S RPC INTERFACE. IN THE COMING WEEKS WE'LL WRITE A TUTORIAL ON
// HOW BEST TO DO THIS.

const app = angular.module('nettingAppModule', ['ui.bootstrap','ngLoadingOverlay']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('NeetingAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/netting/";
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'createTradeModal.html',
            controller: 'CreateTradeCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });
        modalInstance.result.then(() => {}, () => {});
    };


    demoApp.openCounterTradeModal = (trade) => {
       demoApp.currentTrade=trade;
       const modalInstance1 = $uibModal.open({
            templateUrl: 'CounterTradeModal.html',
            controller: 'CounterTradeCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });
        modalInstance1.result.then(() => {}, () => {});
    };

    demoApp.openTransactionDetailsModal = (tradeId) => {
       demoApp.currentTradeId=tradeId;
       const modalInstance1 = $uibModal.open({
            templateUrl: 'TransactionDetailsModal.html',
            controller: 'TransactionDetailsTradeCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });
        modalInstance1.result.then(() => {}, () => {});
    };

    demoApp.getTrades = () => $http.get(apiBaseURL + "trades")
        .then((response) => demoApp.trades = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getTrades();

});

app.controller('CreateTradeCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers, $loadingOverlay) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    // Validate and create Trade.
    modalInstance.create = () => {
        $loadingOverlay.show('Transaction Processing ...', 'rgba(0, 0, 0, 0.3)', '#fff');
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;
            $uibModalInstance.close();
            const createTradeEndpoint = `${apiBaseURL}create-trade?counterParty=${modalInstance.form.counterparty}&sellValue=${modalInstance.form.sellValue}&buyValue=${modalInstance.form.buyValue}&sellCurrency=${modalInstance.form.sellCurrency}&buyCurrency=${modalInstance.form.buyCurrency}&tradeStatus=PENDING`;
            // Create Trade and handle success / fail responses.
            $http.put(createTradeEndpoint).then(
                (result) => {
                    $loadingOverlay.hide();
                    modalInstance.displayMessage(result);
                    demoApp.getTrades();
                },
                (result) => {
                    $loadingOverlay.hide();
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Trade modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Trade.
    function invalidFormInput() {
        return isNaN(modalInstance.form.sellValue) || (modalInstance.form.counterparty === undefined);
    }
});


app.controller('CounterTradeCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers, $loadingOverlay) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    modalInstance.counterTradeEndpoint = "";
    modalInstance.trade = demoApp.currentTrade;

    // No validation required as we reverse Trade.
   modalInstance.counterTrade = (status) => {
            $loadingOverlay.show('Transaction Processing ...', 'rgba(0, 0, 0, 0.3)', '#fff');
            modalInstance.formError = false;
            $uibModalInstance.close();
            if(status=='ACCEPTED'){
                // Reverse the trade if counter party has accepted
                modalInstance.counterTradeEndpoint = `${apiBaseURL}counter-trade?tradeId=${modalInstance.trade.linearId.id}&counterParty=${modalInstance.trade.initiatingParty}&sellValue=${modalInstance.trade.buyValue}&buyValue=${modalInstance.trade.sellValue}&sellCurrency=${modalInstance.trade.buyCurrency}&buyCurrency=${modalInstance.trade.sellCurrency}&tradeStatus=ACCEPTED`;
            }else{
                // Retain the old trade values if counter party has rejected
                modalInstance.counterTradeEndpoint = `${apiBaseURL}counter-trade?tradeId=${modalInstance.trade.linearId.id}&counterParty=${modalInstance.trade.initiatingParty}&sellValue=${modalInstance.trade.sellValue}&buyValue=${modalInstance.trade.buyValue}&sellCurrency=${modalInstance.trade.sellCurrency}&buyCurrency=${modalInstance.trade.buyCurrency}&tradeStatus=REJECTED`;
            }

            // Counter Trade and handle success / fail responses.
            $http.put(modalInstance.counterTradeEndpoint).then(
                (result) => {
                    $loadingOverlay.hide();
                    modalInstance.displayMessage(result);
                    demoApp.getTrades();
                  },
                (result) => {
                    $loadingOverlay.hide();
                    modalInstance.displayMessage(result);
                }
            );
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Trade modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

});

app.controller('TransactionDetailsTradeCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;
    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;
    $http.get(apiBaseURL + "getTrade?linearID="+demoApp.currentTradeId).then(
        (response) => modalInstance.transactionDetails = response.data
        );
    // Close create Trade modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});