/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/7/18.

 * !!! IMPORTANT !!!
 * DO NOT EDIT THIS FILE
 * !!! IMPORTANT !!!
 */

function PagesHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_PRIMARY;
    this.type = "$pages";
    this.origin = [];
    this.pages = null;
    this.isInstalled = false;
    this.currentType = null;
    this.preventHistory = true;
    this.preventState = true;


    this.start = function () {
        console.log("Starting PagesHolder");
        u.getJSON("/rest/data", {resource: "pages-" + main.mainType + ".json"}).then(function (json) {
            self.origin = json;
            setUpPages(json);
        }).catch(function (e, x) {
            console.error(e, x);
        });
    };

    this.resume = function (type) {
        u.progress.show(u.lang.loading);
        if (type) {
            this.currentType = type;
        } else {
            type = this.currentType;
        }

        if (!self.pages) {
            u.getJSON("/rest/data", {
                resource: "pages-" + main.mainType + ".json",
                locale: main.selectLang.value
            }).then(function (json) {
                setUpPages(json);
                processPage(type);
            }).catch(function (e, x) {
                console.error(e, x);
            });
        } else /*if(self.pages[type])*/ {
            processPage(type);
        }
    };

    this.onEvent = function (event) {
        switch (event) {
            case "loaded":
                setUpPages(self.pages);
                break;
        }
    };

    function setUpPages(pages) {
        try {
            if (!pages) return;
            if (self.isInstalled) return;
            if (pages.constructor === Object) {
                if (pages.type) {
                    self.pages = self.pages || {};
                    if (!self.pages[pages.type]) {
                        self.pages[pages.type] = pages;
                        var icon = pages.icon;
                        if (icon && icon.split("/").length > 1) {
                            icon = u.create(HTML.IMG, {
                                src: icon,
                                className: "icon drawer-menu-item-icon"
                            })
                        }
                        if (pages.menu) {
                            pages.drawerItem = main.drawer.add({
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
                    } else if (main.drawer.items[pages.type]) {
                        u.lang.updateNode(main.drawer.items[pages.type].labelNode, u.lang[pages.menu] || pages.menu);
                    }
                }
            } else if (pages.constructor === Array) {
                for (var i in pages) {
                    setUpPages(pages[i]);
                }
            }
        } catch (e) {
            console.error(e);
        }
    }

    function processPage(type) {
        try {
            var page = self.pages[type];
            if (!page) {
                main.holder = u.eventBus.holders[404];
                // main.turn(404, type);
                if (main.holder) {
                    if (type) {
                        main.holder.resume(type);
                    } else {
                        main.holder.resume();
                    }
                    main.actionbar.setTitle(main.holder.title);
                    u.lang.updateNode(main.drawer.headerPrimary, main.holder.title);
                } else {
                    window.location = "/";
                }
                return;
            }
            u.post("/rest/content", {
                resource: page.resource,
                locale: main.selectLang.value
            }).then(function (xhr) {
                u.clear(main.content);
                u.create(HTML.DIV, {
                    className: "content-normal",
                    innerHTML: xhr.response
                }, main.content);
                main.eventBus.fire("pages_done", type);
                u.progress.hide();
            }).catch(function (error, json) {
                console.error(json);
                u.clear(main.content);
                u.create(HTML.DIV, {
                    className: "content-centered",
                    innerHTML: u.lang.error || "Error"
                }, main.content);
                main.eventBus.fire("pages_done", type);
                u.progress.hide();
            });
            window.history.pushState({}, null, "/" + main.mainType + "/" + type);
            main.history.add(self.type, [type]);
            main.actionbar.setTitle(u.lang[page.title] || page.title);
            u.lang.updateNode(main.drawer.headerPrimary, u.lang[page.title] || page.title);
        } catch (e) {
            console.error(e);
        }
    }

}