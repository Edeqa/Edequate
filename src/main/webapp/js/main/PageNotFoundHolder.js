/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 2/12/18.
 */

function PageNotFoundHolder(main) {

//    this.category = DRAWER.SECTION_LAST;
    this.type = "404";
    this.title = u.lang.page_not_found;
//    this.menu = u.lang.about;
//    this.icon = "info_outline";

    this.start = function() {
        console.log("Starting PageNotFoundHolder");
    };

    this.resume = function() {
        console.log("Resuming PageNotFoundHolder");
        u.progress.show(u.lang.loading);

        this.title = u.lang.page_not_found;

        u.clear(main.content);
        u.post("/rest/content", {resource: "page-not-found.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: u.lang.error}, main.content);
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