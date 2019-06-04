// Javascript functions to display the dialog / secret scan results on the pull request user interface
// [1] https://docs.atlassian.com/bitbucket-server/docs/6.3.0/reference/javascript/

define('SecretWarden/PullRequestUIOverview', [
        'jquery',
        'bitbucket/util/navbuilder',
        'bitbucket/util/server',
        'bitbucket/util/state',
        'exports'
    ], function(
        $,
        navbuilder,
        server,
        state,
        exports) {
        'use strict';

        var onLinkClickHasBeenSet = false;

        // On an open pull-request, poll the REST Endpoint every 500ms to check the secret scan status.
        // This can be not found, in progress, failed, completed. Each case should be handled appropriately.
        var CHECK_INTERVAL = 500;
        var checkScanStatus;
        function getScanStatusPoller() {
            server.rest({
                url: navbuilder.rest("secretwarden").addPathComponents("prscan", "result", state.getProject().key,
                     state.getRepository().slug, state.getPullRequest().id).build(),
                type: 'GET',
                async: false,
                success: function (results, textStatus, jqXHR) {
                    console.log("SecretWarden scan result has been retrieved successfully! Updating UI elements");
                    updateOverviewLinkWithResults(results);

                },
                error: function (jqXHR, textStatus, errorThrown) {
                    if (jqXHR.status === 404) {
                        console.log("SecretWarden scan has still not started. Will recheck in " + CHECK_INTERVAL + "ms")
                    } else {
                        console.log("SecretWarden getScanStatus failed! Status: " + jqXHR.status + " Error:" + errorThrown.toString());
                        updateOverviewLinkAsFailed();
                    }
                },
                statusCode: {
                    500: false, // Stop bitbucket default error handling on failure
                    404: false // Stop bitbucket default error handling on missing results
                }
            });

        }

        // The scan results have come through, update UI element with secret count
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
                labEle.text("No secrets were found");
                setOnClickHandler(foundSecrets)
            } else {
                updateOverviewLinkAsFailed();
            }
        }

        // The secret scan has been reported as failed.
        function updateOverviewLinkAsFailed() {
            clearInterval(checkScanStatus);
            var linkEle = $(".secretwarden-overview-link");
            var labEle = $(".secretwarden-overview-link .label");

            linkEle.addClass("failed");
            labEle.text("SecretWarden secret scan failed");
        }

        // When secret have been found, handle on click so that they are displayed in a dialog
        function setOnClickHandler(foundSecrets) {

            if (onLinkClickHasBeenSet)
                return;

            $(document).on('click', '.secretwarden-overview-link .label', function (e) {
                e.preventDefault();

                if (foundSecrets.length > 0)
                    var dialog = AJS.dialog2($(com.cyanoth.secretwarden.overviewDialog({foundSecrets: foundSecrets})));
                else
                    var dialog = AJS.dialog2($(com.cyanoth.secretwarden.noSecretsDialog()));

                dialog.show();
            });

            onLinkClickHasBeenSet = true;
        }

        // Start a timer to poll the secret scan result
        function start() {
            getScanStatusPoller();
            checkScanStatus = setInterval(getScanStatusPoller, 500);
        }

        exports.isPullRequestOpen = function(context) {
            var pr = (context['pullRequest']).toJSON();
            console.log(pr);
            return pr.state === 'OPEN';
        };

        exports.showSecretScanPROverview = function (context) {
            start();
            return {};
        };

});