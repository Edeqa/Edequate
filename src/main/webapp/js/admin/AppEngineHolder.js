/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 4/9/18.
 */
function AppEngineHolder(main) {
    var u = main.edequate;

    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "appengine";
    this.title = "AppEngine";
    this.menu = "AppEngine";
    this.icon = "memory";
    var div;
    var info;

    this.start = function() {
        div = main.content;
        u.clear(div);
        info = u.create(HTML.DIV, "This site is working under Google AppEngine that is still not supporting by Edequate Admin. Please prepare your files on the local server and then deploy it without changes.");
        div.appendChild(info);
    };

    this.resume = function(action,id) {
        u.clear(div);
        div.appendChild(info);

    };

    this.onEvent = function(event) {
        switch(event) {
            case "pages_done":
            case "turn":
                u.clear(div);
                div.appendChild(info);
                break;
        }
    };

}
