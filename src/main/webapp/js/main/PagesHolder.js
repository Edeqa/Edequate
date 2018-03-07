/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/7/18.
 */

function PagesHolder(main) {

    var self = this;
    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "pages";
    // this.title = "Pages";
    // this.menu = "Pages";
    // this.icon = "home";

    this.pages = [];
    this.structure = {};
    this.isInstalled = false;


    this.start = function() {
        console.log("Starting PagesHolder");
        u.getJSON("/rest/data", {resource: "pages.json"}).then(function(json){
            self.pages = json;
            setUpPages(json);
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    this.resume = function(type) {
        console.log("Resuming PagesHolder");
        u.progress.show(u.lang.loading);

        if(self.structure[type]) {
            processPage(type);
        } else {
            u.getJSON("/rest/data", {resource: "pages.json"}).then(function(json){
                setUpPages(json, true);
                processPage(type);
            }).catch(function(e,x){
                console.error(e,x);
            });
        }
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case "loaded":
                setUpPages(self.pages);
                break;
        }
    };

    function setUpPages(pages, onlyBuildStructure) {
        try {
            if (!pages) return;
            if (self.isInstalled) return;
            if (pages.constructor === Object) {
                if (pages.menu) {
                    console.log("page", pages);
                    self.structure[pages.type] = pages;
                    if (!onlyBuildStructure) {
                        main.drawer.add({
                            section: pages.category,
                            id: pages.type,
                            name: pages.menu,
                            icon: pages.icon,
                            priority: pages.priority,
                            callback: function () {
                                main.drawer.toggleSize(false);
                                main.actionbar.toggleSize(false);

                                main.content.scrollTop = 0;

                                main.drawer.close();
                                window.history.pushState({}, null, "/main/" + this.type);
                                self.resume(this.type);
                                return false;
                            }.bind(pages)
                        });
                    }
                }
            } else if (pages.constructor === Array) {
                for (var i in pages) {
                    setUpPages(pages[i]);
                }
            }
        } catch(e) {
            console.error(e);
        }
    }

    function processPage(type) {
        try {
            var page = self.structure[type];

            u.clear(main.content);
            u.post("/rest/content", {
                resource: page.resource,
                locale: main.selectLang.value
            }).then(function (xhr) {
                u.create(HTML.DIV, {
                    className: "content-normal",
                    innerHTML: xhr.response
                }, main.content);
                u.progress.hide();
            }).catch(function (error, json) {
                console.error(json);
                u.create(HTML.DIV, {
                    className: "content-centered",
                    innerHTML: u.lang.error
                }, main.content);
                u.progress.hide();
            });
            main.history.add(self.type, [type]);
            main.actionbar.setTitle(page.title);
            u.lang.updateNode(main.drawer.headerPrimary, page.title);
        } catch(e) {
            console.error(e);
        }
    }

}