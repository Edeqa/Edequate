/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 12/28/17.
 */

function ApiHolder(main) {

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "api";
    this.title = "API";
    this.menu = "API";
    this.icon = "extension";

    this.start = function() {
        console.log("START API");
    };

    this.resume = function() {
        console.log("RESUME API");
    }

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.API:
                console.log("INDEX API");
                u.byId("content").innerHTML = u.lang.api_body.innerHTML;
                u.byId("content").classList.add("content-api");
                if(object) object();
                break;
        }
        return true;
    }

}