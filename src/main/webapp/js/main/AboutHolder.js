/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 12/28/17.
 */

function AboutHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "about";
    this.title = "About";
    this.menu = "About";
    this.icon = "info_outline";

    this.start = function() {
        console.log("Starting HomeHolder");
    };

    this.resume = function() {
        console.log("Resuming HomeHolder");
        u.progress.show(u.lang.loading);
        u.clear(main.content);
        u.post("/rest/v1/content", {resource: "main-about.html", locale: main.selectLang.value}).then(function(xhr){
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