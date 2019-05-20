
console.log("HEEEERREEE3");


define('SecretWarden/GlobalConfig', [
        'jquery',
        'bitbucket/util/navbuilder',
        'bitbucket/util/server',
        'bitbucket/util/state',
        'aui/flag',
        'exports'
    ], function($,
        navbuilder,
        server,
        state,
        flag,
        exports) {
        'use strict';

        console.log("HEEEERREEE4");



    function invokeClearCache() {
        //POST: /secretwarden/globalconfig/clearcache
    }


    function confirmWithUser(caption) {

    }

    function buildTable() {
        new AJS.RestfulTable({
            el: jQuery("#project-config-versions-table"),
            autoFocus: true,
            allowDelete: false, // DELETE Not yet implemented
            resources: {
                all: "rest/project/HSP/versions?expand=operations",
                self: "rest/version"
            },
            columns: [
                {
                    id: "identifier",
                    header: "Rule Identifier",
                    allowEdit: false
                },
                {
                    id: "name",
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

    exports.loadPage = function (context) {
        console.log("HEEEERREEE2");

        buildTable();

            return {};
    };



});

function hello() {
    console.log("hello")
}

console.log("HEEEERREEE")

alert("IM HERE");