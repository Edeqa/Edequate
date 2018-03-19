/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/7/18.
 */

function PagesHolder(main) {

    var self = this;
    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "$pages";
    // this.title = "Pages";
    // this.menu = "Pages";
    // this.icon = "home";

    this.origin = [];
    this.pages = null;
    this.isInstalled = false;
    this.currentType = null;


    this.start = function() {
        console.log("Starting PagesHolder");
        u.getJSON("/rest/data", {resource: "pages-" + main.mainType + ".json"}).then(function(json){
            self.origin = json;
            setUpPages(json);
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    this.resume = function(type) {
        u.progress.show(u.lang.loading);
        if(type) {
            this.currentType = type;
        } else {
            type = this.currentType;
        }

        if(!self.pages) {
            u.getJSON("/rest/data", {resource: "pages-" + main.mainType + ".json", locale: main.selectLang.value}).then(function(json){
                setUpPages(json);
                processPage(type);
            }).catch(function(e,x){
                console.error(e,x);
            });
        } else if(self.pages[type]) {
            processPage(type);
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
                    self.pages = self.pages || {};
                    if(!self.pages[pages.type]) {
                        self.pages[pages.type] = pages;
                        var icon = pages.icon;
                        if(icon && icon.split("/").length > 1) {
                            icon = u.create(HTML.IMG, {
                                src: icon,
                                className: "icon drawer-menu-item-icon"
                            })
                        }
                        main.drawer.add({
                            section: pages.category,
                            id: pages.type,
                            name: u.lang[pages.menu] || pages.menu,
                            icon: icon,
                            priority: pages.priority,
                            callback: function () {
                                main.holder = self;
                                main.drawer.toggleSize(false);
                                main.actionbar.toggleSize(false);

                                main.content.scrollTop = 0;

                                main.drawer.close();
                                window.history.pushState({}, null, "/" + main.mainType + "/" + this.type);
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
            var page = self.pages[type];
            if(!page) {
                main.holder = u.eventBus.holders[404];
                // main.turn(404, type);
                if(main.holder) {
                    if(type) {
                        main.holder.resume(type);
                    } else {
                        main.holder.resume();
                    }
                    // if(!main.holder.preventState) {
                    //     main.history.add(holderType, options);
                        main.actionbar.setTitle(main.holder.title);
                        u.lang.updateNode(main.drawer.headerPrimary, main.holder.title);
                    // }
                } else {
                    window.location = "/";
                }
                return;
            }

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
                    innerHTML: u.lang.error || "Error"
                }, main.content);
                u.progress.hide();
            });
            main.history.add(self.type, [type]);
            main.actionbar.setTitle(u.lang[page.title] || page.title);
            u.lang.updateNode(main.drawer.headerPrimary, u.lang[page.title] || page.title);
        } catch(e) {
            console.error(e);
        }
    }

}