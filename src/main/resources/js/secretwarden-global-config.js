// Global Configuration Page Javascript Functions
// [1] https://docs.atlassian.com/aui/8.0.2/docs/restful-table.html

define('SecretWarden/GlobalConfig', [
        'jquery',
        'bitbucket/util/navbuilder',
        'bitbucket/util/server',
        'exports'
    ], function($,
        navbuilder,
        server,
        exports) {
        'use strict';


    exports.onReady = function () {
        $(document).on('click', '#secretwarden-clearcache-button', function (e) {
            invokeClearCache();
        });

        $(document).on('click', '#secretwarden-reloadruleset-button', function (e) {
            invokeReloadRuleSet();
        });

        buildMatchRulesetTable();
    };

    function invokeClearCache() {
        var selectButton = $("#secretwarden-clearcache-button");
        selectButton.attr("aria-disabled", "true");

        server.rest({
            url: navbuilder.rest("secretwarden").addPathComponents("globalconfig", "clear-result-cache").build(),
            type: 'PUT',
            async: false,
            complete: function(jqXHR) {
                if (jqXHR.status === 200) {
                    AJS.flag({
                        type: 'success',
                        title: 'Success!',
                        persistent: false,
                        body: 'SecretWarden result cache has been cleared successfully!'
                    });
                } else {
                    AJS.flag({
                        type: 'error',
                        title: 'Failed!',
                        persistent: false,
                        body: 'An error occurred clearing the SecretWarden cache. Please check console / server-logs for more information'
                    });
                    console.log("SecretWarden clear result cache failed! Status: " + jqXHR.status);
                }
                selectButton.attr("aria-disabled", "false");
            }
        });

    }

    function buildMatchRulesetTable() {
        console.log("Building the MatchSecretRule RESTful table...");

        new AJS.RestfulTable({
            el: jQuery("#match-rule-config-table"),
            autoFocus: true,
            allowDelete: false, // DELETE Not yet implemented
            resources: {
                all: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rules",
                self: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rule/{ruleNumber}"
            },
            columns: [
                {
                    id: "ruleNumber",
                    header: "Rule Identifier",
                    allowEdit: false
                },
                {
                    id: "friendlyName",
                    header: "Rule Name"
                },
                {
                    id: "regexPattern",
                    header: "Regular Expression Pattern"
                },
                {
                    id: "enabled",
                    header: "Enabled"
                }
            ]
        });
    }

    function invokeReloadRuleSet() {
        var selectButton = $("#secretwarden-reloadruleset-button");

        server.rest({
            url: navbuilder.rest("secretwarden").addPathComponents("globalconfig", "reload-ruleset").build(),
            type: 'PUT',
            async: false,
            complete: function(jqXHR) {
                if (jqXHR.status === 200) {
                    AJS.flag({
                        type: 'success',
                        title: 'Success!',
                        persistent: false,
                        body: 'SecretWarden ruleset has reloaded successfully!'
                    });
                } else {
                    AJS.flag({
                        type: 'error',
                        title: 'Failed!',
                        persistent: false,
                        body: 'SecretWarden ruleset reload has failed! The old ruleset still applies. Please check console / server-logs for more information'
                    });
                    console.log("SecretWarden ruleset reload has failed! The old ruleset still applies. Status: " + jqXHR.status);
                }
                selectButton.attr("aria-disabled", "false");
            }
        });

    }

});

jQuery(document).ready(function () {
    require('SecretWarden/GlobalConfig').onReady();
});
