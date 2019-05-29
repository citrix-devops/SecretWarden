define('SecretWarden/GlobalConfig', [
        'jquery',
        'bitbucket/util/navbuilder',
        'bitbucket/util/server',
        'bitbucket/util/state',
        'exports'
    ], function($,
        navbuilder,
        server,
        state,
        exports) {
        'use strict';


    exports.onReady = function () {
        buildTable();

        $(document).on('click', '#secretwarden-clearcache-button', function (e) {
            invokeClearCache();
        });

    };

    function invokeClearCache() {
        var selectButton = $("#secretwarden-clearcache-button");
        selectButton.attr("aria-disabled", "true");

        server.rest({
            url: navbuilder.rest("secretwarden").addPathComponents("globalconfig", "clear-result-cache").build(),
            type: 'PUT',
            async: false,
            complete: function(jqXHR, status) {
                if (status === 200) {
                    AJS.flag({
                        type: 'success',
                        title: 'Success!',
                        persistent: false,
                        body: 'SecretWarden result cache has been cleared!'
                    });
                } else {
                    AJS.flag({
                        type: 'error',
                        title: 'Failed!',
                        persistent: false,
                        body: 'An error occurred clearing the SecretWarden cache. Please check console for more information'
                    });
                    console.log("SecretWarden clear result cache failed! Status: " + jqXHR.status + " Error:" + errorThrown.toString());
                }
                selectButton.attr("aria-disabled", "false");
            }
        });

    }

    function buildTable() {

        console.log("Going to build the table...")

        new AJS.RestfulTable({
            el: jQuery("#match-rule-config-table"),
            autoFocus: true,
            allowDelete: false, // DELETE Not yet implemented
            resources: {
                all: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rules",
                self: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rule/{identifier}"
            },
            columns: [
                {
                    id: "identifier",
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
        //POST: secretwarden/globalconfig/reloadruleset

    }

});

jQuery(document).ready(function () {
    require('SecretWarden/GlobalConfig').onReady();
});
