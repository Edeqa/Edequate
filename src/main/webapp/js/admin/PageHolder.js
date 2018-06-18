/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/11/18.
 */
function PageHolder(main) {
    var u = main.edequate;

    this.type = "page";
    this.title = "Pages";
    this.preventHistory = true;
    var dialog;
    var dialogSection;
    var dialogCategory;
    var dialogConfirm;
    var dialogAddString;
    var locales;
    var locale;
    var div;
    var localeNode;
    var menuNode;
    var titleNode;
    var contentNode;
    var icons;
    var strings;
    var structure;
    var categoriesSelect;

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

        var ids = (id || "").split(":");
        u.require([
            {src:"/rest/locales", isJSON:true},
            {src:"/rest/resources", body: {resource:"icons.json"}, isJSON: true},
            {src:"/rest/data", isJSON: true, body: {resource: "pages-" + ids[0] + ".json"}}
        ], function(jsonLocales, jsonIcons, jsonStructure){

            locales = jsonLocales.message;
            icons = jsonIcons || {};
            structure = main.buildTree(jsonStructure);

            categoriesSelect = [];
            for(var i in structure.categories) {
                var option = {};
                option[structure.categories[i].category] = structure.categories[i].title;
                categoriesSelect.push(option);
            }

            if(action === "add") {
                if(ids.length === 1) {
                    editPage(ids, {mode: "add"});
                } else if(ids.length === 2) {
                    editPage(ids, {mode: "add", category: structure.categories[ids[1]]});
                }
            } else if(action === "edit") {
                if(ids.length === 1) {
                    editSection(ids, structure);
                } else if(ids.length === 2) {
                    editCategory(ids, structure.categories[ids[1]]);
                } else {
                    editPage(ids, {mode: "edit", category: structure.categories[ids[1]]});
                }
            } else {
                main.turn("pages");
            }
        }).catch(function(e,x){
            console.error(e,x);
            u.toast.error("Error processing page, try again later");
        });
    };


    function editSection(ids, section) {
        section = section || {};
        try {
            dialogSection = dialogSection || u.dialog({
                title: "Edit section",
                items: [
                    {type: HTML.INPUT, label: "Section", disabled: true},
                    {type: HTML.INPUT, label: "Title"}
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    dismiss: false,
                    onclick: function () {
                        u.progress.show("Saving section...");
                        var options = {
                            section: dialogSection.initialOptions[0],
                            title: titleNode.value
                        };
                        u.post("/admin/rest/page", {section: options}).then(function(){
                            main.eventBus.holders.$pages.start();
                            dialogSection.close();
                            main.turn("pages");
                            u.progress.hide();
                            if(options.section === "admin") {
                                window.location.reload(true);
                            }
                            u.toast.show("Section saved");
                        }).catch(function (code, reason) {
                            u.progress.hide();
                            u.toast.error("Error saving section" + (reason && reason.statusText ? ": " + reason.statusText : ""));
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
            var sectionNode = dialogSection.items[0];
            sectionNode.value = ids[0];

            titleNode = dialogSection.items[1];

            titleNode.value = section.title && section.title.dataset && section.title.dataset.lang || section.title && section.title.innerText || section.title || "";

            dialogSection.setTitle("Edit section: %s".sprintf((ids[0] || "").toUpperCaseFirst()));

            dialogSection.initialOptions = ids;

            dialogSection.open();
            titleNode.focus();
        } catch(e){
            console.error(e);
        }
    }

    function editCategory(ids, category) {
        category = category || {};
        try {
            dialogCategory = dialogCategory || u.dialog({
                title: "Edit category",
                items: [
                    {type: HTML.SELECT, label: "Title", onchange: onselect},
                    {type: HTML.CHECKBOX, label: "Show title"}
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    dismiss: false,
                    onclick: function () {
                        u.progress.show("Saving category...");
                        var options = {
                            section: dialogCategory.initialOptions[0],
                            category: dialogCategory.initialOptions[1],
                            title: titleNode.value,
                            explicit: explicitNode.checked
                        };
                        u.post("/admin/rest/page", {category: options}).then(function(){
                            main.eventBus.holders.$pages.start();
                            dialogCategory.close();
                            main.turn("pages");
                            u.progress.hide();
                            if(options.section === "admin") {
                                u.lang.updateNode(main.drawer.sections[options.category].labelNode, u.lang[options.title] || options.title);
                            }
                            u.toast.show("Category saved");
                        }).catch(function (code, reason) {
                            u.progress.hide();
                            u.toast.error("Error saving category" + (reason && reason.statusText ? ": " + reason.statusText : ""));
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
            titleNode = dialogCategory.items[0];
            var explicitNode = dialogCategory.items[1];

            titleNode.value = category.title && category.title.dataset && category.title.dataset.lang || category.title && category.title.innerText || category.title || "";
            explicitNode.checked = category.explicit;

            dialogCategory.setTitle("Edit category: %s / %s".sprintf((ids[0] || "").toUpperCaseFirst(), category.title ? category.title.innerText || category.title : ids[1]));

            dialogCategory.initialOptions = ids;

            populateWithLang(ids[0], [category.title], function() {
                titleNode.value = category.title && category.title.dataset && category.title.dataset.lang || category.title && category.title.innerText || category.title || "";
            });
            dialogCategory.open();
            titleNode.focus();
        } catch(e){
            console.error(e);
        }
    }

    function editPage(ids, options) {
        try {
            var page = {
                section: ids[0],
                category: ids[1]
            };
            if(options.category && options.category.pages) {
                for (var i in options.category.pages) {
                    if (options.category.pages[i].type === ids[2]) {
                        page = options.category.pages[i];
                        page.section = ids[0];
                        page.category = ids[1];
                    }
                }
            }
            dialog = dialog || u.dialog({
                title: "Edit page",
                resizeable: true,
                className: "page-edit-dialog",
                items: [
                    {type: HTML.SELECT, label: "Section", disabled: true},
                    {type: HTML.SELECT, label: "Category", values: categoriesSelect},
                    {type: HTML.INPUT, label: "Name"},
                    {type: HTML.SELECT, label: "Language", values: locales, value: locale, onchange: function() {
                            function changeLocale() {
                                u.progress.show("Loading resources");
                                locale = localeNode.value;
                                populateWithLang(ids[0], [dialog.initialOptions.title, dialog.initialOptions.menu], function() {
                                    titleNode.value = dialog.initialOptions.title;
                                    menuNode.value = dialog.initialOptions.menu;
                                });
                                populateContent(dialog.initialOptions.resource);
                            }
                            var oldValue = this.oldValue;
                            if(contentNode.changed) {
                                dialogConfirm = dialogConfirm || new u.dialog({
                                    title: "Page changed",
                                    className: "page-changed-dialog",
                                    items: [
                                        { innerHTML: "Page was changed. Do you want to discard changes and reload page?" }
                                    ],
                                    positive: {
                                        label: u.create(HTML.SPAN, "Yes"),
                                        onclick: function () {
                                            changeLocale();
                                        }
                                    },
                                    negative: {
                                        label: u.create(HTML.SPAN, "No"),
                                        onclick: function () {
                                            localeNode.value = oldValue;
                                        }
                                    }
                                });
                                dialogConfirm.open();
                            } else {
                                changeLocale();
                            }
                        }
                    },
                    {type: HTML.SELECT, label: "Menu icon", itemClassName: "icon", values: icons},
                    {type: HTML.SELECT, label: "Menu name", onchange: onselect },
                    {type: HTML.SELECT, label: "Title", onchange: onselect},
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
                        if(!nameNode.value) {
                            u.toast.error("Name must be defined");
                            nameNode.focus();
                            return;
                        }
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
                        u.post("/admin/rest/page", {initial: dialog.initialOptions, update: options}).then(function(){
                            contentNode.changed = false;
                            /** @namespace main.eventBus.holders.$pages */
                            main.eventBus.holders.$pages.start();
                            dialog.close();
                            main.turn("pages");
                            u.progress.hide();
                            u.toast.show("Page saved");
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
            dialog.setTitle(options.mode === "edit" ? "Edit page" : "Add page");
            var sectionNode = dialog.items[0];
            var categoryNode = dialog.items[1];
            var nameNode = dialog.items[2];
            localeNode = dialog.items[3];
            var iconNode = dialog.items[4];
            menuNode = dialog.items[5];
            titleNode = dialog.items[6];
            var priorityNode = dialog.items[7];
            contentNode = dialog.items[8];

            if(ids[1] === "10") {
                categoryNode.parentNode.hide();
                iconNode.parentNode.hide();
                menuNode.parentNode.hide();
            } else {
                categoryNode.parentNode.show();
                iconNode.parentNode.show();
                menuNode.parentNode.show();
            }

            var opts = {};opts[page.section] = page.section.toUpperCaseFirst();
            sectionNode.setOptions(opts);
            sectionNode.value = page.section;
            categoryNode.value = page.category;
            categoryNode.disabled = !!page.category;

            nameNode.value = page.type || "";
            iconNode.value = page.icon || "";
            priorityNode.value = page.priority || 0;

            dialog.initialOptions = page;

            populateWithLang(ids[0], [page.title, page.menu], function() {
                titleNode.value = page.title;
                menuNode.value = page.menu;
            });
            populateContent(page.resource);

            dialog.open();

            if(options.mode === "add" && ids.length === 1) {
                categoryNode.focus();
            } else if(options.mode === "add") {
                nameNode.focus();
            } else if(options.mode === "edit") {
                contentNode.focus();
            }

        } catch(e){
            console.error(e);
        }
    }

    function populateWithLang(section, optional, callback) {
        if (section) {
            u.progress.show("Loading resources");
            u.getJSON("/rest/resources", {
                resource: ["common.json", section + ".json"],
                locale: locale || "en"
            }).then(function (json) {
                for (var x in json) {
                    json[x] += " (" + x + ")";
                }
                strings = json;
                setStrings(strings, optional, callback);
                u.progress.hide();
            }).catch(function (e, x, j) {
                console.error(e, x, j);
                u.toast.error("Error loading strings")
            });
        } else {
            strings = {};
            setStrings(strings, optional, callback);
        }
    }

    function setStrings(json, optional, callback) {
        json[""] = "[ Add ]";
        if(optional) {
            for(var i in optional) {
                if(!optional[i]) continue;
                var value = optional[i].innerText || optional[i];
                if(value && !json[value]) {
                    json[value] = value;
                }
            }
        }
        titleNode.setOptions(json);
        menuNode && menuNode.setOptions(json);
        if(callback) callback();
    }

    function populateContent(resource) {
        if(resource) {
            u.progress.show("Loading resources");
            contentNode.setValue("");
            if(resource) {
                u.progress.show("Loading");
                u.post("/rest/content", {
                    resource: resource,
                    locale: locale
                }).then(function (xhr) {
                    contentNode.setValue(xhr.response);
                }).catch(function (error, json) {
                    console.error("Error", error, json);
                    contentNode.setValue("");
                    u.progress.hide();
                });
            } else {
                contentNode.setValue("");
            }
        } else {
            contentNode.setValue("");
        }
    }

    function onselect(event) {
        console.log(this.value, event);
        if(!this.value) {
            dialogAddString = dialogAddString || new u.dialog({
                title: "Add string",
                className: "add-locale-dialog",
                items: [
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    onclick: function () {
                        console.log("SAVE STRING");
                        setStrings(strings, [dialogAddString.items[0].value], function() {
                            dialogAddString.itemNode.value = dialogAddString.items[0].value;
                        });
                    }
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel"),
                    onclick: function () {
                        dialogAddString.itemNode.value = null;
                    }
                }
            });
            dialogAddString.clearItems();
            var stringNode = dialogAddString.add({
                label: "String", type: HTML.INPUT
            });
            // for(var x in locales) {
            //     dialogAddString.add({
            //         type: HTML.INPUT, id: x, label: locales[x]
            //     });
            // }
            dialogAddString.itemNode = this;
            dialogAddString.open();
            stringNode.focus();
        }
    }
}
