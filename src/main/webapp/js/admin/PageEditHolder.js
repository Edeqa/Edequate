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
                    editPage(ids, {sections: json.message}, {mode:"Add page"});
                    return;
                }

                for (var i in json.message) {
                    if(ids[0] && json.message[i] === ids[0]) {
                        resolved = true;
                        u.getJSON("/rest/data", {resource: "pages-" + json.message[i] + ".json"}).then(function(json) {
                            var pages = normalizeStructure(json, [[],[],[],[],[],[],[],[],[],[]]);
                            for(var i in pages[+ids[1]]) {
                                if(pages[+ids[1]][i].type === ids[2]) {
                                    editPage(ids, pages[+ids[1]][i], {mode:"Edit page"});
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
                            locale = languageNode.value;
                            populateWithLang(ids[0], page.title, titleNode);
                            populateContent(page.resource, contentNode);
                        }},
                    {type: HTML.SELECT, label: "Title"},
                    {
                        type: HTML.SELECT,
                        label: "Priority",
                        values: [{"10": "Highest"}, {"9": "9"}, {"8": "8"}, {"7": "7"}, {"6": "6"}, {"5": "5"}, {"4": "4"}, {"3": "3"}, {"2": "2"}, {"1": "1"}, {"0": "Default"}, {"-1": "-1"}, {"-2": "-2"}, {"-3": "-3"}, {"-4": "-4"}, {"-5": "-5"}, {"-6": "-6"}, {"-7": "-7"}, {"-8": "-8"}, {"-9": "-9"}, {"-10": "Lowest"}]
                    },
                    // {type: HTML.TEXTAREA, id:"page-content", label: "Content"},
                    {type:HTML.DIV, className: "dialog-item-input page-item-textarea", content: u.create(HTML.DIV)
                            .place(HTML.LABEL, {innerHTML: "Content", className:"dialog-item-label"})
                            .place(HTML.DIV, {
                                className: "dialog-item-input-textarea",
                                content: u.create(HTML.DIV)
                                    .place(HTML.DIV, {id:"page-content"})
                            })
                    }
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    onclick: function () {
                        console.log("ok");
                        main.turn("pages");
                    }
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel"),
                    onclick: function () {
                        main.turn("pages");
                    }
                }
            });
            dialog.setTitle(options.mode);
            var sectionNode = dialog.items[0];
            var categoryNode = dialog.items[1];
            var nameNode = dialog.items[2];
            var languageNode = dialog.items[3];
            var titleNode = dialog.items[4];
            var priorityNode = dialog.items[5];
            var contentNode = dialog.items[6].lastChild.lastChild;

            sectionNode.value = ids[0];
            categoryNode.value = ids[1];
            nameNode.value = ids[2] || "";

            populateWithLang(ids[0], page.title, titleNode);
            priorityNode.value = page && page.priority || 0;
            // contentNode.value = page && page.resource || "";

            populateContent(page.resource, contentNode);
            dialog.open();
        } catch(e) {
            console.error(e);
        }
    }

    function populateWithLang(section, value, selectNode) {
        u.progress.show("Loading resources");
        u.getJSON("/rest/resources", {resource: section +".json", locale: locale || "en"}).then(function(json){
            for(var x in json) {
                json[x] += " (" + x + ")";
            }
            selectNode.setOptions(json);
            selectNode.value = value;
            u.progress.hide();
        });
    }

    function populateContent(resource, contentNode) {
        u.progress.show("Loading resources");
        if(resource) {
            u.progress.show("Loading");

            u.create(HTML.LINK, {href:"https://cdn.quilljs.com/1.3.6/quill.snow.css", rel:"stylesheet"}, document.head);

            u.require("https://cdn.quilljs.com/1.3.6/quill.js").then(function(result){
                console.log("result",result);
                u.post("/rest/content", {
                    resource: resource,
                    locale: locale
                }).then(function (xhr) {
                    contentNode.innerHTML = xhr.response;
                    editor = editor || new Quill("#page-content", {
                        theme: 'snow'
                    });
                    u.progress.hide();
                }).catch(function (error, json) {
                    contentNode.innerHTML = "";
                    u.progress.hide();
                });
            }).catch(function (error, json) {
                console.error(error, json);
                contentNode.innerHTML = "";
                u.progress.hide();
            })
        } else {
            contentNode.innerHTML = "";
        }
    }
}
