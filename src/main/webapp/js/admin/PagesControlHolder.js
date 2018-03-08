/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/8/17.
 */
function PagesControlHolder(main) {
    var div;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "pages";
    this.title = "Pages";
    this.menu = "Pages";
    this.icon = "mode_edit";

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        u.create(HTML.H2, "Pages edit", div);

        u.getJSON("/rest/data/types").then(function(json){
            console.log(json.message);
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

}
