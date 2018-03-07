/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 12/28/17.
 */

function AboutHolder(main) {

    this.category = DRAWER.SECTION_LAST;
    this.type = "about";
    this.title = u.lang.about;
    this.menu = u.lang.about;
    this.icon = "info_outline";
    this.priority = -10;

    this.start = function() {
        console.log("Starting AboutHolder");
    };

    this.resume = function() {
        console.log("Resuming AboutHolder");
        u.progress.show(u.lang.loading);

        this.title = u.lang.about;
        this.menu = u.lang.about;

        u.clear(main.content);
        u.post("/rest/content", {resource: "main-about.html", locale: main.selectLang.value}).then(function(xhr){
            u.create(HTML.DIV, {className: "content-normal", innerHTML: xhr.response}, main.content);
            u.progress.hide();
        }).catch(function(error, json) {
            console.error(json);
            u.create(HTML.DIV, {className: "content-centered", innerHTML: u.lang.error}, main.content);
            u.progress.hide();
        });
    }
}