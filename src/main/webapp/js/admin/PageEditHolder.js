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
    // this.preventState = true;

    var sectionsNames = {
        "0": "Primary",
        "1": "Summary",
        "2": "Main",
        "3": "Explore",
        "4": "Share",
        "5": "Resources",
        "6": "Miscellaneous",
        "7": "Settings",
        "8": "Help",
        "9": "Last"
    };

    this.start = function() {
        div = main.content;
    };

    this.resume = function(action,id) {

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

        u.getJSON("/rest/data/types").then(function(json) {
            var sections = [];
            for(var i in json.message) {
                sections.push(json.message[i].replace(/pages-(.*?)\.json/, "$1"));
            }
            var resolved = false;
            var ids = id.split(":");
            if(action === "add") {
                editPage(ids, {sections: sections}, {mode:"Add page"});
                return;
            }
            for (var i in json.message) {
                if(ids[0] && sections[i] === ids[0]) {
                    resolved = true;
                    u.getJSON("/rest/data", {resource: json.message[i]}).then(function(json) {
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

        dialog = dialog || u.dialog({
            title: options.mode,
            resizeable: true,
            className: "page-edit-dialog",
            items: [
                { type: HTML.INPUT, label: "Section" },
                { type: HTML.SELECT, label: "Category", values: sectionsNames},
                { type: HTML.INPUT, label: "Name" },
                { type: HTML.INPUT, label: "Title" },
                { type: HTML.SELECT, label: "Priority", values: {"10":"Highest", "9":"9","8":"8","7":"7","6":"6","5":"5","4":"4","3":"3","2":"2","1":"1","0":"Default","-1":"-1","-2":"-2","-3":"-3","-4":"-4","-5":"-5","-6":"-6","-7":"-7","-8":"-8","-9":"-9","-10":"Lowest"}},
                { type: HTML.INPUT, label: "Content" },
            ],
            positive: {
                label: u.create(HTML.SPAN, "OK"),
                onclick: function() {
                    console.log("ok");
                    main.turn("pages");
                }
            },
            negative: {
                label: u.create(HTML.SPAN, "Cancel"),
                onclick: function() {
                    main.turn("pages");
                }
            }
        });
        dialog.setTitle(options.mode);
        sectionNode = dialog.items[0];
        categoryNode = dialog.items[1];
        nameNode = dialog.items[2];
        titleNode = dialog.items[3];
        priorityNode = dialog.items[4];
        contentNode = dialog.items[5];

        sectionNode.value = ids[0];
        categoryNode.value = ids[1];
        nameNode.value = ids[2] || "";
        titleNode.value = page && page.title || "";
        priorityNode.value = page && page.priority || 0;
        contentNode.value = page && page.resource || "";

        dialog.open();

    }

}
