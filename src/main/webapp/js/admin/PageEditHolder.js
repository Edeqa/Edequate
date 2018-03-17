/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/11/17.
 */
function PageEditHolder(main) {
    var div;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "page";
    this.title = "Page Edit";
    var dialog;
    var sections;
    var locales;
    var locale;
    var editor;
    // this.preventState = true;



    var categories = [
        {"0": "Primary"},
        {"1": "Summary"},
        {"2": "Main"},
        {"3": "Explore"},
        {"4": "Share"},
        {"5": "Resources"},
        {"6": "Miscellaneous"},
        {"7": "Settings"},
        {"8": "Help"},
        {"9": "Last"}
    ];

    this.start = function() {
        div = main.content;
    };

    this.resume = function(action,id) {

        locale = locale || main.selectLang.value;

        switch(action) {
            case "edit":
                if(!id) {
                    main.turn("pages");
                    return;
                }
                break;
            case "add":
                break;
            default:
                main.turn("pages");
                return;
        }
        window.history.pushState({}, null, "/admin/page/" + action + (id ? "/" + id : ""));

        u.getJSON("/rest/locales").then(function(json){
            locales = json.message;

            u.getJSON("/rest/data/types").then(function(json) {
                var resolved = false;
                var ids = id.split(":");

                sections = [];
                for(var i in json.message) {
                    var section = {};
                    section[json.message[i]] = json.message[i].toUpperCaseFirst();
                    sections.push(section);
                }

                if(action === "add") {
                    editPage(ids, {initial: true, section: json.message}, {mode:"Add page"});
                    return;
                }

                for (var i in json.message) {
                    if(ids[0] && json.message[i] === ids[0]) {
                        resolved = true;
                        u.getJSON("/rest/data", {resource: "pages-" + json.message[i] + ".json"}).then(function(json) {
                            var pages = normalizeStructure(json, [[],[],[],[],[],[],[],[],[],[]]);
                            for(var i in pages[+ids[1]]) {
                                if(pages[+ids[1]][i].type === ids[2]) {
                                    var page = pages[+ids[1]][i];
                                    page.section = ids[0];
                                    page.initial = true;
                                    editPage(ids, page, {mode:"Edit page"});
                                    break;
                                }
                            }
                        }).catch(function(e,x){
                            console.error(e,x);
                        });
                    }
                }
                if(!resolved) {
                    main.turn("pages");
                }
            }).catch(function(e,x){
                console.error(e,x);
            });
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    function normalizeStructure(json, structure) {
        if(json instanceof Array) {
            for(var i in json) {
                normalizeStructure(json[i], structure);
            }
        } else if(json instanceof Object) {
            var category = +json.category;
            if(category < 0 || category > 9) {
                category = 0;
                json.category = 0;
            }
            structure[category].push(json);
        }
        return structure;
    }

    function editPage(ids, page, options) {
        try {
            dialog = dialog || u.dialog({
                title: options.mode,
                resizeable: true,
                className: "page-edit-dialog",
                items: [
                    {type: HTML.SELECT, label: "Section", values: sections},
                    {type: HTML.SELECT, label: "Category", values: categories},
                    {type: HTML.INPUT, label: "Name", prefix:""},
                    {type: HTML.SELECT, label: "Language", values: locales, value: locale, onchange: function() {
                            locale = localeNode.value;
                            populateWithLang(ids[0], dialog.initialOptions.title, titleNode, dialog.initialOptions.menu, menuNode);
                            populateContent(dialog.initialOptions.resource, contentNode);
                        }},
                    {type: HTML.INPUT, label: "Menu icon"},
                    {type: HTML.SELECT, label: "Menu name"},
                    {type: HTML.SELECT, label: "Title"},
                    {
                        type: HTML.SELECT,
                        label: "Priority",
                        values: [{"10": "Highest"}, {"9": "9"}, {"8": "8"}, {"7": "7"}, {"6": "6"}, {"5": "5"}, {"4": "4"}, {"3": "3"}, {"2": "2"}, {"1": "1"}, {"0": "Default"}, {"-1": "-1"}, {"-2": "-2"}, {"-3": "-3"}, {"-4": "-4"}, {"-5": "-5"}, {"-6": "-6"}, {"-7": "-7"}, {"-8": "-8"}, {"-9": "-9"}, {"-10": "Lowest"}]
                    },
                    {type: HTML.TEXTAREA, id:"page-content", editor:true, label: "Content"}
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    dismiss: false,
                    onclick: function () {
                        u.progress.show("Saving...");

                        var options = {
                            category: categoryNode.value,
                            icon: iconNode.value,
                            menu: menuNode.value,
                            section: sectionNode.value,
                            name: nameNode.value,
                            locale: localeNode.value,
                            title: titleNode.value,
                            priority: priorityNode.value,
                            content: contentNode.getValue()
                        };
                        u.post("/admin/rest/page", {initial: dialog.initialOptions, update: options}).then(function(result){
                            u.progress.hide();
                            u.toast.show("Page saved");
                            dialog.close();
                            main.turn("pages");
                        }).catch(function (code, reason) {
                            u.progress.hide();
                            u.toast.error("Error saving page" + (reason && reason.statusText ? ": " + reason.statusText : ""));
                        });
                    }
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel"),
                    onclick: function () {
                        main.turn("pages");
                    }
                }
            }, div.parentNode);
            dialog.setTitle(options.mode);
            var sectionNode = dialog.items[0];
            var categoryNode = dialog.items[1];
            var nameNode = dialog.items[2];
            var localeNode = dialog.items[3];
            var iconNode = dialog.items[4];
            var menuNode = dialog.items[5];
            var titleNode = dialog.items[6];
            var priorityNode = dialog.items[7];
            var contentNode = dialog.items[8];//.lastChild.lastChild;


            sectionNode.value = ids[0];
            categoryNode.value = ids[1];
            nameNode.value = ids[2] || "";
            iconNode.value = page.icon || "";
            priorityNode.value = page.priority || 0;

            dialog.initialOptions = page;

            populateWithLang(ids[0], page.title, titleNode, page.menu, menuNode);
            populateContent(page.resource, contentNode);

            dialog.open();
        } catch(e){
            console.error(e);
        }
    }

    function populateWithLang(section, titleValue, selectNode, menuValue, menuNode) {
        u.progress.show("Loading resources");

        function setStrings(json) {
            for(var x in json) {
                json[x] += " (" + x + ")";
            }
            selectNode.setOptions(json);
            selectNode.value = titleValue;

            menuNode.setOptions(json);
            menuNode.value = menuValue;

            u.progress.hide();
        }

        u.getJSON("/rest/resources", {resource: section +".json", locale: "en"}).then(function(json){
            if(locale !== "en") {
                u.getJSON("/rest/resources", {resource: section +".json", locale: locale || "en"}).then(function(j){
                    for(var x in j) {
                        json[x] = j[x];
                    }
                    setStrings(json);
                }).catch(function(e,x,j) {
                    console.error(e,x,j);
                    setStrings(json);
                });
            } else {
                setStrings(json);
            }
        });
    }

    function populateContent(resource, contentNode) {
        u.progress.show("Loading resources");
        contentNode.setValue("");
        if(resource) {
            u.progress.show("Loading");
            u.post("/rest/content", {
                resource: resource,
                locale: locale
            }).then(function (xhr) {
                contentNode.setValue(xhr.response);
                u.progress.hide();
            }).catch(function (error, json) {
                console.error("Error", error, json);
                contentNode.setValue("");
                u.progress.hide();
            });
        } else {
            contentNode.setValue("");
        }
    }
}
