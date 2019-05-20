
// [1] https://docs.atlassian.com/bitbucket-server/docs/6.3.0/reference/javascript/

define('SecretWarden/PullRequestUIOverview', [
        'jquery',
        'bitbucket/util/navbuilder',
        'bitbucket/util/server',
        'bitbucket/util/state',
        'aui/flag',
        'exports'
    ], function(
        $,
        navbuilder,
        server,
        state,
        flag,
        exports) {
        'use strict';

        var CHECK_INTERVAL = 500;

        var checkScanStatus;
        function getScanStatusTimer() {
            server.rest({
                url: navbuilder.rest("secretwarden").addPathComponents("prscan", "result", state.getProject().key,
                     state.getRepository().slug, state.getPullRequest().id).build(),
                type: 'GET',
                async: false,
                success: function (results, textStatus, jqXHR) {
                    console.log("SecretWarden scan result retrieved successfully.");
                    updateOverviewLinkWithResults(results);

                },
                error: function (jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status === 404) {
                        console.log("SecretWarden scan has not yet started. Will retry in " + CHECK_INTERVAL + "ms")
                    } else {
                        console.log("SecretWarden getScanStatus failed! Status: " + jqXHR.status + " Error:" + errorThrown.toString());
                        updateOverviewLinkAsFailed();
                    }
                }
            });

        }

        function updateOverviewLinkWithResults(scanResult) {
            clearInterval(checkScanStatus);
            var linkEle = $(".secretwarden-overview-link");
            var labEle = $(".secretwarden-overview-link .label");

            linkEle.removeClass("incomplete");

            var foundSecrets = scanResult["foundSecrets"]["foundSecrets"];
            var secretCount = foundSecrets.length;

            if (secretCount > 0) {
                linkEle.addClass("hassecrets");
                labEle.text(secretCount + " secrets found.");
                setOnClickHandler(foundSecrets)

            } else if (secretCount === 0) {
                linkEle.addClass("nosecrets");
                removeLinkElement()
                labEle.text("No secrets were found");
            } else {
                updateOverviewLinkAsFailed();
            }
        }



        function updateOverviewLinkAsFailed() {
            clearInterval(checkScanStatus);
            var linkEle = $(".secretwarden-overview-link");
            var labEle = $(".secretwarden-overview-link .label");

            linkEle.addClass("failed");
            labEle.text("SecretWarden secret scan failed");
            removeLinkElement()

        }

        function removeLinkElement() {
            var linkEle = $(".secretwarden-overview-link");
            linkEle.$("a").replaceWith(function() {
                return $('span', this);
            });

        }

        function setOnClickHandler(foundSecrets) {
            $(document).on('click', '.secretwarden-overview-link', function (e) {
                e.preventDefault();
                console.log(foundSecrets);
                var dialog = AJS.dialog2($(com.cyanoth.secretwarden.overviewDialog({foundSecrets: foundSecrets})));
                dialog.show();
                console.log("I got clicked...");
            });
        }

        function start() {
            checkScanStatus = setInterval(getScanStatusTimer, 500);
        }



        exports.showSecretScanPROverview = function (context) {
            start();
        return {};
    };

});