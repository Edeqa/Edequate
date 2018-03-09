/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Created 3/8/17.
 */
function PagesControlHolder(main) {
    var div;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "pages";
    this.title = "Pages";
    this.menu = "Pages";
    this.icon = "mode_edit";

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        u.create(HTML.H2, "Pages edit", div);

        var tree = new u.tree();
        div.appendChild(tree);

        u.getJSON("/rest/data/types").then(function(json){
            console.log(json.message);

            for(var i in json.message) {
                var ids = json.message[i].split(/[\-.]/);
                if(ids && ids[0] === "pages") {
                    console.log(ids[1]);
                    tree.add({
                        id: ids[1],
                        innerHTML: ids[1].toUpperCaseFirst()
                    });
                }

                u.getJSON("/rest/data", {resource: "pages-" + ids[1] + ".json"}).then(function(json){
                    var structure = parsePages(json);
                    for(var x in structure) {
                        tree.add({
                            id: ids[1] + ":" + structure[x].type,
                            innerHTML: structure[x].title,
                        });
                    }
                }).catch(function(e,x){
                    console.error(e,x);
                });

            }

        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    function parsePages(pages, structure) {
        var structure = structure || [];
        try {
            if (!pages) return;
            if (pages.constructor === Object) {
                if (pages.menu) {
                    if(!structure[pages.type]) {
                        structure[pages.type] = pages;
                    }
                }
            } else if (pages.constructor === Array) {
                for (var i in pages) {
                    parsePages(pages[i], structure);
                }
            }
        } catch(e) {
            console.error(e);
        }
        return structure;
    }

}
