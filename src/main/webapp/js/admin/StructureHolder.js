/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 3/8/18.
 */
function StructureHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "pages";
    this.title = "Pages";
    this.menu = "Pages";
    this.icon = "mode_edit";
    this.scrollTop = 0;

    var div;
    var dialogConfirm;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, "Structure", div);

        var tree = new u.tree({hideRoot:true, expanded:true});
        div.appendChild(tree);

        u.create(HTML.BUTTON, { className:"icon button-inline", innerHTML: "expand_more", title:"Expand all", onclick: function(){
                tree.expand();
            }}, titleNode);
        u.create(HTML.BUTTON, { className:"icon button-inline", innerHTML: "expand_less", title:"Collapse all", onclick: function(){
                tree.collapse();
            }}, titleNode);

        u.getJSON("/rest/data/types").then(function(json){
            for(var i in json.message) {
                var id = json.message[i];
                if(!id) return;
                var branch = tree.add({
                    id: id,
                    titleClassName: "tree-pages-item-title",
                    content: u.create(HTML.DIV, {className:"tree-pages-root"})
                        .place(HTML.DIV, {innerHTML: id.toUpperCaseFirst()})
                        .place(HTML.BUTTON, {
                            innerHTML:"add",
                            title: "Add page into section",
                            className:"icon notranslate tree-item-icon",
                            onclick: function (e) {
                                e.stopPropagation();
                                self.scrollTop = main.content.scrollTop;
                                main.turn("page", ["add",this.parentNode.item.path]);
                            }
                        })
                        .place(HTML.BUTTON, {
                            innerHTML: "edit",
                            title: "Edit section",
                            className: "icon notranslate tree-item-icon",
                            onclick: function (e) {
                                e.stopPropagation();
                                self.scrollTop = main.content.scrollTop;
                                main.turn("page", ["edit", this.parentNode.item.path]);
                            }
                        })
                        .place(HTML.BUTTON, {
                            innerHTML:"expand_more",
                            title: "Expand all",
                            className:"icon notranslate tree-item-icon",
                            onclick: function (e) {
                                e.stopPropagation();
                                this.parentNode.item.expand();
                            }
                        })
                        .place(HTML.BUTTON, {
                            innerHTML:"expand_less",
                            title: "Collapse all",
                            className:"icon notranslate tree-item-icon",
                            onclick: function (e) {
                                e.stopPropagation();
                                this.parentNode.item.collapse();
                            }
                        })
                });
                u.require([
                    {src:"/rest/data", body: {resource: "pages-" + id + ".json"}, isJSON:true},
                    {src:"/rest/" + id, isJSON:true}
                ], function(json, json1){
                    try {
                        var id = this.id;
                        var structure = main.buildTree(json);

                        if(structure.title) u.lang.updateNode(this.titleNode.firstChild, u.lang[structure.title] || structure.title);

                        for (var x in structure.categories) {
                            var category = this.items[x] || this.add({
                                id: x,
                                priority: -(+x),
                                content: u.create(HTML.DIV, {className: "tree-pages-category"})
                                    .place(HTML.DIV, {innerHTML: structure.categories[x].title.cloneNode(true)})
                                    .place(HTML.BUTTON, {
                                        innerHTML: "add",
                                        title: "Add page into category",
                                        className: "icon notranslate tree-item-icon",
                                        onclick: function (e) {
                                            e.stopPropagation();
                                            self.scrollTop = main.content.scrollTop;
                                            main.turn("page", ["add", this.parentNode.item.path]);
                                        }
                                    })
                                    .place(HTML.BUTTON, {
                                        innerHTML: "edit",
                                        title: "Edit category",
                                        className: "icon notranslate tree-item-icon hidden",
                                        onclick: function (e) {
                                            e.stopPropagation();
                                            self.scrollTop = main.content.scrollTop;
                                            main.turn("page", ["edit", this.parentNode.item.path]);
                                        }
                                    })
                                    .place(HTML.BUTTON, {
                                        innerHTML: "visibility",
                                        title: "Category title is shown",
                                        className: "icon notranslate tree-item-icon hidden",
                                        onclick: function (e) {
                                            e.stopPropagation();
                                            var category = this.parentNode.item.id;
                                            var options = {
                                                path: this.parentNode.item.path,
                                                explicit: false
                                            };
                                            u.post("/admin/rest/page", {category: options}).then(function () {
                                                if (id === "admin") main.drawer.sections[category].labelNode.hide();
                                                this.hide();
                                                this.parentNode.parentNode.parentNode.hideButtonNode.show();
                                            }.bind(this)).catch(function (code, reason) {
                                                u.toast.error("Error switching title" + (reason && reason.statusText ? ": " + reason.statusText : ""));
                                            });
                                        }
                                    })
                                    .place(HTML.BUTTON, {
                                        innerHTML: "visibility_off",
                                        title: "Category title is hidden",
                                        className: "icon notranslate tree-item-icon hidden",
                                        onclick: function (e) {
                                            e.stopPropagation();
                                            var category = this.parentNode.item.id;
                                            var options = {
                                                path: this.parentNode.item.path,
                                                explicit: true
                                            };
                                            u.post("/admin/rest/page", {category: options}).then(function () {
                                                if (id === "admin") main.drawer.sections[category].labelNode.show();
                                                this.hide();
                                                this.parentNode.parentNode.parentNode.showButtonNode.show();
                                            }.bind(this)).catch(function (code, reason) {
                                                u.toast.error("Error switching title" + (reason && reason.statusText ? ": " + reason.statusText : ""));
                                            });
                                        }
                                    })
                            });
                            category.editButtonNode = category.titleNode.childNodes[2];
                            category.showButtonNode = category.titleNode.childNodes[3];
                            category.hideButtonNode = category.titleNode.childNodes[4];
                            for (var y in structure.categories[x].pages) {
                                var values = structure.categories[x].pages[y];
                                category.add({
                                    id: values.type,
                                    priority: values.priority,
                                    content: u.create(HTML.DIV, {className: "tree-pages-item-leaf"})
                                        .place(HTML.DIV, {
                                            innerHTML: values.title
                                        })
                                        .place(HTML.BUTTON, {
                                            innerHTML: "edit",
                                            title: "Edit page",
                                            className: "icon notranslate tree-item-icon",
                                            onclick: function (e) {
                                                e.stopPropagation();
                                                self.scrollTop = main.content.scrollTop;
                                                main.turn("page", ["edit", this.parentNode.item.path]);
                                            }
                                        })
                                        .place(HTML.A, {
                                            innerHTML: "[" + values.type + "]",
                                            // className: "icon notranslate tree-item-icon",
                                            href: "/" + this.path + "/" + values.type,
                                            target: "_blank"
                                        })
                                        .place(HTML.BUTTON, {
                                            innerHTML: "clear",
                                            title: "Remove page",
                                            className: "icon notranslate tree-item-icon" + (values.persistent ? " hidden" : ""),
                                            onclick: function (e) {
                                                e.stopPropagation();
                                                dialogConfirm = dialogConfirm || new u.dialog({
                                                    title: "Removing page",
                                                    items: [
                                                        {innerHTML: "Page will be removed. Continue?"}
                                                    ],
                                                    positive: {
                                                        label: u.create(HTML.SPAN, "Yes"),
                                                        onclick: function () {
                                                            u.progress.show("Removing page...");
                                                            var ids = dialogConfirm.current.path.split(":");
                                                            var options = {
                                                                path: dialogConfirm.current.path,
                                                                name: ids[2]
                                                            };
                                                            u.post("/admin/rest/page", {remove: options}).then(function () {
                                                                u.progress.hide();
                                                                u.toast.show("Page removed");
                                                                main.turn("pages");
                                                                // main.drawer.remove(options.name);
                                                            }).catch(function (code, reason) {
                                                                u.progress.hide();
                                                                u.toast.error("Error removing page" + (reason && reason.statusText ? ": " + reason.statusText : ""));
                                                            });
                                                        }
                                                    },
                                                    negative: {
                                                        label: u.create(HTML.SPAN, "No")
                                                    }
                                                });
                                                dialogConfirm.open();
                                                dialogConfirm.current = this.parentNode.item;
                                            }
                                        })
                                });
                                if (category.id !== "10") {
                                    category.editButtonNode.show();
                                    if (structure.categories[category.id].explicit) {
                                        category.showButtonNode.show();
                                    } else {
                                        category.hideButtonNode.show();
                                    }
                                }
                                main.content.scrollTop = self.scrollTop;
                            }
                        }
                        for (var i in json1.message) {
                            u.require(json1.extra + "/" + json1.message[i], main, function (holder) {
                                var category = holder && holder.category;
                                category = this.items["" + category];
                                if (category && "menu" in holder) {
                                    category.add({
                                        id: holder.type,
                                        priority: holder.priority,
                                        content: u.create(HTML.DIV, {className: "tree-item-title"})
                                            .place(HTML.DIV, {
                                                innerHTML: holder.title || holder.moduleName,
                                                title: holder.moduleName + "'s responsibility, can not be edited"
                                            })
                                            .place(HTML.DIV, {
                                                innerHTML: "lock_outline",
                                                className: "icon notranslate tree-item-icon",
                                                title: holder.moduleName + "'s responsibility, can not be edited"
                                            })
                                            .place(HTML.A, {
                                                innerHTML: "[" + holder.type + "]",
                                                // className: "icon notranslate tree-item-icon",
                                                href: "/" + this.path + "/" + holder.type,
                                                target: "_blank"
                                            })
                                    });
                                    if (category.id !== "10") {
                                        category.editButtonNode.show();
                                        if (structure.categories[category.id].explicit) {
                                            category.showButtonNode.show();
                                        } else {
                                            category.hideButtonNode.show();
                                        }
                                    }
                                }
                                main.content.scrollTop = self.scrollTop;
                            }.bind(this))
                        }
                    } catch(e) {
                        console.error(e)
                    }
                }.bind(branch)).catch(function(e,x){
                    console.error(e,x);
                });
            }
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case "settings":
                object.appendChild(u.create(HTML.DIV, "Structure settings"));
                break;
        }
        return true;
    }
}
