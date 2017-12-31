/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 8/26/17.
 */

function PrivacyPolicyHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "privacy";
    this.title = u.lang.title_privacy_policy;
    this.menu = u.lang.title_privacy_policy;
    this.icon = "help";

    this.start = function() {
        console.log("Starting PrivacyPolicyHolder");
    };

    this.resume = function() {
        console.log("Resuming PrivacyPolicyHolder");
        u.progress.show(u.lang.loading);
        u.clear(main.content);
        u.post("/rest/v1/content", {resource: "privacy-policy.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: "Error"}, main.content);
            u.progress.hide();
        });
    }

    this.onEvent = function(event, object) {
        console.log("onEvent", event, object);
//        switch(event) {
//            case EVENTS.API:
//                console.log("INDEX HOME");
//                u.byId("content").innerHTML = u.lang.api_body.innerHTML;
//                u.byId("content").classList.add("content-api");
//                if(object) object();
//                break;
//        }
        return true;
    }

}