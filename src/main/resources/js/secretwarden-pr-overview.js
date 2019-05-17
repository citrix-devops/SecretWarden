(function($) {
    // Set up our namespace
    window.SecretWarden = window.SecretWarden || {};
    SecretWarden.PROverviewUI = SecretWarden.PROverviewUI || {};


    console.log("start2");
    $('.com.cyanoth.secretwarden.proverviewui.link').replaceWith(com.cyanoth.secretwarden.panel());
    console.log("start3");

    SecretWarden.PROverviewUI._pullRequestIsOpen = function(context) {
        var pr = context['pullRequest'];
        return pr.state === 'OPEN';
    };

    function showDialog() {
        var dialog = showDialog._dialog;
        if (!dialog) {
            dialog = showDialog._dialog = new AJS.dialog2()
                .addHeader("Secret Warden - Identified Secrets")
                .addPanel("Scan Results")
                .addCancel("Close", function() {
                    dialog.hide();
                });
        }

        dialog.getCurrentPanel().body.html("<p> Hello World! </p>");
        dialog.show().updateHeight();
    }

    $(document).on('click', 'com.cyanoth.secretwarden.proverviewui.link', function(e) {
        e.preventDefault();
        showDialog();
    });




}(AJS.$));

