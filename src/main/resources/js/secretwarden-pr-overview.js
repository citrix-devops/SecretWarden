(function($) {
    // Set up our namespace
    window.SecretWarden = window.SecretWarden || {};
    SecretWarden.PullRequestUI = SecretWarden.PullRequestUI || {};

     function reloadContext(context) {

     }

    function showDialog() {
        dialog = AJS.dialog2($(com.cyanoth.secretwarden.overviewDialog()));
        dialog.show();
    }

    function doSomething() {
                  console.log("HERE IN doSOMETHING");
         var count_test = 15;
         var ele = $(".secretwarden-overview-link");
         var elelink = $(".secretwarden-overview-link a");


         if (count_test >=0) {
            ele.removeClass("incomplete");
         }

        if (count_test > 0) {
            ele.addClass("hassecrets");
            $(".secretwarden-overview-link .label").text(count_test + " secrets found");

        } else if (count_test === 0) {
            ele.addClass("nosecrets");


            //$('a').contents().unwrap();


            elelink.replaceWith(function() {
             return $('span', this);
            });

            $(".secretwarden-overview-link .label").text("No secrets found")
        }
    }

   $(document).on('click', '.secretwarden-overview-link', function(e) {
        e.preventDefault();
        doSomething()
    });

// CHECKME: Do we need document.ready() or is the context pass enough???
}(AJS.$));

