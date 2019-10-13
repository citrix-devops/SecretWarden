# SecretWarden for Bitbucket Server


The plugin includes a Merge Check option which can prevent secrets from being merged into a repository except by a Repository Administrator. 
Details about the found secrets will be displayed on the pull-request overview tab.

Secrets are found using Regular Expressions. The plugin includes a configuration page for Global Administrators whom can edit the default rulesetor even add custom rules.

A custom Bitbucket Server plugin, (Data Center compatible) to scan source-code for common passwords & secrets.


## Getting Started

To build this plugin, clone & change into the root directory, then run `atlas compile`
You will find the plugin in `./target/secretwarden-x-x-x.jar`.

On Bitbucket, go to Manage Applications in the admin area and upload the jar file.

On a Bitbucket project or repository, go to Settings -> Merge Checks -> Enable or Disable 'Prevent Merging Secrets'

### Configuring

The plugin includes serveral built-in match secret rules, you can override the values or disable them completely. You can even add your own rules.

As an administrator, go to the admin area and select `Secret Warden` under the heading 'Add-ons'.

From here you can:
* Clear the result cache. A troubleshooting tool, to clear the cache of secret scan results from the entire cluster.
* Change Match Secret Rules by editing the table. There is further information on the page on how to enter or change a rule.
* Reload Ruleset . A troubleshooting tool, to reload the cached ruleset across the entire cluster.


## Troubleshooting

You can enable debug logging on this plugin by going the following URL:
```curl -u <user>:<password> -v -X PUT -H "Content-Type: application/json" http://<bitbucket_url>:7990/rest/api/latest/logs/logger/com.cyanoth.secretwarden/debug```

This change will apply to every-node in the cluster automatically, the log files will sho debug messages.

## Known Issues
* There is no delete for custom/default rules - but you can always disable them