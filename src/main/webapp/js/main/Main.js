/**
 * Edequate. Copyright (C) 2017-18 Edeqa <http://www.edeqa.com>
 *
 * Created 12/27/17.
 *
 * !!! IMPORTANT !!!
 * DO NOT EDIT THIS FILE
 * !!! IMPORTANT !!!
 */

function Main(u) {
    var self = this;

    this.start = function(arguments) {
        self.arguments = arguments = arguments || {};

        var info = arguments.info;
        self.mainType = arguments.type || "main";

        self.history = new HoldersHistory(self.mainType);
        window.addEventListener("popstate", function() {
            self.history.goBack();
        });

        self.layout = u.create(HTML.DIV, {className:"layout", role:"main"}, document.body);
        self.actionbar = u.actionBar({
            title: "Loading...",
            onbuttonclick: function(){
                try {
                    self.drawer.open();
                } catch(e) {
                    console.error(e);
                }
            }
        }, document.body);

        self.selectLang = u.create(HTML.SELECT, { className: "actionbar-select-lang changeable", value: u.load("lang"), onchange: function() {
                var lang = (this.value || navigator.language).toLowerCase().slice(0,2);
                u.save("lang", lang);
                self.loadResources(self.mainType + ".json");
                self.holder.resume();
            }}, self.actionbar).place(HTML.OPTION, { name: u.lang.loading, value:"" });

        u.require({src:"/rest/locales",isJSON:true}).then(function(json){
            console.log("locales", json.message);
            u.clear(self.selectLang);
            var count = 1;
            self.selectLang.place(HTML.OPTION, { innerHTML: "Default", value: "en" });
            for(var x in json.message) {
                // noinspection JSUnfilteredForInLoop
                self.selectLang.place(HTML.OPTION, { innerHTML: json.message[x], value: x });
                if(u.load("lang") === x) self.selectLang.selectedIndex = count;
                count++;
            }
        });

        this.turn = function(holderType, options) {
            self.content.scrollTop = 0;
            switchFullDrawer.call(self.content);

            self.drawer.close();
            if(u.eventBus.holders[holderType]) {
                self.holder = u.eventBus.holders[holderType];
                /** @namespace self.holder.preventState */
                if(!self.holder.preventState) {
                    window.history.pushState({}, null, "/" + self.mainType + "/" + holderType);
                }
            } else {
                console.log("Passing '" + holderType + "' to PagesHolder");
                self.holder = u.eventBus.holders["$pages"];//[404];
                options = [holderType].concat(options)
                // options = [holderType];
            }

            if(self.holder && self.holder.resume) {
                if(options && options instanceof Array && options.length > 0) {
                    self.holder.resume.apply(self.holder, options);
                } else if(options && options.constructor === String) {
                    self.holder.resume(options);
                } else {
                    self.holder.resume();
                }
                if(!self.holder.preventState) {
                    self.history.add(holderType, options);
                    self.actionbar.setTitle(self.holder.title);
                    u.lang.updateNode(self.drawer.headerPrimary, self.holder.title);
                }
            } else {
                window.location = "/";
            }
        };

        this.loadResources(self.mainType + ".json", function() {
            var dialogAbout = u.dialog({
                className: "about-dialog",
                itemsClassName: "about-dialog-items",
                buttonsClassName: "about-dialog-buttons",
                items: [
                    { innerHTML: "Edequate" },
                    { innerHTML: "&nbsp;" },
                    { content: [
                            u.create(HTML.IMG, {src: "/images/edeqa-logo.svg", className: "about-dialog-edeqa-logo"}),
                            u.create(HTML.DIV)
                                .place(HTML.DIV, { innerHTML: "Copyright &copy;2017-18 Edeqa" })
                                .place(HTML.A, {className: "about-dialog-edeqa-link", href: "http://www.edeqa.com", target: "_blank", rel:"noopener", innerHTML: "http://www.edeqa.com" })
                        ]},
                    {
                        enclosed: true,
                        label: u.lang.legal_information || "Legal information",
                        body: u.lang.loading.outerHTML,
                        className: "dialog-about-terms",
                        onopen: function(e) {
                            var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
                            u.post("/rest/content", {resource: "legal-information.html", locale: lang}).then(function(xhr){
                                e.body.innerHTML = xhr.response;
                            }).catch(function(error, json) {
                                console.error(error, json);
                                e.body.innerHTML = u.lang.error;
                            });
                        }
                    }
                ],
                positive: {
                    label: u.lang.ok
                }
            });

            self.drawer = new u.drawer({
                title: u.lang.title || "Title",
                // collapsed: u.load("drawer:collapsed"),
                logo: {
                    src: "/images/logo.svg"
                },
                onprimaryclick: function(){
                    console.log("onprimaryclick");
                },
                footer: {
                    className: "drawer-footer-label",
                    content: [
                        u.create(HTML.DIV, { className: "drawer-footer-link", innerHTML: "Powered with Edequate", onclick: function(){
                                dialogAbout.open();
                            }})
                    ]
                },
                sections: {
                    "0": u.lang.drawer_primary,
                    "1": u.lang.drawer_summary,
                    "2": u.lang.drawer_main,
                    "3": u.lang.drawer_explore,
                    "4": u.lang.drawer_share,
                    "5": u.lang.drawer_resources,
                    "6": u.lang.drawer_miscellaneous,
                    "7": u.lang.drawer_settings,
                    "8": u.lang.drawer_help,
                    "9": u.lang.drawer_last
                }
            }, document.body);

            u.getJSON("/rest/" + self.mainType).then(function(json){
                for(var i in json.message) {
                    // noinspection JSUnfilteredForInLoop
                    /** @namespace json.extra */
                    json.message[i] = json.extra + "/" + json.message[i];
                }
                if(json.message.indexOf("/js/main/PagesHolder.js") < 0) {
                    json.message.push("/js/main/PagesHolder.js");
                }
                u.eventBus.register(json.message, {
                    context: self,
                    onprogress: function (loaded) {
                        u.byId("loading-dialog-progress").innerHTML = Math.ceil(loaded / json.message.length * 100) + "%";
                    },
                    onstart: function () {
                        console.log("Holders started:", u.eventBus.holders);
                    },
                    onsuccess: function () {
                        u.eventBus.fire("loaded");
                        for(var x in u.eventBus.holders) {
                            var holder = u.eventBus.holders[x];
                            if(holder.menu) {
                                self.drawer.add({section: holder.category, id: holder.type, name: holder.menu, icon: holder.icon, priority: holder.priority, callback: function(){
                                        self.turn(this.type);
                                        return false;
                                    }.bind(holder)});
                            }
                        }

                        if(info) {
                            self.content.innerHTML = info;
                            self.actionbar.setTitle(u.lang.info);
                            u.byId("loading-dialog").hide();
                        } else {
                            var urlPath = new URL(window.location);
                            var path = urlPath.path.split("/");
                            var holderType;
                            if(path.length > 2 && path[1].toLowerCase() === self.mainType) {
                                path.shift();path.shift();
                                holderType = path.shift();
                            }
                            holderType = holderType || "home";
                            self.turn(holderType, path);
                        }
                        u.byId("loading-dialog").hide();
                    },
                    onerror: function (code, origin, error) {
                        console.error(code, origin, error);
                    }
                });
            });

            // noinspection JSUnusedGlobalSymbols
            self.content = u.create(HTML.DIV, {className:"content", onscroll: switchFullDrawer}, self.layout);

            self.buttonScrollTop = u.create(HTML.BUTTON, {
                className: "icon button-scroll-top changeable hidden",
                onclick: function() {
                    self.content.scrollTop = 0;
                    switchFullDrawer.call(self.content);
                },
                innerHTML: "keyboard_arrow_up"
            }, self.layout);
        });
    };

    this.loadResources = function(resource, callback) {
        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        u.lang.overrideResources({
            "default": "/resources/en/" + resource,
            resources: "/rest/resources",
            resource: resource,
            locale: lang,
            callback: callback
        });
    };

    function HoldersHistory(type) {
        var history = u.load("history:" + type) || [];

        this.add = function(holderType, options) {
            var previousState = history[history.length - 1] || {};
            var newState = {h: holderType, o:options};
            if(JSON.stringify(newState) !== JSON.stringify(previousState)) {
                history.push(newState);
                while(history.length > 100) {
                    history.shift();
                }
                u.save("history:" + type, history);
            }
        };

        this.goBack = function() {
            history.pop();
            var state = history.pop();
            if(state) {
                self.turn(state.h, state.o);
            }
        };

        this.clear = function() {
            history = [];
            u.save("history:" + type);
        }
    }

    function switchFullDrawer(){
        if(getComputedStyle(self.actionbar).display === "none") return;
        if(self.content.scrollTop > 200) {
            self.drawer.toggleSize(true);
            self.actionbar.toggleSize(true);
//                    clearTimeout(self.buttonScrollTop.hideTimeout);
            if(!self.buttonScrollTop.offsetHeight) {
                self.buttonScrollTop.hideTimeout = setTimeout(function(){
                    self.buttonScrollTop.hide(/*HIDING.OPACITY*/);
                }, 1500);
            }
            self.buttonScrollTop.show(/*HIDING.OPACITY*/);
        } else {
            self.drawer.toggleSize(false);
            self.actionbar.toggleSize(false);
            if(self.buttonScrollTop.offsetHeight) {
                self.buttonScrollTop.hide(/*HIDING.OPACITY*/);
            }
        }
    }

}