/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 12/28/17.
 */

function ProductsHolder(main) {

    this.category = DRAWER.SECTION_EXPLORE;
    this.type = "products";
    this.title = "Products";
    this.menu = "Products";
    this.icon = "extension";

    this.start = function() {
        console.log("START roducts");
    };

    this.resume = function() {
        console.log("RESUME roducts", main);
    }

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.API:
                console.log("INDEX roducts");
                u.byId("content").innerHTML = u.lang.api_body.innerHTML;
                u.byId("content").classList.add("content-api");
                if(object) object();
                break;
        }
        return true;
    }

}