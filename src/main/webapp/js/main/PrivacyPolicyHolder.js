/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 8/26/17.
 */

function PrivacyPolicyHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "privacy";
    this.title = u.lang.privacy_policy;
    this.menu = u.lang.privacy_policy;
    this.icon = "help";

    this.start = function() {
        console.log("Starting PrivacyPolicyHolder");
    }

    this.resume = function() {
        console.log("Resuming PrivacyPolicyHolder");
        u.progress.show(u.lang.loading);
        this.title = u.lang.privacy_policy;
        this.menu = u.lang.privacy_policy;
        u.clear(main.content);
        u.post("/rest/content", {resource: "privacy-policy.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: u.lang.error}, main.content);
            u.progress.hide();
        });
    }
}