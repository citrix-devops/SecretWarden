// Global Configuration Page Javascript Functions
// [1] https://docs.atlassian.com/aui/8.0.2/docs/restful-table.html
// [2] https://bitbucket.org/jwalton/aui-archive/src/master/auiplugin-tests/src/main/resources/restfultable/restfultable-example.js

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

    var CheckboxCreateView = AJS.RestfulTable.CustomCreateView.extend({
        render: function (self) {
            var $select = $("<input type='checkbox' checked class='ajs-restfultable-input-"  + self.name + "' />" +
                "<input type='hidden' name='" + self.name + "'/>");
            return $select;
        }
    });

    var CheckboxEditView = AJS.RestfulTable.CustomEditView.extend({
        render: function (self) {
            var attrChecked = "";
            if (self.value === true)
                attrChecked = "checked";

            var $select = $("<input type='checkbox' " + attrChecked + " class='ajs-restfultable-input-" + self.name + "' />" +
                "<input type='hidden' name='" + self.name + "'/>");

            $select.change(function() {
                if ($select.is(":checked")) {
                    self.value = true;
                    $select.val(true);
                } else {
                    self.value = false;
                    $select.val(false);
                }
            });

            return $select;
        }
    });
    var CheckboxReadView = AJS.RestfulTable.CustomReadView.extend({
        render: function (self) {

            var attrChecked = "";
            if (self.value === true)
                attrChecked = "checked";

            var $select = $("<input type='checkbox' disabled='disabled' " + attrChecked + " class='ajs-restfultable-input-" + self.name + "' />" +
                "<input type='hidden' name='" + self.name + "'/>");
            return $select;
        }
    });

    function buildMatchRulesetTable() {
        console.log("Building the MatchSecretRule RESTful table...");

        new AJS.RestfulTable({
            el: jQuery("#match-rule-config-table"),
            autoFocus: true,
            allowDelete: false, // DELETE Not yet implemented
            resources: {
                all: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rule",
                self: AJS.contextPath() + "/rest/secretwarden/1.0/globalconfig/match-secret-rule"
            },
            columns: [
                {
                    id: "ruleNumber",
                    header: "Rule Number",
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
                    header: "Enabled",
                    readView: CheckboxReadView,
                    editView: CheckboxEditView,
                    createView: CheckboxCreateView
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
