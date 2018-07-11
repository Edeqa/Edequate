/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 7/10/18.
 */
function IgnoredHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "ignored";
    this.title = "Ignored paths";
    this.menu = "Ignored paths";
    this.icon = "error_outline";
    this.priority = -10;
    this.scrollTop = 0;
    var div;
    var dialog;
    var alert;

    this.start = function() {
        div = main.content;
    };

    this.resume = function() {
        u.clear(div);
        var titleNode = u.create(HTML.H2, self.title, div);

        u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "add", title:"Add path", onclick: function(){
                editElement("", {}, "Add ignored path");
            }}, titleNode);

        var tablePaths = u.table({
            id: "groups",
            className: "admins",
            caption: {
                items: [
                    { label: "Path" },
                    { label: "Code" },
                    { label: "Message" },
                    { label: "Redirect"  }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.getJSON("/admin/rest/data/ignored").then(function(json){
            console.log(json);
            var paths = u.keys(json).sort();

            for(var i in paths) {
                var path = paths[i];

                tablePaths.add({
                    id: path,
                    className: "highlight",
                    onclick: function(){
                        // main.turn("admin", ["edit", this.id]);
                        editElement(this.id, this.origin);
                        return false;
                    },
                    cells: [
                        { innerHTML: u.clear(path) },
                        { innerHTML: u.clear(json[path].code) || "&#150" },
                        { innerHTML: u.clear(json[path].message) || "&#150" },
                        { innerHTML: u.clear(json[path].redirect) || "&#150"}
                    ]
                }).origin = json[path];
            }
        }).catch(function(e,x){
            console.error(e,x);
        });
    };

    function editElement(id, options, title) {
        try {
            dialog = dialog || u.dialog({
                title: "Ignored path",
                className: "ignored-edit-dialog",
                items: [
                    {type: HTML.INPUT, label: "Ignored path", required: true, tabindex: 1 },
                    {type: HTML.INPUT, label: "Error code", placeholder: "404", tabindex: 2 },
                    {type: HTML.INPUT, label: "Message", placeholder: "Not found", tabindex: 3 },
                    {type: HTML.INPUT, label: "Redirect", tabindex: 4 }
                ],
                positive: {
                    label: u.create(HTML.SPAN, "OK"),
                    dismiss: false,
                    onclick: function () {
                        u.progress.show("Saving...");
                        var resultOptions = {
                            path: idNode.value,
                            code: codeNode.value,
                            message: messageNode.value,
                            redirect: redirectNode.value,
                            origin: idNode.origin
                        };
                        u.post("/admin/rest/ignored", { update: resultOptions}).then(function(){
                            u.progress.hide();
                            u.toast.show("Ignored path saved");
                            dialog.close();
                            main.turn("ignored");
                        }).catch(function (error) {
                            u.progress.hide();
                            var json = JSON.parse(error.message);
                            var message = json.message || (reason && reason.statusText);
                            u.toast.error(message || "Error saving ignored path");
                        });
                    }
                },
                neutral: {
                    label: u.create(HTML.SPAN, "Remove"),
                    onclick: function() {
                        u.post("/admin/rest/ignored", { remove: {path: idNode.origin}}).then(function(){
                            u.progress.hide();
                            u.toast.show("Ignored path removed");
                            dialog.close();
                            main.turn("ignored");
                        }).catch(function (error) {
                            u.progress.hide();
                            var json = JSON.parse(error.message);
                            var message = json.message || (reason && reason.statusText);
                            u.toast.error(message || "Error removing ignored path");
                        });
                    }
                },
                negative: {
                    label: u.create(HTML.SPAN, "Cancel"),
                    onclick: function() {

                    }
                }
            }, div.parentNode);
            // dialog.setTitle(options.mode);
            var idNode = dialog.items[0];
            var codeNode = dialog.items[1];
            var messageNode = dialog.items[2];
            var redirectNode = dialog.items[3];

            idNode.value = id || "";
            if(id) {
                dialog.neutral.show();
            } else {
                dialog.neutral.hide();
            }
            idNode.origin = id || "";
            codeNode.value = options.code || "";
            messageNode.value = options.message || "";
            redirectNode.value = options.redirect || "";

            dialog.setTitle(title || "/" + id);
            dialog.open();

        } catch(e) {
            console.error(e);
            // main.turn("admins");
        }
    }

}
