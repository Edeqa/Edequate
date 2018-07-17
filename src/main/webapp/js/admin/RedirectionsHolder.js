/**
 * Copyright (C) Edeqa <http://www.edeqa.com>
 *
 * Created 7/10/18.
 */
function RedirectionsHolder(main) {
    var self = this;
    var u = main.edequate;

    this.category = DRAWER.SECTION_RESOURCES;
    this.type = "redirections";
    this.title = "Redirections";
    this.menu = "Redirections";
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

        u.create(HTML.BUTTON, { className:"icon notranslate button-inline", innerHTML: "add", title:"Add redirection", onclick: function(){
                editElement("", {}, "Add redirection");
            }}, titleNode);

        var tablePaths = u.table({
            id: "groups",
            className: "admins",
            caption: {
                items: [
                    { label: "Path" },
                    { label: "Code" },
                    { label: "Message" },
                    { label: "Redirect" },
                    { label: "MIME" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.getJSON("/admin/rest/data/redirections").then(function(json){
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
                        { innerHTML: u.clear(json[path].redirect) || "&#150"},
                        { innerHTML: u.clear(json[path].mime) || "&#150"}
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
                title: "Redirection",
                className: "redirection-edit-dialog",
                items: [
                    {type: HTML.INPUT, label: "Source path", required: true, tabindex: 1 },
                    {type: HTML.INPUT, label: "Redirection", tabindex: 4 },
                    {type: HTML.INPUT, label: "Error code", placeholder: "404", tabindex: 2 },
                    {type: HTML.INPUT, label: "Message", placeholder: "Not found", tabindex: 3 },
                    {type: HTML.INPUT, label: "MIME", placeholder: "MIME type", tabindex: 4 },
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
                            mime: mimeNode.value,
                            origin: idNode.origin
                        };
                        u.post("/admin/rest/redirections", { update: resultOptions}).then(function(){
                            u.progress.hide();
                            u.toast.show("Redirection saved");
                            dialog.close();
                            main.turn("redirections");
                        }).catch(function (error) {
                            u.progress.hide();
                            var json = JSON.parse(error.message);
                            var message = json.message || (reason && reason.statusText);
                            u.toast.error(message || "Error saving redirection");
                        });
                    }
                },
                neutral: {
                    label: u.create(HTML.SPAN, "Remove"),
                    onclick: function() {
                        u.post("/admin/rest/redirections", { remove: {path: idNode.origin}}).then(function(){
                            u.progress.hide();
                            u.toast.show("Redirection removed");
                            dialog.close();
                            main.turn("redirections");
                        }).catch(function (error) {
                            u.progress.hide();
                            var json = JSON.parse(error.message);
                            var message = json.message || (reason && reason.statusText);
                            u.toast.error(message || "Error removing redirection");
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
            var redirectNode = dialog.items[1];
            var codeNode = dialog.items[2];
            var messageNode = dialog.items[3];
            var mimeNode = dialog.items[4];

            idNode.value = id || "";
            if(id) {
                dialog.neutral.show();
            } else {
                dialog.neutral.hide();
            }
            idNode.origin = id || "";
            redirectNode.value = options.redirect || "";
            codeNode.value = options.code || "";
            messageNode.value = options.message || "";
            mimeNode.value = options.mime || "";

            dialog.setTitle(title || "/" + id);
            dialog.open();

        } catch(e) {
            console.error(e);
            // main.turn("admins");
        }
    }

}
